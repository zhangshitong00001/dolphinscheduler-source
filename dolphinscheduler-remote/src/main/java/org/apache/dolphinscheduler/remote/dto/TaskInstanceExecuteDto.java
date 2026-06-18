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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;

import java.util.Date;
import java.util.Map;

import lombok.Data;

import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * 任务实例执行DTO，用于在远程通信中传输任务实例的执行数据。
 */
@Data
public class TaskInstanceExecuteDto {

    /** 任务实例ID */
    private int id;

    /** 任务名称 */
    private String name;

    /** 任务类型 */
    private String taskType;

    /** 流程实例ID */
    private int processInstanceId;

    /** 任务编码 */
    private long taskCode;

    /** 任务定义版本号 */
    private int taskDefinitionVersion;

    /** 流程实例名称 */
    private String processInstanceName;

    /** 任务组优先级 */
    private int taskGroupPriority;

    /** 任务执行状态 */
    private TaskExecutionStatus state;

    /** 首次提交时间 */
    private Date firstSubmitTime;

    /** 提交时间 */
    private Date submitTime;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 执行主机 */
    private String host;

    /** 执行路径 */
    private String executePath;

    /** 日志路径 */
    private String logPath;

    /** 重试次数 */
    private int retryTimes;

    /** 告警标志 */
    private Flag alertFlag;

    /** 进程ID */
    private int pid;

    /** 应用链接 */
    private String appLink;

    /** 标志 */
    private Flag flag;

    /** 运行时长 */
    private String duration;

    /** 最大重试次数 */
    private int maxRetryTimes;

    /** 重试间隔 */
    private int retryInterval;

    /** 任务实例优先级 */
    private Priority taskInstancePriority;

    /** 流程实例优先级 */
    private Priority processInstancePriority;

    /** Worker分组 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;

    /** 环境配置 */
    private String environmentConfig;

    /** 执行者ID */
    private int executorId;

    /** 变量池 */
    private String varPool;

    /** 执行者名称 */
    private String executorName;

    /** 资源映射 */
    private Map<String, String> resources;

    /** 延迟时间 */
    private int delayTime;

    /** 任务参数 */
    private String taskParams;

    /** 是否试运行 */
    private int dryRun;

    /** 任务组ID */
    private int taskGroupId;

    /** CPU配额 */
    private Integer cpuQuota;

    /** 最大内存 */
    private Integer memoryMax;

    /** 任务执行类型 */
    private TaskExecuteType taskExecuteType;
}
