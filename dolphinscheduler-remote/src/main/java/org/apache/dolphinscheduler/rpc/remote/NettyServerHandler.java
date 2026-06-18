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

import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.common.ThreadPoolManager;
import org.apache.dolphinscheduler.rpc.config.ServiceBean;
import org.apache.dolphinscheduler.rpc.protocol.EventType;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Netty RPC 服务器端处理器。处理 RPC 请求的读取、服务反射调用、心跳事件和连接管理。
 * 接收到业务请求后，通过 ServiceBean 查找目标服务类，反射调用方法并返回响应。
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    /** 线程池管理器，用于异步处理请求 */
    private static final ThreadPoolManager threadPoolManager = ThreadPoolManager.INSTANCE;

    /**
     * 通道断开时关闭连接。
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("channel close");
        ctx.channel().close();
    }

    /**
     * 通道激活时记录客户端连接日志。
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("client connect success !" + ctx.channel().remoteAddress());
    }

    /**
     * 读取 RPC 请求消息，心跳消息直接忽略，业务请求提交到线程池异步处理。
     *
     * @param ctx 通道处理器上下文
     * @param msg 接收到的 RpcProtocol 消息
     */
    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol<RpcRequest> rpcProtocol = (RpcProtocol<RpcRequest>) msg;
        if (rpcProtocol.getMsgHeader().getEventType() == EventType.HEARTBEAT.getType()) {
            logger.info("heart beat");
            return;
        }
        threadPoolManager.addExecuteTask(() -> readHandler(ctx, rpcProtocol));
    }

    /**
     * 处理 RPC 请求：通过 ServiceBean 查找服务实现类，反射调用目标方法，封装响应并写回客户端。
     *
     * @param ctx 通道处理器上下文
     * @param protocol RPC 协议消息
     */
    private void readHandler(ChannelHandlerContext ctx, RpcProtocol protocol) {
        RpcRequest req = (RpcRequest) protocol.getBody();
        RpcResponse response = new RpcResponse();

        response.setStatus((byte) 0);

        String classname = req.getClassName();

        String methodName = req.getMethodName();

        Class<?>[] parameterTypes = req.getParameterTypes();

        Object[] arguments = req.getParameters();
        Object result = null;
        try {
            Class serviceClass = ServiceBean.getServiceClass(classname);

            Object object = serviceClass.newInstance();

            Method method = serviceClass.getMethod(methodName, parameterTypes);

            result = method.invoke(object, arguments);
        } catch (Exception e) {
            logger.error("netty server execute error,service name :{} method name :{} ", classname + methodName, e);
            response.setStatus((byte) -1);
        }

        response.setResult(result);
        protocol.setBody(response);
        protocol.getMsgHeader().setEventType(EventType.RESPONSE.getType());
        ctx.writeAndFlush(protocol);
    }

    /**
     * 用户自定义事件触发处理，空闲状态事件仅记录日志。
     *
     * @param ctx 通道处理器上下文
     * @param evt 用户事件
     * @throws Exception 处理异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            logger.debug("IdleStateEvent triggered, send heartbeat to channel " + ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常捕获处理，记录错误日志并关闭连接。
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
