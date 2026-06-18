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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * ProtoStuff 序列化器实现。基于 io.protostuff 库的高效二进制序列化方案，
 * 使用 RuntimeSchema 动态生成对象模式，无需预编译 IDL 文件。
 * 相比 Java 原生序列化，具有更高的性能和更小的数据体积。
 */
public class ProtoStuffSerializer implements Serializer {

    /** ProtoStuff 序列化缓冲区，线程安全通过 LinkedBuffer 的 clear() 复用 */
    private static LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    /** 运行时 Schema 缓存，避免重复创建 Schema */
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    /**
     * 获取或创建指定类型的 ProtoStuff Schema。
     *
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return ProtoStuff Schema 对象
     */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        return (Schema<T>) schemaCache.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    /**
     * 将对象序列化为字节数组。
     *
     * @param obj 待序列化的对象
     * @param <T> 对象类型
     * @return 序列化后的字节数组
     */
    @Override
    public <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Schema<T> schema = getSchema(clazz);
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return data;
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     *
     * @param data 待反序列化的字节数组
     * @param clz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象，若创建消息实例失败返回 null
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) {
        Schema<T> schema = getSchema(clz);
        T obj = schema.newMessage();
        if (null == obj) {
            return null;
        }
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}
