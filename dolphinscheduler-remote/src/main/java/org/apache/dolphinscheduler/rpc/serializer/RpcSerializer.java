package org.apache.dolphinscheduler.rpc.serializer;/*
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

import java.util.HashMap;

/**
 * RPC 序列化器注册枚举。定义 RPC 协议支持的序列化方式及其对应的 Serializer 实现。
 * 通过序列化类型字节码查找对应的序列化器实例，支持扩展多家序列化方案。
 */
public enum RpcSerializer {


    /** ProtoStuff 二进制序列化方案 */
    PROTOSTUFF((byte) 1, new ProtoStuffSerializer());

    /** 序列化类型标识字节 */
    byte type;

    /** 序列化器实例 */
    Serializer serializer;

    RpcSerializer(byte type, Serializer serializer) {
        this.type = type;
        this.serializer = serializer;
    }

    public byte getType() {
        return type;
    }

    /** 类型字节码到序列化器实例的映射表 */
    private static HashMap<Byte, Serializer> SERIALIZERS_MAP = new HashMap<>();

    static {
        for (RpcSerializer rpcSerializer : RpcSerializer.values()) {
            SERIALIZERS_MAP.put(rpcSerializer.type, rpcSerializer.serializer);
        }
    }

    /**
     * 根据序列化类型字节码获取对应的序列化器实例。
     *
     * @param type 序列化类型字节码
     * @return 对应的 Serializer 实例，若不存在则返回 null
     */
    public static Serializer getSerializerByType(byte type) {
        return SERIALIZERS_MAP.get(type);
    }
}
