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

package org.apache.dolphinscheduler.remote;

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
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.dolphinscheduler.remote.codec.NettyDecoder;
import org.apache.dolphinscheduler.remote.codec.NettyEncoder;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.exceptions.RemotingTimeoutException;
import org.apache.dolphinscheduler.remote.exceptions.RemotingTooMuchRequestException;
import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ReleaseSemaphore;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.handler.NettyClientHandler;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.CallerThreadExecutePolicy;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.remote.utils.NettyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty远程通信客户端。提供同步/异步消息发送、通道管理和命令处理器注册功能。
 */
public class NettyRemotingClient implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(NettyRemotingClient.class);

    /**
     * Netty客户端引导程序
     */
    private final Bootstrap bootstrap = new Bootstrap();

    /**
     * Netty编码器
     */
    private final NettyEncoder encoder = new NettyEncoder();

    /**
     * 主机到通道的映射缓存
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap<>(128);

    /**
     * 客户端是否已启动标志
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * 工作线程组
     */
    private final EventLoopGroup workerGroup;

    /**
     * 客户端配置
     */
    private final NettyClientConfig clientConfig;

    /**
     * 异步信号量，用于并发控制
     */
    private final Semaphore asyncSemaphore = new Semaphore(200, true);

    /**
     * 回调线程执行器
     */
    private final ExecutorService callbackExecutor;

    /**
     * 客户端通道处理器
     */
    private final NettyClientHandler clientHandler;

    /**
     * 响应Future定时清理执行器
     */
    private final ScheduledExecutorService responseFutureExecutor;

    public NettyRemotingClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        if (Epoll.isAvailable()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), new NamedThreadFactory("NettyClient"));
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new NamedThreadFactory("NettyClient"));
        }
        this.callbackExecutor = new ThreadPoolExecutor(
                Constants.CPUS,
                Constants.CPUS,
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(1000),
                new NamedThreadFactory("CallbackExecutor"),
                new CallerThreadExecutePolicy());
        this.clientHandler = new NettyClientHandler(this, callbackExecutor);

        this.responseFutureExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ResponseFutureExecutor"));

        this.start();
    }

    private void start() {

        this.bootstrap
                .group(this.workerGroup)
                .channel(NettyUtils.getSocketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast("client-idle-handler", new IdleStateHandler(Constants.NETTY_CLIENT_HEART_BEAT_TIME, 0, 0, TimeUnit.MILLISECONDS))
                                .addLast(new NettyDecoder(), clientHandler, encoder);
                    }
                });
        this.responseFutureExecutor.scheduleAtFixedRate(ResponseFuture::scanFutureTable, 5000, 1000, TimeUnit.MILLISECONDS);
        isStarted.compareAndSet(false, true);
    }

    /**
     * 异步发送命令到指定主机。通过信号量控制并发数，超时或失败时通过回调通知调用方。
     *
     * @param host host
     * @param command command
     * @param timeoutMillis timeoutMillis
     * @param invokeCallback callback function
     */
    public void sendAsync(final Host host, final Command command,
                          final long timeoutMillis,
                          final InvokeCallback invokeCallback) throws InterruptedException, RemotingException {
        final Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException("network error");
        }
        /*
         * request unique identification
         */
        final long opaque = command.getOpaque();
        /*
         *  control concurrency number
         */
        boolean acquired = this.asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final ReleaseSemaphore releaseSemaphore = new ReleaseSemaphore(this.asyncSemaphore);

            /*
             *  response future
             */
            final ResponseFuture responseFuture = new ResponseFuture(opaque,
                    timeoutMillis,
                    invokeCallback,
                    releaseSemaphore);
            try {
                channel.writeAndFlush(command).addListener(future -> {
                    if (future.isSuccess()) {
                        responseFuture.setSendOk(true);
                        return;
                    } else {
                        responseFuture.setSendOk(false);
                    }
                    responseFuture.setCause(future.cause());
                    responseFuture.putResponse(null);
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Exception ex) {
                        logger.error("execute callback error", ex);
                    } finally {
                        responseFuture.release();
                    }
                });
            } catch (Exception ex) {
                responseFuture.release();
                throw new RemotingException(String.format("send command to host: %s failed", host), ex);
            }
        } else {
            String message = String.format("try to acquire async semaphore timeout: %d, waiting thread num: %d, total permits: %d",
                    timeoutMillis, asyncSemaphore.getQueueLength(), asyncSemaphore.availablePermits());
            throw new RemotingTooMuchRequestException(message);
        }
    }

    /**
     * 同步发送命令到指定主机，阻塞等待响应返回。
     *
     * @param host host
     * @param command command
     * @param timeoutMillis timeoutMillis
     * @return command 响应命令
     */
    public Command sendSync(final Host host, final Command command, final long timeoutMillis) throws InterruptedException, RemotingException {
        final Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException(String.format("connect to : %s fail", host));
        }
        final long opaque = command.getOpaque();
        final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
        channel.writeAndFlush(command).addListener(future -> {
            if (future.isSuccess()) {
                responseFuture.setSendOk(true);
                return;
            } else {
                responseFuture.setSendOk(false);
            }
            responseFuture.setCause(future.cause());
            responseFuture.putResponse(null);
            logger.error("send command {} to host {} failed", command, host);
        });
        /*
         * sync wait for result
         */
        Command result = responseFuture.waitResponse();
        if (result == null) {
            if (responseFuture.isSendOK()) {
                throw new RemotingTimeoutException(host.toString(), timeoutMillis, responseFuture.getCause());
            } else {
                throw new RemotingException(host.toString(), responseFuture.getCause());
            }
        }
        return result;
    }

    /**
     * 发送命令到指定主机，不等待响应。用于单向通知场景。
     *
     * @param host host
     * @param command command
     */
    public void send(final Host host, final Command command) throws RemotingException {
        Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException(String.format("connect to : %s fail", host));
        }
        try {
            ChannelFuture future = channel.writeAndFlush(command).await();
            if (future.isSuccess()) {
                logger.debug("send command : {} , to : {} successfully.", command, host.getAddress());
            } else {
                String msg = String.format("send command : %s , to :%s failed", command, host.getAddress());
                logger.error(msg, future.cause());
                throw new RemotingException(msg);
            }
        } catch (RemotingException remotingException) {
            throw remotingException;
        } catch (Exception e) {
            logger.error("Send command {} to address {} encounter error.", command, host.getAddress());
            throw new RemotingException(String.format("Send command : %s , to :%s encounter error", command, host.getAddress()), e);
        }
    }

    /**
     * 注册命令处理器，使用默认线程执行器。
     *
     * @param commandType command type
     * @param processor processor
     */
    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor) {
        this.registerProcessor(commandType, processor, null);
    }

    /**
     * 注册命令处理器，指定线程执行器。
     *
     * @param commandType command type
     * @param processor processor
     * @param executor thread executor
     */
    public void registerProcessor(final CommandType commandType, final NettyRequestProcessor processor, final ExecutorService executor) {
        this.clientHandler.registerProcessor(commandType, processor, executor);
    }

    /**
     * 获取指定主机的活跃通道，若不存在或已关闭则创建新通道。
     *
     * @param host host
     * @return channel
     */
    public Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        return createChannel(host, true);
    }

    /**
     * 创建到指定主机的Netty通道。支持同步等待连接完成。
     *
     * @param host host
     * @param isSync sync flag
     * @return channel 创建的通道，失败返回null
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

    @Override
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
                if (callbackExecutor != null) {
                    this.callbackExecutor.shutdownNow();
                }
                if (this.responseFutureExecutor != null) {
                    this.responseFutureExecutor.shutdownNow();
                }
                logger.info("netty client closed");
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
        }
    }

    /**
     * 关闭所有已建立的通道并清空通道缓存。
     */
    private void closeChannels() {
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }

    /**
     * 关闭指定主机的通道并从缓存中移除。
     *
     * @param host host
     */
    public void closeChannel(Host host) {
        Channel channel = this.channels.remove(host);
        if (channel != null) {
            channel.close();
        }
    }
}
