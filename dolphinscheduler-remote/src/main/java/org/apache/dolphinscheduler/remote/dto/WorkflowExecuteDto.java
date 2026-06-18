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

package org.apache.dolphinscheduler.remote.dto;

import org.apache.dolphinscheduler.common.enums.*;

import java.util.Collection;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 工作流执行DTO，用于在远程通信中传输工作流实例的完整执行数据。
 */
@Setter
@Getter
public class WorkflowExecuteDto {

    /** 流程实例ID */
    private int id;

    /** 流程实例名称 */
    private String name;

    /** 流程定义编码 */
    private Long processDefinitionCode;

    /** 流程定义版本号 */
    private int processDefinitionVersion;

    /** 工作流执行状态 */
    private WorkflowExecutionStatus state;

    /** 容错恢复标志 */
    private Flag recovery;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 运行次数 */
    private int runTimes;

    /** 执行主机 */
    private String host;

    /** 命令类型 */
    private CommandType commandType;

    /** 命令参数 */
    private String commandParam;

    /** 任务依赖类型 */
    private TaskDependType taskDependType;

    /** 最大尝试次数 */
    private int maxTryTimes;

    /** 任务失败时的失败策略 */
    private FailureStrategy failureStrategy;

    /** 告警类型 */
    private WarningType warningType;

    /** 告警组ID */
    private Integer warningGroupId;

    /** 调度时间 */
    private Date scheduleTime;

    /** 命令开始时间 */
    private Date commandStartTime;

    /** 用户自定义参数 */
    private String globalParams;

    /** 执行者ID */
    private int executorId;

    /** 执行者名称 */
    private String executorName;

    /** 租户编码 */
    private String tenantCode;

    /** 队列 */
    private String queue;

    /** 是否为子流程 */
    private Flag isSubProcess;

    /** 历史命令 */
    private String historyCmd;

    /** 依赖流程的调度时间 */
    private String dependenceScheduleTimes;

    /** 运行时长 */
    private String duration;

    /** 流程实例优先级 */
    private Priority processInstancePriority;

    /** Worker分组 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;

    /** 超时时间 */
    private int timeout;

    /** 租户ID */
    private int tenantId;

    /** 变量池字符串 */
    private String varPool;

    /** 下一个流程实例ID */
    private int nextProcessInstanceId;

    /** 是否试运行 */
    private int dryRun;

    /** 重启时间 */
    private Date restartTime;

    /** 是否被阻塞 */
    private boolean isBlocked;

    /** 任务实例集合 */
    private Collection<TaskInstanceExecuteDto> taskInstances;
}
