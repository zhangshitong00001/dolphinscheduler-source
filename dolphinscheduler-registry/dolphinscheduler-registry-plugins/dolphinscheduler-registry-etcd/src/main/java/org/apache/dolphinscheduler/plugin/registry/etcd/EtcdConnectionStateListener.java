/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.registry.etcd;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.etcd.jetcd.Client;

/**
 * Etcd 连接状态监听器，通过周期性探测 Etcd 集群可达性来监测连接状态变化。
 *
 * 与 ZooKeeper 的被动回调不同，Etcd 客户端不主动推送连接状态变化，
 * 因此采用定时轮询方式：通过尝试创建租约（grant）来检测连接是否正常，
 * 如果请求成功则判定为 CONNECTED，如果出现异常则判定为 DISCONNECTED。
 * 当检测到状态变化时，通知所有已注册的 ConnectionListener。
 * 实现 AutoCloseable 接口以支持资源清理。
 */
public class EtcdConnectionStateListener implements AutoCloseable {

    /** 已注册的连接状态监听器列表，使用线程安全的同步列表 */
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());

    /** 定时探测连接状态的线程池，使用守护线程避免阻塞 JVM 退出 */
    private final ScheduledExecutorService scheduledExecutorService;

    /** 被监控的 Etcd 客户端 */
    private final Client client;

    /** 上一次监控到的连接状态，使用 volatile 保证多线程可见性 */
    private volatile ConnectionState connectionState;

    /**
     * 构造 Etcd 连接状态监听器。
     *
     * @param client Etcd 客户端实例
     */
    public EtcdConnectionStateListener(Client client) {
        this.client = client;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("EtcdConnectionStateListenerThread").setDaemon(true).build());
    }

    /**
     * 添加连接状态变化的回调监听器。
     *
     * @param connectionListener 连接状态变化监听器
     */
    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    /**
     * 关闭连接状态监听器，清理线程池并清空监听器列表。
     */
    @Override
    public void close() throws Exception {
        connectionListeners.clear();
        scheduledExecutorService.shutdownNow();
    }

    /**
     * 探测当前 Etcd 连接状态。
     * 通过向 Etcd 申请一个租约（grant 1）来检测连接是否正常：
     * - 如果请求成功，说明连接正常，返回 CONNECTED
     * - 如果出现 ExecutionException 或 InterruptedException，说明连接异常，返回 DISCONNECTED
     *
     * @return 当前连接状态
     */
    private ConnectionState currentConnectivityState() {
        try {
            // 尝试申请一个租约来验证连接可达性
            client.getLeaseClient().grant(1).get().getID();
            return ConnectionState.CONNECTED;
        } catch (ExecutionException e) {
            return ConnectionState.DISCONNECTED;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConnectionState.DISCONNECTED;
        }
    }

    /**
     * 启动定时连接状态探测任务。
     * 每隔 500 毫秒探测一次连接状态，当检测到状态变化时通知所有监听器。
     * 支持三种状态转换：
     * - CONNECTED -> DISCONNECTED：连接断开
     * - DISCONNECTED -> CONNECTED：重新连接
     * - null -> 首次状态：初始化连接状态
     */
    public void start() {
        long initialDelay = 500L;
        long delay = 500L;
        this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
            ConnectionState currentConnectionState = currentConnectivityState();
            // 状态未变化，无需通知
            if (currentConnectionState == connectionState) {
                return;
            }
            if (connectionState == ConnectionState.CONNECTED) {
                if (currentConnectionState == ConnectionState.DISCONNECTED) {
                    connectionState = ConnectionState.DISCONNECTED;
                    triggerListener(ConnectionState.DISCONNECTED);
                }
            } else if (connectionState == ConnectionState.DISCONNECTED) {
                if (currentConnectionState == ConnectionState.CONNECTED) {
                    connectionState = ConnectionState.CONNECTED;
                    triggerListener(ConnectionState.RECONNECTED);
                }
            } else if (connectionState == null) {
                // 首次探测，记录初始状态
                connectionState = currentConnectionState;
                triggerListener(connectionState);
            }
        },
                initialDelay,
                delay,
                TimeUnit.MILLISECONDS);
    }

    /**
     * 通知所有已注册的连接状态监听器。
     *
     * @param connectionState 新的连接状态
     */
    private void triggerListener(ConnectionState connectionState) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onUpdate(connectionState);
        }
    }
}
