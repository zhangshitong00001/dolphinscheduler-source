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

package org.apache.dolphinscheduler.server.master.metrics;

import com.facebook.presto.jdbc.internal.guava.collect.ImmutableSet;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 任务实例指标收集工具类。用于记录和暴露任务实例各状态的计数、任务分发成功/失败/错误次数以及待执行任务数量等指标。
 */
@UtilityClass
public class TaskMetrics {

    private final Map<String, Counter> taskInstanceCounters = new HashMap<>();

    private final Set<String> taskInstanceStates = ImmutableSet.of(
            "submit", "timeout", "finish", "failover", "retry", "dispatch", "success", "kill", "fail", "stop");

    static {
        for (final String state : taskInstanceStates) {
            taskInstanceCounters.put(
                    state,
                    Counter.builder("ds.task.instance.count")
                            .tags("state", state)
                            .description(String.format("Process instance %s total count", state))
                            .register(Metrics.globalRegistry));
        }

    }

    private final Counter taskDispatchCounter =
            Counter.builder("ds.task.dispatch.count")
                    .description("Task dispatch count")
                    .register(Metrics.globalRegistry);

    private final Counter taskDispatchFailCounter =
            Counter.builder("ds.task.dispatch.failure.count")
                    .description("Task dispatch failures count, retried ones included")
                    .register(Metrics.globalRegistry);

    private final Counter taskDispatchErrorCounter =
            Counter.builder("ds.task.dispatch.error.count")
                    .description("Number of errors during task dispatch")
                    .register(Metrics.globalRegistry);

    /**
     * 注册待执行任务数量的Gauge指标。
     *
     * @param consumer 提供当前待执行任务数量的Supplier函数
     */
    public synchronized void registerTaskPrepared(Supplier<Number> consumer) {
        Gauge.builder("ds.task.prepared", consumer)
                .description("Task prepared count")
                .register(Metrics.globalRegistry);
    }

    /**
     * 增加任务分发失败计数。
     *
     * @param failedCount 本次失败的任务数量
     */
    public void incTaskDispatchFailed(int failedCount) {
        taskDispatchFailCounter.increment(failedCount);
    }

    /**
     * 增加任务分发错误计数。
     */
    public void incTaskDispatchError() {
        taskDispatchErrorCounter.increment();
    }

    /**
     * 增加任务分发计数。
     */
    public void incTaskDispatch() {
        taskDispatchCounter.increment();
    }

    /**
     * 根据状态增加任务实例计数。
     *
     * @param state 任务实例状态（如submit、timeout、finish、success、fail等）
     */
    public void incTaskInstanceByState(final String state) {
        if (taskInstanceCounters.get(state) == null) {
            return;
        }
        taskInstanceCounters.get(state).increment();
    }

}
