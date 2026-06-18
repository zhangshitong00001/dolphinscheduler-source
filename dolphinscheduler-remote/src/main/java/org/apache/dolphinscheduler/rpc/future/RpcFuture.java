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

package org.apache.dolphinscheduler.rpc.future;

import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * RPC 异步调用 Future。基于 CountDownLatch 实现的 Future 模式，
 * 调用方通过 get() 方法阻塞等待 RPC 响应，当响应到达时由 Netty 回调线程调用 done() 唤醒等待。
 */
public class RpcFuture implements Future<Object> {

    /** 同步锁存器，初始计数为1 */
    private CountDownLatch latch = new CountDownLatch(1);

    /** RPC 响应结果 */
    private RpcResponse response;

    /** 对应的 RPC 请求 */
    private RpcRequest request;

    /** 请求ID */
    private long requestId;

    public RpcFuture(RpcRequest rpcRequest, long requestId) {
        this.request = rpcRequest;
        this.requestId = requestId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    /**
     * 阻塞等待 RPC 响应，默认超时时间为5秒。
     *
     * @return RPC 响应对象
     * @throws InterruptedException 等待被中断时抛出
     * @throws RuntimeException 等待超时时抛出
     */
    @Override
    public RpcResponse get() throws InterruptedException {
        // the timeout period should be defined by the business party
        boolean success = latch.await(5, TimeUnit.SECONDS);
        if (!success) {
            throw new RuntimeException("Timeout exception. Request id: " + this.requestId
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
        return response;
    }

    /**
     * 阻塞等待 RPC 响应，可指定超时时间和时间单位。
     *
     * @param timeout 超时时间值
     * @param unit 时间单位
     * @return RPC 响应对象
     * @throws InterruptedException 等待被中断时抛出
     * @throws RuntimeException 等待超时时抛出
     */
    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        boolean success = latch.await(timeout, unit);
        if (!success) {
            throw new RuntimeException("Timeout exception. Request id: " + requestId
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
        return response;
    }

    /**
     * 设置 RPC 响应并释放等待线程。由 Netty 回调线程在响应到达时调用。
     *
     * @param response RPC 响应对象
     */
    public void done(RpcResponse response) {
        this.response = response;
        latch.countDown();
    }
}
