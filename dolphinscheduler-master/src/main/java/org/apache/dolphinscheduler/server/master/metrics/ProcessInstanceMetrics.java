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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.experimental.UtilityClass;

/**
 * 工作流实例指标收集工具类。用于记录和暴露工作流实例各状态的计数、命令查询耗时以及实例生成耗时等指标。
 */
@UtilityClass
public class ProcessInstanceMetrics {

    private final Map<String, Counter> processInstanceCounters = new HashMap<>();

    private final Set<String> processInstanceStates = ImmutableSet.of(
            "submit", "timeout", "finish", "failover", "success", "fail", "stop");

    static {
        for (final String state : processInstanceStates) {
            processInstanceCounters.put(
                    state,
                    Counter.builder("ds.workflow.instance.count")
                            .tag("state", state)
                            .description(String.format("Process instance %s total count", state))
                            .register(Metrics.globalRegistry)
            );
        }

    }

    private final Timer commandQueryTimer =
        Timer.builder("ds.workflow.command.query.duration")
            .description("Command query duration")
            .register(Metrics.globalRegistry);

    private final Timer processInstanceGenerateTimer =
        Timer.builder("ds.workflow.instance.generate.duration")
            .description("Process instance generated duration")
            .register(Metrics.globalRegistry);

    /**
     * 记录命令查询耗时。
     *
     * @param milliseconds 查询消耗的毫秒数
     */
    public void recordCommandQueryTime(long milliseconds) {
        commandQueryTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录工作流实例生成耗时。
     *
     * @param milliseconds 生成消耗的毫秒数
     */
    public void recordProcessInstanceGenerateTime(long milliseconds) {
        processInstanceGenerateTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * 注册正在运行的工作流实例数量的Gauge指标。
     *
     * @param function 提供当前运行实例数量的Supplier函数
     */
    public synchronized void registerProcessInstanceRunningGauge(Supplier<Number> function) {
        Gauge.builder("ds.workflow.instance.running", function)
            .description("The current running process instance count")
            .register(Metrics.globalRegistry);
    }

    /**
     * 注册需要重新提交的工作流实例数量的Gauge指标。
     *
     * @param function 提供当前待重新提交实例数量的Supplier函数
     */
    public synchronized void registerProcessInstanceResubmitGauge(Supplier<Number> function) {
        Gauge.builder("ds.workflow.instance.resubmit", function)
            .description("The current process instance need to resubmit count")
            .register(Metrics.globalRegistry);
    }

    /**
     * 根据状态增加工作流实例计数。
     *
     * @param state 工作流实例状态（如submit、timeout、finish、success、fail等）
     */
    public void incProcessInstanceByState(final String state) {
        processInstanceCounters.get(state).increment();
    }

}
