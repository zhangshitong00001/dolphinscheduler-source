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

package org.apache.dolphinscheduler.rpc.codec;

import org.apache.dolphinscheduler.rpc.protocol.EventType;
import org.apache.dolphinscheduler.rpc.protocol.MessageHeader;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocolConstants;
import org.apache.dolphinscheduler.rpc.serializer.RpcSerializer;
import org.apache.dolphinscheduler.rpc.serializer.Serializer;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Netty RPC 解码器。将字节流按照 DolphinScheduler RPC 协议格式解码为 RpcProtocol 对象。
 * 协议头部17字节：magic(2) + eventType(1) + version(1) + serialization(1) + requestId(8) + dataLength(4)。
 */
public class NettyDecoder extends ByteToMessageDecoder {

    /** 反序列化目标类 */
    private Class<?> genericClass;

    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * 解码字节流为 RpcProtocol 对象。
     *
     * @param channelHandlerContext Netty 通道处理器上下文
     * @param byteBuf 待解码的字节缓冲区
     * @param list 解码后的对象列表，解码结果会添加到此列表中
     * @throws Exception 解码过程中可能发生的异常
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < RpcProtocolConstants.HEADER_LENGTH) {
            return;
        }

        byteBuf.markReaderIndex();

        short magic = byteBuf.readShort();

        if (RpcProtocolConstants.MAGIC != magic) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte eventType = byteBuf.readByte();
        byte version = byteBuf.readByte();
        byte serialization = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        int dataLength = byteBuf.readInt();
        byte[] data = new byte[dataLength];

        RpcProtocol rpcProtocol = new RpcProtocol();

        MessageHeader header = new MessageHeader();
        header.setVersion(version);
        header.setSerialization(serialization);
        header.setRequestId(requestId);
        header.setEventType(eventType);
        header.setMsgLength(dataLength);
        byteBuf.readBytes(data);
        rpcProtocol.setMsgHeader(header);
        if (eventType != EventType.HEARTBEAT.getType()) {
            Serializer serializer = RpcSerializer.getSerializerByType(serialization);
            Object obj = serializer.deserialize(data, genericClass);
            rpcProtocol.setBody(obj);
        }
        list.add(rpcProtocol);
    }

}
