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

package org.apache.dolphinscheduler.service.model;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.model.PreviousTaskNode;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.parameters.TaskTimeoutParameter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 任务节点模型，表示工作流DAG中的一个任务节点。包含任务的基本信息、依赖关系、超时配置、资源配额等属性。
 */
public class TaskNode {

    /**
     * task node id
     */
    private String id;

    /**
     * task node code
     */
    private long code;

    /**
     * task node version
     */
    private int version;

    /**
     * task node name
     */
    private String name;

    /**
     * task node description
     */
    private String desc;

    /**
     * task node type
     */
    private String type;

    /**
     * the run flag has two states, NORMAL or FORBIDDEN
     */
    private String runFlag;

    /**
     * the front field
     */
    private String loc;

    /**
     * maximum number of retries
     */
    private int maxRetryTimes;

    /**
     * Unit of retry interval: points
     */
    private int retryInterval;

    /**
     * task group id
     */
    private int taskGroupId;
    /**
     * task group id
     */
    private int taskGroupPriority;

    /**
     * params information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String params;

    /**
     * inner dependency information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String preTasks;

    /**
     * node dependency list
     */
    private List<PreviousTaskNode> preTaskNodeList;

    /**
     * users store additional information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String extras;

    /**
     * node dependency list
     */
    private List<String> depList;

    /**
     * outer dependency information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String dependence;

    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String conditionResult;

    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String switchResult;

    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String waitStartTimeout;

    /**
     * task instance priority
     */
    private Priority taskInstancePriority;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * task time out
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String timeout;

    /**
     * delay execution time.
     */
    private int delayTime;

    /**
     * cpu quota
     */
    private Integer cpuQuota;

    /**
     * max memory
     */
    private Integer memoryMax;

    /**
     * task execute type
     */
    private TaskExecuteType taskExecuteType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getPreTasks() {
        return preTasks;
    }

