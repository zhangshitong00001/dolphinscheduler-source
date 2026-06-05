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

import org.apache.dolphinscheduler.rpc.client.ConsumerConfig;
import org.apache.dolphinscheduler.rpc.client.ConsumerConfigCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.common.ThreadPoolManager;
import org.apache.dolphinscheduler.rpc.future.RpcFuture;
import org.apache.dolphinscheduler.rpc.protocol.EventType;
import org.apache.dolphinscheduler.rpc.protocol.MessageHeader;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Netty RPC 客户端处理器。处理 RPC 响应读取、心跳发送和通道事件。
 * 支持同步和异步两种调用模式，异步模式下可选回调处理。
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {


    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /** 线程池管理器，用于异步处理响应 */
    private static final ThreadPoolManager threadPoolManager = ThreadPoolManager.INSTANCE;

    /**
     * 通道断开时关闭连接。
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    /**
     * 读取 RPC 响应消息，从请求表中查找对应的请求缓存并异步处理。
     *
     * @param ctx 通道处理器上下文
     * @param msg 接收到的 RpcProtocol 消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;

        RpcResponse rsp = (RpcResponse) rpcProtocol.getBody();
        long reqId = rpcProtocol.getMsgHeader().getRequestId();
        RpcRequestCache rpcRequest = RpcRequestTable.get(reqId);

        if (null == rpcRequest) {
            logger.warn("rpc read error,this request does not exist");
            return;
        }
        threadPoolManager.addExecuteTask(() -> readHandler(rsp, rpcRequest, reqId));
    }

    /**
     * 处理 RPC 响应。同步模式下唤醒 Future；异步且配置回调时触发回调。
     *
     * @param rsp RPC 响应对象
     * @param rpcRequest 请求缓存
     * @param reqId 请求ID
     */
    private void readHandler(RpcResponse rsp, RpcRequestCache rpcRequest, long reqId) {
        String serviceName = rpcRequest.getServiceName();
        ConsumerConfig consumerConfig = ConsumerConfigCache.getConfigByServersName(serviceName);
        if (Boolean.FALSE.equals(consumerConfig.getAsync())) {
            RpcFuture future = rpcRequest.getRpcFuture();
            RpcRequestTable.remove(reqId);
            future.done(rsp);
            return;
        }

        if (Boolean.FALSE.equals(consumerConfig.getCallBack())) {
            return;
        }

        if (rsp.getStatus() == 0) {

            try {
                consumerConfig.getServiceCallBackClass().getDeclaredConstructor().newInstance().run(rsp.getResult());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                logger.error("rpc service call back error,serviceName {},rsp {}", serviceName, rsp);
            }
        } else {
            logger.error("rpc response error ,serviceName {},rsp {}", serviceName, rsp);
        }

    }

    /**
     * 用户事件触发处理。空闲事件时发送心跳消息。
     *
     * @param ctx 通道处理器上下文
     * @param evt 触发的用户事件
     * @throws Exception 处理异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            RpcProtocol rpcProtocol = new RpcProtocol();
            MessageHeader messageHeader = new MessageHeader();
            messageHeader.setEventType(EventType.HEARTBEAT.getType());
            rpcProtocol.setMsgHeader(messageHeader);
            ctx.channel().writeAndFlush(rpcProtocol);
            logger.debug("send heart beat msg...");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常捕获处理，记录日志并关闭连接。
     *
     * @param ctx 通道处理器上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }

}
