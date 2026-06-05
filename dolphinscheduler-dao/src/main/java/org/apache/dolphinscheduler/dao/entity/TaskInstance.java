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

package org.apache.dolphinscheduler.dao.entity;

import static org.apache.dolphinscheduler.common.constants.Constants.SEC_2_MINUTES_TIME_UNIT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 任务实例实体，映射到 t_ds_task_instance 表，存储任务运行时的实例数据。
 */
@Data
@TableName("t_ds_task_instance")
public class TaskInstance implements Serializable {

    /** 任务实例主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 任务名称 */
    private String name;

    /** 任务类型 */
    private String taskType;

    /** 流程实例 ID */
    private int processInstanceId;

    /** 任务编码 */
    private long taskCode;

    /** 任务定义版本号 */
    private int taskDefinitionVersion;

    /** 非数据库字段：流程实例名称 */
    @TableField(exist = false)
    private String processInstanceName;

    /** 非数据库字段：流程定义名称 */
    @TableField(exist = false)
    private String processDefinitionName;

    /** 非数据库字段：任务组优先级 */
    @TableField(exist = false)
    private int taskGroupPriority;

    /** 任务执行状态 */
    private TaskExecutionStatus state;

    /** 任务首次提交时间 */
    private Date firstSubmitTime;

    /** 任务提交时间 */
    private Date submitTime;

    /** 任务开始时间 */
    private Date startTime;

    /** 任务结束时间 */
    private Date endTime;

    /** 执行任务的主机地址 */
    private String host;

    /** 任务执行路径，资源从 HDFS 下载到该路径，默认路径：$base_run_dir/processInstanceId/taskInstanceId/retryTimes */
    private String executePath;

    /** 任务日志路径，默认路径：$base_run_dir/processInstanceId/taskInstanceId/retryTimes */
    private String logPath;

    /** 重试次数 */
    private int retryTimes;

    /** 告警标志 */
    private Flag alertFlag;

    /** 非数据库字段：关联的流程实例 */
    @TableField(exist = false)
    private ProcessInstance processInstance;

    /** 非数据库字段：关联的流程定义 */
    @TableField(exist = false)
    private ProcessDefinition processDefine;

    /** 非数据库字段：关联的任务定义 */
    @TableField(exist = false)
    private TaskDefinition taskDefine;

    /** 进程 ID */
    private int pid;

    /** 应用链接 */
    private String appLink;

    /** 标志：是否有效 */
    private Flag flag;

    /** 非数据库字段：依赖参数 */
    @TableField(exist = false)
    private DependentParameters dependency;

    /** 非数据库字段：Switch 依赖参数 */
    @TableField(exist = false)
    private SwitchParameters switchDependency;

    /** 非数据库字段：任务执行持续时间 */
    @TableField(exist = false)
    private String duration;

    /** 最大重试次数 */
    private int maxRetryTimes;

    /** 任务重试间隔，单位：分钟 */
    private int retryInterval;

    /** 任务实例优先级 */
    private Priority taskInstancePriority;

    /** 非数据库字段：流程实例优先级 */
    @TableField(exist = false)
    private Priority processInstancePriority;

    /** 非数据库字段：依赖判断结果 */
    @TableField(exist = false)
    private String dependentResult;

    /** Worker 分组名称 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;

    /** 环境配置 */
    private String environmentConfig;

    /** 执行者 ID */
    private int executorId;

    /** 变量池，JSON 字符串格式 */
    private String varPool;

    /** 非数据库字段：执行者名称 */
    @TableField(exist = false)
    private String executorName;

    /** 非数据库字段：资源映射 */
    @TableField(exist = false)
    private Map<String, String> resources;

    /** 延时执行时间 */
    private int delayTime;

    /** 任务参数 */
    private String taskParams;

    /** 试运行标志 */
    private int dryRun;
    /** 任务组 ID */
    private int taskGroupId;

    /** CPU 配额 */
    private Integer cpuQuota;

    /** 最大内存 */
    private Integer memoryMax;

    /** 任务执行类型 */
    private TaskExecuteType taskExecuteType;

    public void init(String host, Date startTime, String executePath) {
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }

    public DependentParameters getDependency() {
        if (this.dependency == null) {
            Map<String, Object> taskParamsMap =
                    JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
                    });
            this.dependency =
                    JSONUtils.parseObject((String) taskParamsMap.get(Constants.DEPENDENCE), DependentParameters.class);
        }
        return this.dependency;
    }

    public void setDependency(DependentParameters dependency) {
        this.dependency = dependency;
    }

    public SwitchParameters getSwitchDependency() {
        if (this.switchDependency == null) {
            Map<String, Object> taskParamsMap =
                    JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
                    });
            this.switchDependency =
                    JSONUtils.parseObject((String) taskParamsMap.get(Constants.SWITCH_RESULT), SwitchParameters.class);
        }
        return this.switchDependency;
    }

    public void setSwitchDependency(SwitchParameters switchDependency) {
        Map<String, Object> taskParamsMap =
                JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {
                });
        taskParamsMap.put(Constants.SWITCH_RESULT, JSONUtils.toJsonString(switchDependency));
        this.setTaskParams(JSONUtils.toJsonString(taskParamsMap));
    }

    public boolean isTaskComplete() {

        return this.getState().isSuccess()
                || this.getState().isKill()
                || (this.getState().isFailure() && !taskCanRetry());
    }

    public boolean isSubProcess() {
        return TASK_TYPE_SUB_PROCESS.equalsIgnoreCase(this.taskType);
    }

    public boolean isDependTask() {
        return TASK_TYPE_DEPENDENT.equalsIgnoreCase(this.taskType);
    }

    public boolean isConditionsTask() {
        return TASK_TYPE_CONDITIONS.equalsIgnoreCase(this.taskType);
    }

    public boolean isSwitchTask() {
        return TASK_TYPE_SWITCH.equalsIgnoreCase(this.taskType);
    }

    public boolean isBlockingTask() {
        return TASK_TYPE_BLOCKING.equalsIgnoreCase(this.taskType);
    }

    public boolean isFirstRun() {
        return endTime == null;
    }

    /**
     * 判断任务实例是否可以重试，子流程不可重试。
     *
     * @return 是否可重试
     */
    public boolean taskCanRetry() {
        if (this.isSubProcess()) {
            return false;
        }
        if (this.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        }
        return this.getState() == TaskExecutionStatus.FAILURE && (this.getRetryTimes() < this.getMaxRetryTimes());
    }

    /**
     * 判断重试间隔是否已超时
     *
     * @return 是否超时
     */
    public boolean retryTaskIntervalOverTime() {
        if (getState() != TaskExecutionStatus.FAILURE) {
            return true;
        }
        if (getMaxRetryTimes() == 0 || getRetryInterval() == 0) {
            return true;
        }
        Date now = new Date();
        long failedTimeInterval = DateUtils.differSec(now, getEndTime());
        // task retry does not over time, return false
        return getRetryInterval() * SEC_2_MINUTES_TIME_UNIT < failedTimeInterval;
    }
}
