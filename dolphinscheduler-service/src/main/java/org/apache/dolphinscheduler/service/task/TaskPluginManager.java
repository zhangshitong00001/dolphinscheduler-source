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

package org.apache.dolphinscheduler.service.task;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务插件管理器，通过SPI机制加载和管理所有任务插件。负责从classpath加载 TaskChannelFactory 实现，
 * 维护任务通道的注册表，并提供任务参数校验和解析功能。
 */
@Component
public class TaskPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskPluginManager.class);

    private final Map<String, TaskChannelFactory> taskChannelFactoryMap = new HashMap<>();
    private final Map<String, TaskChannel> taskChannelMap = new HashMap<>();

    private final AtomicBoolean loadedFlag = new AtomicBoolean(false);

    /**
     * Load task plugins from classpath.
     */
    public void loadPlugin() {
        if (!loadedFlag.compareAndSet(false, true)) {
            logger.warn("The task plugin has already been loaded");
            return;
        }
        PrioritySPIFactory<TaskChannelFactory> prioritySPIFactory = new PrioritySPIFactory<>(TaskChannelFactory.class);
        for (Map.Entry<String, TaskChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            String factoryName = entry.getKey();
            TaskChannelFactory factory = entry.getValue();

            logger.info("Registering task plugin: {} - {}", factoryName, factory.getClass());

            taskChannelFactoryMap.put(factoryName, factory);
            taskChannelMap.put(factoryName, factory.create());

            logger.info("Registered task plugin: {} - {}", factoryName, factory.getClass());
        }

    }

    /**
     * 获取所有已注册的任务通道映射表（只读）。
     *
     * @return unmodifiable map of task channel
     */
    public Map<String, TaskChannel> getTaskChannelMap() {
        return Collections.unmodifiableMap(taskChannelMap);
    }

    /**
     * 获取所有已注册的任务通道工厂映射表（只读）。
     *
     * @return unmodifiable map of task channel factories
     */
    public Map<String, TaskChannelFactory> getTaskChannelFactoryMap() {
        return Collections.unmodifiableMap(taskChannelFactoryMap);
    }

    /**
     * 根据任务类型获取对应的任务通道。
     *
     * @param type the task type name
     * @return the task channel for the given type, or null if not found
     */
    public TaskChannel getTaskChannel(String type) {
        return this.getTaskChannelMap().get(type);
    }

    /**
     * 校验任务参数的有效性。
     *
     * @param parametersNode the parameters node to check
     * @return true if parameters are valid
     */
    public boolean checkTaskParameters(ParametersNode parametersNode) {
        AbstractParameters abstractParameters = this.getParameters(parametersNode);
        return abstractParameters != null && abstractParameters.checkParameters();
    }

    /**
     * 解析任务参数节点为具体的参数对象。
     *
     * @param parametersNode the parameters node to parse
     * @return the parsed AbstractParameters object, or null if parsing fails
     */
    public AbstractParameters getParameters(ParametersNode parametersNode) {
        String taskType = parametersNode.getTaskType();
        if (Objects.isNull(taskType)) {
            return null;
        }
        TaskChannel taskChannel = this.getTaskChannelMap().get(taskType);
        if (Objects.isNull(taskChannel)) {
            return null;
        }
        return taskChannel.parseParameters(parametersNode);
    }

}
