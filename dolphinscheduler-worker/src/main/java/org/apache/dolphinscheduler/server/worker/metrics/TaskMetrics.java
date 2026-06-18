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

package org.apache.dolphinscheduler.server.worker.metrics;


import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.experimental.UtilityClass;

/**
 * 任务指标工具类。提供按任务类型统计执行次数的计数功能，通过Micrometer指标注册到全局监控系统。
 * 支持所有通过SPI加载的任务类型以及未知类型的自动注册。
 */
@UtilityClass
public class TaskMetrics {

    private final Map<String, Counter> taskTypeExecutionCounter = new HashMap<>();
    private final Counter taskUnknownTypeExecutionCounter =
            Counter.builder("ds.task.execution.count.by.type")
                    .tag("task_type", "unknown")
                    .description("task execution counter by type")
                    .register(Metrics.globalRegistry);

    static {
        for (TaskChannelFactory taskChannelFactory : ServiceLoader.load(TaskChannelFactory.class)) {
            taskTypeExecutionCounter.put(
                    taskChannelFactory.getName(),
                    Counter.builder("ds.task.execution.count.by.type")
                            .tag("task_type", taskChannelFactory.getName())
                            .description("task execution counter by type")
                            .register(Metrics.globalRegistry)
            );
        }
    }

    /**
     * 增加指定任务类型的执行计数。如果任务类型未注册，则使用未知类型计数器进行统计。
     *
     * @param taskType 任务类型名称
     */
    public void incrTaskTypeExecuteCount(String taskType) {
        taskTypeExecutionCounter.getOrDefault(taskType, taskUnknownTypeExecutionCounter).increment();
    }

}
