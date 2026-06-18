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

package org.apache.dolphinscheduler.remote.handler;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.remote.utils.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Netty客户端处理器。负责处理客户端与远程服务端的通信，包括通道管理、请求响应分发和心跳维持。
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * Netty远程客户端实例
     */
    private final NettyRemotingClient nettyRemotingClient;

    /**
     * 心跳数据
     */
    private static byte[] heartBeatData = "heart_beat".getBytes();

    /**
     * 回调线程执行器
     */
    private final ExecutorService callbackExecutor;

    /**
     * 处理器注册表，按命令类型映射到对应的处理器和执行器
     */
    private final ConcurrentHashMap<CommandType, Pair<NettyRequestProcessor, ExecutorService>> processors;

    /**
     * 默认线程执行器
     */
    private final ExecutorService defaultExecutor = Executors.newFixedThreadPool(Constants.CPUS);

    public NettyClientHandler(NettyRemotingClient nettyRemotingClient, ExecutorService callbackExecutor) {
        this.nettyRemotingClient = nettyRemotingClient;
        this.callbackExecutor = callbackExecutor;
        this.processors = new ConcurrentHashMap<>();
    }

    /**
     * 通道变为非活跃状态时的回调。关闭当前通道并清理相关资源。
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    /**
     * 通道读取数据时的回调。将接收到的命令分发给对应的处理器。
     *
     * @param ctx channel handler context
     * @param msg message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        processReceived(ctx.channel(), (Command) msg);
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
        ExecutorService executorRef = executor;
        if (executorRef == null) {
            executorRef = defaultExecutor;
        }
        this.processors.putIfAbsent(commandType, new Pair<>(processor, executorRef));
    }

    /**
     * 处理接收到的命令。优先匹配已注册的Future回调，否则按命令类型分发处理。
     *
     * @param channel channel
     * @param command command
     */
    private void processReceived(final Channel channel, final Command command) {
        ResponseFuture future = ResponseFuture.getFuture(command.getOpaque());
        if (future != null) {
            future.setResponseCommand(command);
            future.release();
            if (future.getInvokeCallback() != null) {
                future.removeFuture();
                this.callbackExecutor.submit(future::executeInvokeCallback);
            } else {
                future.putResponse(command);
            }
        } else {
            processByCommandType(channel, command);
        }
    }

    /**
     * 按命令类型分发处理。根据命令类型查找注册的处理器并在对应线程池中执行。
     *
     * @param channel channel
     * @param command command
     */
    public void processByCommandType(final Channel channel, final Command command) {
        final Pair<NettyRequestProcessor, ExecutorService> pair = processors.get(command.getType());
        if (pair != null) {
            Runnable run = () -> {
                try {
                    pair.getLeft().process(channel, command);
                } catch (Exception e) {
                    logger.error(String.format("process command %s exception", command), e);
                }
            };
            try {
                pair.getRight().submit(run);
            } catch (RejectedExecutionException e) {
                logger.warn("thread pool is full, discard command {} from {}", command, ChannelUtils.getRemoteAddress(channel));
            }
        } else {
            logger.warn("receive response {}, but not matched any request ", command);
        }
    }

    /**
     * 异常捕获处理。记录异常日志并关闭异常通道。
     *
     * @param ctx channel handler context
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        nettyRemotingClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Command heartBeat = new Command();
            heartBeat.setType(CommandType.HEART_BEAT);
            heartBeat.setBody(heartBeatData);
            ctx.channel().writeAndFlush(heartBeat)
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            if (logger.isDebugEnabled()) {
                logger.debug("Client send heart beat to: {}", ChannelUtils.getRemoteAddress(ctx.channel()));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
