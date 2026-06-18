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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.ConnectionState;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionStateListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZooKeeper 连接状态监听器，负责监听 ZooKeeper 客户端的连接状态变化。
 * 将 Curator 框架的连接状态事件转换为 DolphinScheduler 内部的 ConnectionState 枚举，
 * 并回调给上层业务监听器处理。
 *
 * 状态映射关系：
 * - LOST / READ_ONLY -> DISCONNECTED（连接断开）
 * - RECONNECTED -> RECONNECTED（重新连接）
 * - SUSPENDED -> SUSPENDED（连接挂起）
 */
public final class ZookeeperConnectionStateListener implements ConnectionStateListener {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperConnectionStateListener.class);

    /** 上层业务注册的连接状态监听器，用于接收转换后的连接状态事件 */
    private final ConnectionListener listener;

    /**
     * 构造连接状态监听器。
     *
     * @param listener 上层业务连接监听器，不能为空
     */
    public ZookeeperConnectionStateListener(ConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Curator 框架连接状态变化回调方法。
     * 将 Curator 的 ConnectionState 转换为 DolphinScheduler 内部状态并通知业务层。
     *
     * @param client   Curator 客户端实例
     * @param newState ZooKeeper 连接的新状态
     */
    @Override
    public void stateChanged(CuratorFramework client,
                             org.apache.curator.framework.state.ConnectionState newState) {
        switch (newState) {
            case LOST:
                // 连接丢失，通知上层连接已断开
                logger.warn("Registry disconnected");
                listener.onUpdate(ConnectionState.DISCONNECTED);
                break;
            case RECONNECTED:
                // 重新连接成功，通知上层连接已恢复
                logger.info("Registry reconnected");
                listener.onUpdate(ConnectionState.RECONNECTED);
                break;
            case SUSPENDED:
                // 连接暂时挂起，通知上层连接异常
                logger.warn("Registry suspended");
                listener.onUpdate(ConnectionState.SUSPENDED);
                break;
            default:
                break;
        }
    }
}
