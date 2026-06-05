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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.dto.TaskInstanceExecuteDto;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.controller.WorkflowExecuteController;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 执行数据查询服务，用于从内存缓存中查询正在执行的工作流实例数据。
 * 提供将内存中的 ProcessInstance 和 TaskInstance 转换为 DTO 返回给 API 层的能力。
 */
@Component
public class ExecutingService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecuteController.class);

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    /**
     * 查询指定工作流实例的执行数据（包含任务实例列表）。
     *
     * @param processInstanceId 工作流实例 ID
     * @return 工作流执行数据 DTO，如果不在缓存中则返回 empty
     */
    public Optional<WorkflowExecuteDto> queryWorkflowExecutingData(Integer processInstanceId) {
        WorkflowExecuteRunnable workflowExecuteRunnable = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
        if (workflowExecuteRunnable == null) {
            logger.info("workflow execute data not found, maybe it has finished, workflow id:{}", processInstanceId);
            return Optional.empty();
        }
        try {
            WorkflowExecuteDto workflowExecuteDto = new WorkflowExecuteDto();
            BeanUtils.copyProperties(workflowExecuteDto, workflowExecuteRunnable.getProcessInstance());
            List<TaskInstanceExecuteDto> taskInstanceList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(workflowExecuteRunnable.getAllTaskInstances())) {
                for (TaskInstance taskInstance : workflowExecuteRunnable.getAllTaskInstances()) {
                    TaskInstanceExecuteDto taskInstanceExecuteDto = new TaskInstanceExecuteDto();
                    BeanUtils.copyProperties(taskInstanceExecuteDto, taskInstance);
                    taskInstanceList.add(taskInstanceExecuteDto);
                }
            }
            workflowExecuteDto.setTaskInstances(taskInstanceList);
            return Optional.of(workflowExecuteDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("query workflow execute data fail, workflow id:{}", processInstanceId, e);
        }
        return Optional.empty();
    }
}
