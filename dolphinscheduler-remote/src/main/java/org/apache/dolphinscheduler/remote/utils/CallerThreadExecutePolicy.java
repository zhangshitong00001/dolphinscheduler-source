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

package org.apache.dolphinscheduler.remote.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 调用者线程执行拒绝策略。当线程池队列已满时，任务将由提交任务的调用者线程直接执行，提供自然的背压机制。
 */
public class CallerThreadExecutePolicy implements RejectedExecutionHandler {

    private final Logger logger = LoggerFactory.getLogger(CallerThreadExecutePolicy.class);

    /**
     * 当线程池拒绝执行任务时，由调用者线程直接同步执行该任务。
     *
     * @param r runnable
     * @param executor executor
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.warn("queue is full, trigger caller thread execute");
        r.run();
    }
}
