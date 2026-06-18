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

package org.apache.dolphinscheduler.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.experimental.UtilityClass;

/**
 * 线程工具类，提供守护线程池创建和线程休眠的便捷方法。
 * 该类为工具类，使用Lombok @UtilityClass注解实现。
 */
@UtilityClass
public class ThreadUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    /**
     * 创建固定大小的守护线程池。
     *
     * @param threadName 线程名称前缀
     * @param threadsNum 线程数量
     * @return ExecutorService线程池
     */
    public static ExecutorService newDaemonFixedThreadExecutor(String threadName, int threadsNum) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName).build();
        return Executors.newFixedThreadPool(threadsNum, threadFactory);
    }

    /**
     * 使当前线程休眠指定毫秒数，不保证精确计时。
     *
     * @param millis 休眠毫秒数
     */
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            logger.error("Current thread sleep error", interruptedException);
        }
    }
}
