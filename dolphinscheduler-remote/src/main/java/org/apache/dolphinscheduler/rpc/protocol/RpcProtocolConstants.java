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
 * RPC 协议常量定义。定义协议头部的固定长度和协议魔数。
 * 头部格式：magic(2字节) + eventType(1) + version(1) + serialization(1) + requestId(8) + dataLength(4) = 17字节。
 */
public class RpcProtocolConstants {

    public RpcProtocolConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** 协议头部固定长度：17字节 */
    public static final int HEADER_LENGTH = 17;

    /** 协议魔数：0xbabe，用于快速校验数据包格式 */
    public static final short MAGIC = (short) 0xbabe;

}
