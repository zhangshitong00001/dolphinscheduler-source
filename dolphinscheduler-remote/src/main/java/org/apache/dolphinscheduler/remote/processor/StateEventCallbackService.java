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

package org.apache.dolphinscheduler.remote.processor;

import static org.apache.dolphinscheduler.common.constants.Constants.HTTP_CONNECTION_REQUEST_TIMEOUT;
import static org.apache.dolphinscheduler.common.constants.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.netty.channel.Channel;

/**
 * 状态事件回调服务。管理远程通道缓存，提供同步/异步的结果发送和状态回调功能。
 */
@Service
public class StateEventCallbackService {

    private final Logger logger = LoggerFactory.getLogger(StateEventCallbackService.class);

    /**
     * 重试退避策略数组
     */
    private static final int[] RETRY_BACKOFF = {1, 2, 3, 5, 10, 20, 40, 100, 100, 100, 100, 200, 200, 200};

    /**
     * 远程通道缓存，按主机地址映射
     */
    private static final ConcurrentHashMap<String, NettyRemoteChannel> REMOTE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * Netty远程通信客户端
     */
    private final NettyRemotingClient nettyRemotingClient;

    public StateEventCallbackService() {
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
    }

    /**
     * 添加远程通道到缓存中。
     *
     * @param host host
     * @param channel channel
     */
    public void addRemoteChannel(String host, NettyRemoteChannel channel) {
        REMOTE_CHANNELS.put(host, channel);
    }

    /**
     * 获取或创建到指定主机的远程通道。优先从缓存获取，不存在或已关闭则创建新连接。
     *
     * @param host host
     * @return callback channel
     */
    private Optional<NettyRemoteChannel> newRemoteChannel(Host host) {
        Channel newChannel;
        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(host.getAddress());
        if (nettyRemoteChannel != null) {
            if (nettyRemoteChannel.isActive()) {
                return Optional.of(nettyRemoteChannel);
            }
        }
        newChannel = nettyRemotingClient.getChannel(host);
        if (newChannel != null) {
            return Optional.of(newRemoteChannel(newChannel, host.getAddress()));
        }
        return Optional.empty();
    }

    /**
     * 根据重试次数计算退避暂停时间。
     *
     * @param ntries 重试次数
     * @return 暂停时间（毫秒）
     */
    public long pause(int ntries) {
        return SLEEP_TIME_MILLIS * RETRY_BACKOFF[ntries % RETRY_BACKOFF.length];
    }

    private NettyRemoteChannel newRemoteChannel(Channel newChannel, long opaque, String host) {
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel, opaque);
        addRemoteChannel(host, remoteChannel);
        return remoteChannel;
    }

    private NettyRemoteChannel newRemoteChannel(Channel newChannel, String host) {
        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel);
        addRemoteChannel(host, remoteChannel);
        return remoteChannel;
    }

    /**
     * 移除指定主机的远程通道。
     *
     * @param host host
     */
    public void remove(String host) {
        REMOTE_CHANNELS.remove(host);
    }

    /**
     * 发送结果命令到目标主机。此方法不保证发送成功，为异步发送。
     *
     * @param host    target host
     * @param command command need to send
     */
    public void sendResult(Host host, Command command) {
        logger.info("send result, host:{}, command:{}", host.getAddress(), command.toString());
        newRemoteChannel(host).ifPresent(nettyRemoteChannel -> {
            nettyRemoteChannel.writeAndFlush(command);
        });
    }

    /**
     * 同步发送命令并返回响应。发送完成后关闭连接通道。
     *
     * @param host host
     * @param requestCommand requestCommand
     * @return 响应命令，失败返回null
     */
    public Command sendSync(Host host, Command requestCommand) {
        try {
            return this.nettyRemotingClient.sendSync(host, requestCommand, HTTP_CONNECTION_REQUEST_TIMEOUT);
        } catch (InterruptedException e) {
            logger.error("send sync fail, host:{}, command:{}", host, requestCommand, e);
            Thread.currentThread().interrupt();
        } catch (RemotingException e) {
            logger.error("send sync fail, host:{}, command:{}", host, requestCommand, e);
        }
        finally {
            this.nettyRemotingClient.closeChannel(host);
        }
        return null;
    }
}
