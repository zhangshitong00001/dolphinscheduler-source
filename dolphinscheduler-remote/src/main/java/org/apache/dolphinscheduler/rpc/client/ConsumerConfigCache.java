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

package org.apache.dolphinscheduler.rpc.client;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费者配置缓存。按服务名称缓存RPC消费者配置，在首次调用时初始化并在后续调用中复用。
 */
public class ConsumerConfigCache {

    private ConsumerConfigCache() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 消费者配置映射表，按服务名称索引
     */
    private static ConcurrentHashMap<String, ConsumerConfig> consumerMap = new ConcurrentHashMap<>();

    /**
     * 根据服务名称获取缓存的消费者配置。
     *
     * @param serviceName serviceName
     * @return ConsumerConfig
     */
    public static ConsumerConfig getConfigByServersName(String serviceName) {
        return consumerMap.get(serviceName);
    }

    /**
     * 将消费者配置存入缓存，若已存在则不覆盖。
     *
     * @param serviceName serviceName
     * @param consumerConfig consumerConfig
     */
    static void putConfig(String serviceName, ConsumerConfig consumerConfig) {
        consumerMap.putIfAbsent(serviceName, consumerConfig);
    }
}