    public void setPreTasks(String preTasks) {
        this.preTasks = preTasks;
        this.depList = JSONUtils.toList(preTasks, String.class);
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public List<String> getDepList() {
        return depList;
    }

    public void setDepList(List<String> depList) {
        if (depList != null) {
            this.depList = depList;
            this.preTasks = JSONUtils.toJsonString(depList);
        }
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getRunFlag() {
        return runFlag;
    }

    public void setRunFlag(String runFlag) {
        this.runFlag = runFlag;
    }

    /**
     * 判断任务节点是否被禁止执行。当运行标志为 FORBIDDEN 或任务执行类型为 STREAM 时返回 true。
     *
     * @return true if the task node is forbidden
     */
    public boolean isForbidden() {
        // skip stream task when run DAG
        if (taskExecuteType == TaskExecuteType.STREAM) {
            return true;
        }
        return StringUtils.isNotEmpty(this.runFlag) && this.runFlag.equals(Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskNode taskNode = (TaskNode) o;
        return Objects.equals(name, taskNode.name)
                && Objects.equals(desc, taskNode.desc)
                && Objects.equals(type, taskNode.type)
                && Objects.equals(params, taskNode.params)
                && Objects.equals(preTasks, taskNode.preTasks)
                && Objects.equals(extras, taskNode.extras)
                && Objects.equals(runFlag, taskNode.runFlag)
                && Objects.equals(dependence, taskNode.dependence)
                && Objects.equals(workerGroup, taskNode.workerGroup)
                && Objects.equals(environmentCode, taskNode.environmentCode)
                && Objects.equals(conditionResult, taskNode.conditionResult)
                && CollectionUtils.isEqualCollection(depList, taskNode.depList)
                && Objects.equals(taskExecuteType, taskNode.taskExecuteType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc, type, params, preTasks, extras, depList, runFlag);
    }

    public String getDependence() {
        return dependence;
    }

    public void setDependence(String dependence) {
        this.dependence = dependence;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Priority getTaskInstancePriority() {
        return taskInstancePriority;
    }

    public void setTaskInstancePriority(Priority taskInstancePriority) {
        this.taskInstancePriority = taskInstancePriority;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public String getConditionResult() {
        return conditionResult;
    }

    public void setConditionResult(String conditionResult) {
        this.conditionResult = conditionResult;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * 获取任务超时参数，将超时策略字符串解析为 TaskTimeoutParameter 对象。
     * 如果未设置超时时间，则返回默认的未启用超时参数。
     *
     * @return task time out parameter
     */
    public TaskTimeoutParameter getTaskTimeoutParameter() {
        if (!StringUtils.isEmpty(this.getTimeout())) {
            String formatStr =
                    String.format("%s,%s", TaskTimeoutStrategy.WARN.name(), TaskTimeoutStrategy.FAILED.name());
            String taskTimeout = this.getTimeout().replace(formatStr, TaskTimeoutStrategy.WARNFAILED.name());
            return JSONUtils.parseObject(taskTimeout, TaskTimeoutParameter.class);
        }
        return new TaskTimeoutParameter(false);
    }

    /**
     * 判断当前任务节点是否为条件任务类型。
     *
     * @return true if this task node is a conditions task
     */
    public boolean isConditionsTask() {
        return TASK_TYPE_CONDITIONS.equalsIgnoreCase(this.getType());
    }

    /**
     * 判断当前任务节点是否为分支(Switch)任务类型。
     *
     * @return true if this task node is a switch task
     */
    public boolean isSwitchTask() {
        return TASK_TYPE_SWITCH.equalsIgnoreCase(this.getType());
    }

    public List<PreviousTaskNode> getPreTaskNodeList() {
        return preTaskNodeList;
    }

    /**
     * 判断当前任务节点是否为阻塞任务类型。
     *
     * @return true if this task node is a blocking task
     */
    public boolean isBlockingTask() {
        return TASK_TYPE_BLOCKING.equalsIgnoreCase(this.getType());
    }

    public void setPreTaskNodeList(List<PreviousTaskNode> preTaskNodeList) {
        this.preTaskNodeList = preTaskNodeList;
    }

    /**
     * 获取完整的任务参数字符串，将条件结果、依赖、分支结果和等待启动超时等参数合并到任务参数中。
     *
     * @return task params JSON string with all merged parameters
     */
    public String getTaskParams() {
        Map<String, Object> taskParams = JSONUtils.parseObject(this.params, new TypeReference<Map<String, Object>>() {
        });

        if (taskParams == null) {
            taskParams = new HashMap<>();
        }
        taskParams.put(Constants.CONDITION_RESULT, this.conditionResult);
        taskParams.put(Constants.DEPENDENCE, this.dependence);
        taskParams.put(Constants.SWITCH_RESULT, this.switchResult);
        taskParams.put(Constants.WAIT_START_TIMEOUT, this.waitStartTimeout);
        return JSONUtils.toJsonString(taskParams);
    }

    /**
     * 将任务参数字符串解析为 Map 对象，如果解析失败则返回空 HashMap。
     *
     * @param taskParams the task parameters JSON string
     * @return parsed task parameters map
     */
    public Map<String, Object> taskParamsToJsonObj(String taskParams) {
        Map<String, Object> taskParamsMap = JSONUtils.parseObject(taskParams, new TypeReference<Map<String, Object>>() {
        });
        if (taskParamsMap == null) {
            taskParamsMap = new HashMap<>();
        }
        return taskParamsMap;
    }

    @Override
    public String toString() {
        return "TaskNode{"
                + "id='" + id + '\''
                + ", code=" + code
                + ", version=" + version
                + ", name='" + name + '\''
                + ", desc='" + desc + '\''
                + ", type='" + type + '\''
                + ", runFlag='" + runFlag + '\''
                + ", loc='" + loc + '\''
                + ", maxRetryTimes=" + maxRetryTimes
                + ", retryInterval=" + retryInterval
                + ", params='" + params + '\''
                + ", preTasks='" + preTasks + '\''
                + ", preTaskNodeList=" + preTaskNodeList
                + ", extras='" + extras + '\''
                + ", depList=" + depList
                + ", dependence='" + dependence + '\''
                + ", conditionResult='" + conditionResult + '\''
                + ", taskInstancePriority=" + taskInstancePriority
                + ", workerGroup='" + workerGroup + '\''
                + ", environmentCode=" + environmentCode
                + ", timeout='" + timeout + '\''
                + ", delayTime=" + delayTime + '\''
                + ", taskExecuteType=" + taskExecuteType
                + '}';
    }

    public void setEnvironmentCode(Long environmentCode) {
        this.environmentCode = environmentCode;
    }

    public Long getEnvironmentCode() {
        return this.environmentCode;
    }

    public String getSwitchResult() {
        return switchResult;
    }

    public void setSwitchResult(String switchResult) {
        this.switchResult = switchResult;
    }

    public String getWaitStartTimeout() {
        return this.waitStartTimeout;
    }

    public void setWaitStartTimeout(String waitStartTimeout) {
        this.waitStartTimeout = waitStartTimeout;
    }

    public int getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(int taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public int getTaskGroupPriority() {
        return taskGroupPriority;
    }

    public void setTaskGroupPriority(int taskGroupPriority) {
        this.taskGroupPriority = taskGroupPriority;
    }

    public Integer getCpuQuota() {
        return cpuQuota == null ? -1 : cpuQuota;
    }

    public void setCpuQuota(Integer cpuQuota) {
        this.cpuQuota = cpuQuota;
    }

    public Integer getMemoryMax() {
        return memoryMax == null ? -1 : memoryMax;
    }

    public void setMemoryMax(Integer memoryMax) {
        this.memoryMax = memoryMax;
    }

    public TaskExecuteType getTaskExecuteType() {
        return taskExecuteType;
    }

    public void setTaskExecuteType(TaskExecuteType taskExecuteType) {
        this.taskExecuteType = taskExecuteType;
    }
}
