package org.apache.dolphinscheduler.rpc.common;/*
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

/**
 * RPC 响应事件类型枚举。定义 RPC 响应消息的分类，包括确认应答和业务响应两种类型。
 */
public enum ResponseEventType {

    /** 确认应答 */
    ACK((byte) 1, "ack"),
    /** 业务响应 */
    BUSINESS_RSP((byte) 2, "business response");

    /** 事件类型字节码 */
    private Byte type;

    /** 事件类型描述 */
    private String description;

    ResponseEventType(Byte type, String description) {
        this.type = type;
        this.description = description;
    }

    public Byte getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

}
