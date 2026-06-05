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

import org.apache.dolphinscheduler.rpc.future.RpcFuture;

/**
 * RPC请求缓存。关联RPC Future与服务名称，用于追踪和匹配异步RPC请求的响应。
 */
public class RpcRequestCache {

    /**
     * RPC Future实例
     */
    private RpcFuture rpcFuture;

    /**
     * 服务名称
     */
    private String serviceName;

    public RpcFuture getRpcFuture() {
        return rpcFuture;
    }

    public void setRpcFuture(RpcFuture rpcFuture) {
        this.rpcFuture = rpcFuture;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
