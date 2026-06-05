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

package org.apache.dolphinscheduler.rpc.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

/**
 * RPC 线程池管理器。基于枚举单例模式，为 RPC 模块提供统一的线程池用于异步任务处理。
 * 核心线程数为 CPU 核数 x2，最大线程数为 CPU 核数 x4，空闲保活时间为60秒，队列容量为200。
 */
public enum ThreadPoolManager {

    /** 单例实例 */
    INSTANCE;

    /** 线程池执行器 */
    ExecutorService executorService;

    /** 工作队列大小 */
    private static final int WORK_QUEUE_SIZE = 200;
    /** 线程空闲保活时间（秒） */
    private static final long KEEP_ALIVE_TIME = 60;

    ThreadPoolManager() {
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 4, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(WORK_QUEUE_SIZE),
                new DiscardPolicy());
    }

    /**
     * 提交任务到线程池执行。
     *
     * @param task 待执行的任务
     */
    public void addExecuteTask(Runnable task) {
        executorService.submit(task);
    }
}
