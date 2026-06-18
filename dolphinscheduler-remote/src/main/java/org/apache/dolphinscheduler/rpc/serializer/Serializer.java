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

package org.apache.dolphinscheduler.rpc.serializer;

import java.io.IOException;

/**
 * RPC 序列化器接口。定义 RPC 协议中对象的序列化与反序列化契约。
 * 实现类需提供具体的二进制编解码方案（如 ProtoStuff、Kryo、Hessian 等）。
 */
public interface Serializer {

    /**
     * 将对象序列化为字节数组。
     *
     * @param obj 待序列化的对象
     * @param <T> 对象类型
     * @return 序列化后的字节数组
     * @throws IOException 序列化过程中的 I/O 异常
     */
    <T> byte[] serialize(T obj) throws IOException;

    /**
     * 将字节数组反序列化为指定类型的对象。
     *
     * @param data 待反序列化的字节数组
     * @param clz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws IOException 反序列化过程中的 I/O 异常
     */
    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;

}
