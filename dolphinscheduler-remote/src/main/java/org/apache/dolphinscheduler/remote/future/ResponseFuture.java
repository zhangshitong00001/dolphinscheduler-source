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

package org.apache.dolphinscheduler.remote.future;

import org.apache.dolphinscheduler.remote.command.Command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 响应Future。封装异步请求的响应处理，支持超时等待、回调执行和Future表扫描清理。
 */
public class ResponseFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFuture.class);

    /**
     * 全局Future映射表，通过opaque标识关联请求与响应
     */
    private static final ConcurrentHashMap<Long, ResponseFuture> FUTURE_TABLE = new ConcurrentHashMap<>(256);

    /**
     * 请求唯一标识
     */
    private final long opaque;

    /**
     * 超时时间（毫秒）
     */
    private final long timeoutMillis;

    /**
     * 回调函数
     */
    private final InvokeCallback invokeCallback;

    /**
     * 信号量释放器
     */
    private final ReleaseSemaphore releaseSemaphore;

    /**
     * 闭锁，用于同步等待响应
     */
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * 开始时间戳
     */
    private final long beginTimestamp = System.currentTimeMillis();

    /**
     * 响应命令
     */
    private Command responseCommand;

    /**
     * 发送是否成功标志
     */
    private volatile boolean sendOk = true;

    /**
     * 异常原因
     */
    private Throwable cause;

    public ResponseFuture(long opaque, long timeoutMillis, InvokeCallback invokeCallback, ReleaseSemaphore releaseSemaphore) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.releaseSemaphore = releaseSemaphore;
        FUTURE_TABLE.put(opaque, this);
    }

    /**
     * 等待响应返回，在超时时间内阻塞当前线程。
     *
     * @return command 响应命令，超时返回null
     */
    public Command waitResponse() throws InterruptedException {
        this.latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    /**
     * 设置响应命令并释放闭锁，唤醒等待线程，同时从Future映射表中移除当前Future。
     *
     * @param responseCommand 响应命令
     */
    public void putResponse(final Command responseCommand) {
        this.responseCommand = responseCommand;
        this.latch.countDown();
        FUTURE_TABLE.remove(opaque);
    }

    public static ResponseFuture getFuture(long opaque) {
        return FUTURE_TABLE.get(opaque);
    }

    /**
     * 从Future映射表中移除当前Future。
     */
    public void removeFuture() {
        FUTURE_TABLE.remove(opaque);
    }

    /**
     * 判断是否已超时。
     *
     * @return 是否超时
     */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    /**
     * 执行回调函数，通知调用方响应已完成。
     */
    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public boolean isSendOK() {
        return sendOk;
    }

    public void setSendOk(boolean sendOk) {
        this.sendOk = sendOk;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public long getOpaque() {
        return opaque;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public Command getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(Command responseCommand) {
        this.responseCommand = responseCommand;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    /**
     * 释放信号量，归还并发控制许可。
     */
    public void release() {
        if (this.releaseSemaphore != null) {
            this.releaseSemaphore.release();
        }
    }

    /**
     * 扫描Future映射表，清理超时的Future并执行其回调。
     */
    public static void scanFutureTable() {
        final List<ResponseFuture> futureList = new LinkedList<>();
        Iterator<Map.Entry<Long, ResponseFuture>> it = FUTURE_TABLE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ResponseFuture> next = it.next();
            ResponseFuture future = next.getValue();
            if ((future.getBeginTimestamp() + future.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                futureList.add(future);
                it.remove();
                LOGGER.warn("remove timeout request : {}", future);
            }
        }
        for (ResponseFuture future : futureList) {
            try {
                future.release();
                future.executeInvokeCallback();
            } catch (Exception ex) {
                LOGGER.warn("scanFutureTable, execute callback error", ex);
            }
        }
    }

    @Override
    public String toString() {
        return "ResponseFuture{"
                + "opaque=" + opaque
                + ", timeoutMillis=" + timeoutMillis
                + ", invokeCallback=" + invokeCallback
                + ", releaseSemaphore=" + releaseSemaphore
                + ", latch=" + latch
                + ", beginTimestamp=" + beginTimestamp
                + ", responseCommand=" + responseCommand
                + ", sendOk=" + sendOk
                + ", cause=" + cause
                + '}';
    }
}
