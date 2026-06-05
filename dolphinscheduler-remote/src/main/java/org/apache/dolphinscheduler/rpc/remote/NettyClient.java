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

package org.apache.dolphinscheduler.rpc.remote;

import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.NettyUtils;
import org.apache.dolphinscheduler.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.rpc.codec.NettyDecoder;
import org.apache.dolphinscheduler.rpc.codec.NettyEncoder;
import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.future.RpcFuture;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Netty RPC 客户端。基于 Netty Bootstrap 实现的 RPC 客户端，支持连接复用和同步/异步调用。
 * 内部维护 Host 到 Channel 的映射以复用连接，并通过静态内部类实现线程安全的单例模式。
 */
public class NettyClient {

    /**
     * 获取 NettyClient 单例实例。
     *
     * @return NettyClient 实例
     */
    public static NettyClient getInstance() {
        return NettyClient.NettyClientInner.INSTANCE;
    }

    /** 静态内部类实现的单例持有者 */
    private static class NettyClientInner {

        private static final NettyClient INSTANCE = new NettyClient(new NettyClientConfig());
    }

    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /** Netty 工作线程组 */
    private final EventLoopGroup workerGroup;

    /** 客户端配置 */
    private final NettyClientConfig clientConfig;


    /** Netty Bootstrap 启动器 */
    private final Bootstrap bootstrap = new Bootstrap();

    /** 客户端启动状态标志 */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /** Host 到 Channel 的连接池映射，用于连接复用 */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap<>(128);

    /**
     * 获取指定主机的通道，若不存在或已断开则自动创建。
     *
     * @param host 目标主机
     * @return Netty Channel，创建失败返回 null
     */
    private Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        return createChannel(host, true);
    }

    /**
     * 创建到指定主机的 Netty 连接通道。
     *
     * @param host 目标主机
     * @param isSync 是否同步等待连接完成
     * @return 创建的 Channel，失败返回 null
     */
    public Channel createChannel(Host host, boolean isSync) {
        ChannelFuture future;
        try {
            synchronized (bootstrap) {
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if (isSync) {
                future.sync();
            }
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channels.put(host, channel);
                return channel;
            }
        } catch (Exception ex) {
            logger.warn(String.format("connect to %s error", host), ex);
        }
        return null;
    }

    /**
     * NettyClient 构造函数，初始化 EventLoopGroup 并启动客户端。
     *
     * @param clientConfig 客户端配置
     */
    private NettyClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        if (Epoll.isAvailable()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
        this.start();

    }

    /**
     * 启动 Netty 客户端，配置通道选项和处理器链（编码器、解码器、空闲检测、业务处理器）。
     */
    private void start() {

        this.bootstrap
                .group(this.workerGroup)
                .channel(NettyUtils.getSocketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new NettyEncoder())
                                .addLast(new NettyDecoder(RpcResponse.class))
                                .addLast("client-idle-handler", new IdleStateHandler(Constants.NETTY_CLIENT_HEART_BEAT_TIME, 0, 0, TimeUnit.MILLISECONDS))
                                .addLast(new NettyClientHandler());
                    }
                });

        isStarted.compareAndSet(false, true);
    }

    /**
     * 向指定主机发送 RPC 请求消息，支持同步和异步模式。
     *
     * @param host 目标主机
     * @param protocol RPC 协议消息
     * @param async 是否异步发送
     * @return RPC 响应结果，异步模式直接返回空响应
     */
    public RpcResponse sendMsg(Host host, RpcProtocol<RpcRequest> protocol, Boolean async) {

        Channel channel = getChannel(host);
        assert channel != null;
        RpcRequest request = protocol.getBody();
        RpcRequestCache rpcRequestCache = new RpcRequestCache();
        String serviceName = request.getClassName() + request.getMethodName();
        rpcRequestCache.setServiceName(serviceName);
        long reqId = protocol.getMsgHeader().getRequestId();
        RpcFuture future = null;
        if (Boolean.FALSE.equals(async)) {
            future = new RpcFuture(request, reqId);
            rpcRequestCache.setRpcFuture(future);
        }
        RpcRequestTable.put(protocol.getMsgHeader().getRequestId(), rpcRequestCache);
        channel.writeAndFlush(protocol);
        RpcResponse result = null;
        if (Boolean.TRUE.equals(async)) {
            result = new RpcResponse();
            result.setStatus((byte) 0);
            result.setResult(true);
            return result;
        }
        try {
            assert future != null;
            result = future.get();
        } catch (InterruptedException e) {
            logger.error("send msg error，service name is {}", serviceName, e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * 关闭 Netty 客户端，关闭所有连接通道并优雅关闭工作线程组。
     */
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
            logger.info("netty client closed");
        }
    }

    /**
     * 关闭所有已建立的连接通道并清空连接池。
     */
    private void closeChannels() {
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }
}
