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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.constants.Constants;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;

/**
 * 重试工具类，基于Guava-Retryer提供可配置的重试机制。
 * 支持异常重试和结果检查重试两种模式，默认重试3次，每次间隔1秒。
 * 该类为工具类，不可实例化。
 */
public class RetryerUtils {
    private static Retryer<Boolean> defaultRetryerResultCheck;
    private static Retryer<Boolean> defaultRetryerResultNoCheck;

    private RetryerUtils() {
        throw new UnsupportedOperationException("Construct RetryerUtils");
    }

    private static Retryer<Boolean> getDefaultRetryerResultNoCheck() {
        if (defaultRetryerResultNoCheck == null) {
            defaultRetryerResultNoCheck = RetryerBuilder
                    .<Boolean>newBuilder()
                    .retryIfException()
                    .withWaitStrategy(WaitStrategies.fixedWait(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();
        }
        return defaultRetryerResultNoCheck;
    }

    /**
     * 获取默认的重试器。
     * 如果checkResult为true，重试器会在返回false时重试（最多3次，间隔1秒）；
     * 如果为false，重试器仅在抛出异常时重试。
     *
     * @param checkResult true表示需要检查返回结果为true才停止重试，false表示仅在异常时重试
     * @return 默认配置的重试器
     */
    public static Retryer<Boolean> getDefaultRetryer(boolean checkResult) {
        return checkResult ? getDefaultRetryer() : getDefaultRetryerResultNoCheck();
    }

    /**
     * 获取默认的重试器（带结果检查）。
     * 重试器在返回false或抛出异常时进行重试，最多3次，每次间隔1秒。
     *
     * @return 默认配置的重试器
     */
    public static Retryer<Boolean> getDefaultRetryer() {
        if (defaultRetryerResultCheck == null) {
            defaultRetryerResultCheck = RetryerBuilder
                    .<Boolean>newBuilder()
                    .retryIfResult(Boolean.FALSE::equals)
                    .retryIfException()
                    .withWaitStrategy(WaitStrategies.fixedWait(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();
        }
        return defaultRetryerResultCheck;
    }

    /**
     * 使用重试器执行Callable任务，支持自定义是否检查返回结果。
     *
     * @param callable 要执行的任务
     * @param checkResult true表示检查返回结果（结果为false时重试），false表示仅在异常时重试
     * @return 任务的最终执行结果
     * @throws ExecutionException 执行异常
     * @throws RetryException 重试异常
     */
    public static Boolean retryCall(final Callable<Boolean> callable, boolean checkResult) throws ExecutionException, RetryException {
        return getDefaultRetryer(checkResult).call(callable);
    }

    /**
     * 使用带结果检查的重试器执行Callable任务，直到返回true为止。
     *
     * @param callable 要执行的任务
     * @return 任务执行结果
     * @throws ExecutionException 执行异常
     * @throws RetryException 重试异常
     */
    public static Boolean retryCall(final Callable<Boolean> callable) throws ExecutionException, RetryException {
        return retryCall(callable, true);
    }
}
