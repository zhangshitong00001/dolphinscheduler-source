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
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC请求追踪表。维护请求ID生成器与请求缓存映射，用于在客户端追踪每个RPC调用的状态和响应。
 */
public class RpcRequestTable {

    private RpcRequestTable() {
        throw new IllegalStateException("Utility class");
    }

    /** 请求ID自增生成器 */
    private static AtomicLong requestIdGen = new AtomicLong(0);

    /** 请求ID到请求缓存的映射表 */
    private static ConcurrentHashMap<Long, RpcRequestCache> requestMap = new ConcurrentHashMap<>();

    /**
     * 将RPC请求缓存放入追踪表。
     *
     * @param requestId 请求ID
     * @param rpcRequestCache RPC请求缓存对象
     */
    public static void put(long requestId, RpcRequestCache rpcRequestCache) {
        requestMap.put(requestId, rpcRequestCache);
    }

    /**
     * 根据请求ID获取RPC请求缓存。
     *
     * @param requestId 请求ID
     * @return RPC请求缓存对象，若不存在则返回null
     */
    public static RpcRequestCache get(Long requestId) {
        return requestMap.get(requestId);
    }

    /**
     * 根据请求ID移除RPC请求缓存。
     *
     * @param requestId 请求ID
     */
    public static void remove(Long requestId) {
        requestMap.remove(requestId);
    }

    /**
     * 生成并返回一个自增的请求ID。
     *
     * @return 新的请求ID
     */
    public static long getRequestId() {
        return requestIdGen.incrementAndGet();
    }

}
