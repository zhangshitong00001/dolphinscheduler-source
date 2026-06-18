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

package org.apache.dolphinscheduler.rpc.protocol;

/**
 * RPC 协议消息头。定义 DolphinScheduler RPC 协议的固定头部格式，
 * 包含魔数、版本、事件类型、序列化方式、请求ID和消息体长度等字段，共计17字节。
 */
public class MessageHeader {

    /** 协议版本号，默认为1 */
    private byte version = 1;

    /** 事件类型（心跳/请求/响应） */
    private byte eventType;

    /** 消息体长度，默认为0 */
    private int msgLength = 0;

    /** 请求ID，用于关联请求与响应 */
    private long requestId = 0L;

    /** 序列化方式标识 */
    private byte serialization = 0;

    /** 协议魔数，用于校验数据包有效性 */
    private short magic = RpcProtocolConstants.MAGIC;

    public short getMagic() {
        return magic;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getEventType() {
        return eventType;
    }

    public void setEventType(byte eventType) {
        this.eventType = eventType;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public byte getSerialization() {
        return serialization;
    }

    public void setSerialization(byte serialization) {
        this.serialization = serialization;
    }
}
