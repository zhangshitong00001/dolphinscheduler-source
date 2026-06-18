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

package org.apache.dolphinscheduler.server.master.builder;

import static org.apache.dolphinscheduler.common.constants.Constants.SEC_2_MINUTES_TIME_UNIT;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.Map;

/**
 * 任务执行上下文构建器。通过链式调用组装 TaskExecutionContext 所需的各类信息。
 */
public class TaskExecutionContextBuilder {

    public static TaskExecutionContextBuilder get() {
        return new TaskExecutionContextBuilder();
    }

    private TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

    /**
     * 构建任务实例相关信息。
     *
     * @param taskInstance 任务实例
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildTaskInstanceRelatedInfo(TaskInstance taskInstance) {
        taskExecutionContext.setTaskInstanceId(taskInstance.getId());
        taskExecutionContext.setTaskName(taskInstance.getName());
        taskExecutionContext.setFirstSubmitTime(taskInstance.getFirstSubmitTime());
        taskExecutionContext.setStartTime(taskInstance.getStartTime());
        taskExecutionContext.setTaskType(taskInstance.getTaskType());
        taskExecutionContext.setLogPath(taskInstance.getLogPath());
        taskExecutionContext.setWorkerGroup(taskInstance.getWorkerGroup());
        taskExecutionContext.setEnvironmentConfig(taskInstance.getEnvironmentConfig());
        taskExecutionContext.setHost(taskInstance.getHost());
        taskExecutionContext.setResources(taskInstance.getResources());
        taskExecutionContext.setDelayTime(taskInstance.getDelayTime());
        taskExecutionContext.setVarPool(taskInstance.getVarPool());
        taskExecutionContext.setDryRun(taskInstance.getDryRun());
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskExecutionContext.setCpuQuota(taskInstance.getCpuQuota());
        taskExecutionContext.setMemoryMax(taskInstance.getMemoryMax());
        taskExecutionContext.setAppIds(taskInstance.getAppLink());
        return this;
    }

    /**
     * 构建任务定义相关信息，包括超时配置和任务参数。
     *
     * @param taskDefinition 任务定义
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildTaskDefinitionRelatedInfo(TaskDefinition taskDefinition) {
        taskExecutionContext.setTaskTimeout(Integer.MAX_VALUE);
        if (taskDefinition.getTimeoutFlag() == TimeoutFlag.OPEN) {
            taskExecutionContext.setTaskTimeoutStrategy(taskDefinition.getTimeoutNotifyStrategy());
            if (taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.FAILED
                    || taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.WARNFAILED) {
                taskExecutionContext.setTaskTimeout(
                        Math.min(taskDefinition.getTimeout() * SEC_2_MINUTES_TIME_UNIT, Integer.MAX_VALUE));
            }
        }
        taskExecutionContext.setTaskParams(taskDefinition.getTaskParams());
        return this;
    }

    /**
     * 构建流程实例相关信息。
     *
     * @param processInstance 流程实例
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessInstanceRelatedInfo(ProcessInstance processInstance) {
        taskExecutionContext.setProcessInstanceId(processInstance.getId());
        taskExecutionContext.setScheduleTime(processInstance.getScheduleTime());
        taskExecutionContext.setGlobalParams(processInstance.getGlobalParams());
        taskExecutionContext.setExecutorId(processInstance.getExecutorId());
        taskExecutionContext.setCmdTypeIfComplement(processInstance.getCmdTypeIfComplement().getCode());
        taskExecutionContext.setTenantCode(processInstance.getTenantCode());
        taskExecutionContext.setQueue(processInstance.getQueue());
        return this;
    }

    /**
     * 构建流程定义相关信息。
     *
     * @param processDefinition 流程定义
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessDefinitionRelatedInfo(ProcessDefinition processDefinition) {
        taskExecutionContext.setProcessDefineCode(processDefinition.getCode());
        taskExecutionContext.setProcessDefineVersion(processDefinition.getVersion());
        taskExecutionContext.setProjectCode(processDefinition.getProjectCode());
        return this;
    }

    /**
     * 构建数据质量任务执行上下文。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildDataQualityTaskExecutionContext(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        taskExecutionContext.setDataQualityTaskExecutionContext(dataQualityTaskExecutionContext);
        return this;
    }

    /**
     * 构建资源参数信息。
     *
     * @param parametersHelper 资源参数辅助对象
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildResourceParametersInfo(ResourceParametersHelper parametersHelper) {
        taskExecutionContext.setResourceParametersHelper(parametersHelper);
        return this;
    }
    /**
     * 构建 K8s 任务相关信息。
     *
     * @param k8sTaskExecutionContext K8s 任务执行上下文
     * @return TaskExecutionContextBuilder
     */

    public TaskExecutionContextBuilder buildK8sTaskRelatedInfo(K8sTaskExecutionContext k8sTaskExecutionContext) {
        taskExecutionContext.setK8sTaskExecutionContext(k8sTaskExecutionContext);
        return this;
    }

    /**
     * 构建全局和局部参数。
     * @param propertyMap 属性映射
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildParamInfo(Map<String, Property> propertyMap) {
        taskExecutionContext.setPrepareParamsMap(propertyMap);
        return this;
    }

    /**
     * 构建业务参数。
     * @param businessParamsMap 业务参数映射
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildBusinessParamsMap(Map<String, Property> businessParamsMap) {
        taskExecutionContext.setParamsMap(businessParamsMap);
        return this;
    }

    /**
     * 创建并返回组装完成的 TaskExecutionContext 对象。
     *
     * @return 组装完成的 taskExecutionContext
     */

    public TaskExecutionContext create() {
        return taskExecutionContext;
    }

}
