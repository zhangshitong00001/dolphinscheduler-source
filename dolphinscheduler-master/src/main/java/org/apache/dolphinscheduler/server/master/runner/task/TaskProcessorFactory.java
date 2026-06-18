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

package org.apache.dolphinscheduler.server.master.runner.task;

import lombok.experimental.UtilityClass;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMON_TASK_TYPE;

import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务处理器工厂类，通过 SPI 机制注册和创建各类任务处理器实例。
 * 在类加载时扫描所有 ITaskProcessor 的实现，按类型存储构造器，
 * 运行时根据任务类型反射创建对应的处理器实例。
 * 若未找到匹配类型，则默认使用 CommonTaskProcessor。
 */
@UtilityClass
public final class TaskProcessorFactory {

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessorFactory.class);

    private static final Map<String, Constructor<ITaskProcessor>> PROCESS_MAP = new ConcurrentHashMap<>();

    private static final String DEFAULT_PROCESSOR = COMMON_TASK_TYPE;

    static {
        PrioritySPIFactory<ITaskProcessor> prioritySPIFactory = new PrioritySPIFactory<>(ITaskProcessor.class);
        for (Map.Entry<String, ITaskProcessor> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            try {
                logger.info("Registering task processor: {} - {}", entry.getKey(), entry.getValue().getClass());
                PROCESS_MAP.put(entry.getKey(), (Constructor<ITaskProcessor>) entry.getValue().getClass().getConstructor());
                logger.info("Registered task processor: {} - {}", entry.getKey(), entry.getValue().getClass());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("The task processor: %s should has a no args constructor", entry.getKey()));
            }
        }
    }

    /**
     * 根据任务类型获取对应的任务处理器实例。
     *
     * @param type 任务类型
     * @return 任务处理器实例
     * @throws InvocationTargetException 反射调用异常
     * @throws InstantiationException    实例化异常
     * @throws IllegalAccessException    访问权限异常
     */
    public static ITaskProcessor getTaskProcessor(String type) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (StringUtils.isEmpty(type)) {
            type = DEFAULT_PROCESSOR;
        }
        Constructor<ITaskProcessor> iTaskProcessorConstructor = PROCESS_MAP.get(type);
        if (iTaskProcessorConstructor == null) {
            iTaskProcessorConstructor = PROCESS_MAP.get(DEFAULT_PROCESSOR);
        }

        return iTaskProcessorConstructor.newInstance();
    }

    /**
     * 判断指定类型的任务是否为 Master 本地执行的任务。
     *
     * @param type 任务类型
     * @return 如果已在 PROCESS_MAP 中注册则返回 true，表示是 Master 本地任务
     */
    public static boolean isMasterTask(String type) {
        return PROCESS_MAP.containsKey(type);
    }

}
