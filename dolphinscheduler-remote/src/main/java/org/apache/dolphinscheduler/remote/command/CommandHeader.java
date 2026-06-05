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

package org.apache.dolphinscheduler.remote.command;

import java.io.Serializable;

import lombok.Data;

/**
 * 命令头部信息。定义Netty远程通信协议的数据包头部结构，包含命令类型、请求唯一标识（opaque）、上下文长度与内容、消息体长度，用于解码器按协议逐字段解析。
 */
@Data
public class CommandHeader implements Serializable {

    /**
     * 命令类型
     */
    private byte type;

    /**
     * 请求唯一标识，用于关联请求与响应
     */
    private long opaque;

    /**
     * 上下文数据的字节长度
     */
    private int contextLength;

    /**
     * 上下文数据的字节数组
     */
    private byte[] context;

    /**
     * 消息体的字节长度
     */
    private int bodyLength;

}
