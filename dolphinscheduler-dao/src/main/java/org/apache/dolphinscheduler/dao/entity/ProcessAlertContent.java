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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程告警内容 DTO，用于封装告警发送时所需的流程和任务上下文信息，包括项目信息、流程状态、任务执行结果等。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class ProcessAlertContent implements Serializable {

    /** 项目 ID */
    @JsonProperty("projectId")
    private Integer projectId;
    /** 项目编码 */
    @JsonProperty("projectCode")
    private Long projectCode;
    /** 项目名称 */
    @JsonProperty("projectName")
    private String projectName;
    /** 项目负责人 */
    @JsonProperty("owner")
    private String owner;
    /** 流程 ID */
    @JsonProperty("processId")
    private Integer processId;
    /** 流程定义编码 */
    @JsonProperty("processDefinitionCode")
    private Long processDefinitionCode;
    /** 流程名称 */
    @JsonProperty("processName")
    private String processName;
    /** 流程命令类型 */
    @JsonProperty("processType")
    private CommandType processType;
    /** 流程执行状态 */
    @JsonProperty("processState")
    private WorkflowExecutionStatus processState;
    /** 容错标识 */
    @JsonProperty("recovery")
    private Flag recovery;
    /** 运行次数 */
    @JsonProperty("runTimes")
    private Integer runTimes;
    /** 流程开始时间 */
    @JsonProperty("processStartTime")
    private Date processStartTime;
    /** 流程结束时间 */
    @JsonProperty("processEndTime")
    private Date processEndTime;
    /** 流程执行主机 */
    @JsonProperty("processHost")
    private String processHost;
    /** 任务编码 */
    @JsonProperty("taskCode")
    private Long taskCode;
    /** 任务名称 */
    @JsonProperty("taskName")
    private String taskName;
    /** 告警事件类型 */
    @JsonProperty("event")
    private AlertEvent event;
    /** 告警级别 */
    @JsonProperty("warnLevel")
    private AlertWarnLevel warnLevel;
    /** 任务类型 */
    @JsonProperty("taskType")
    private String taskType;
    /** 重试次数 */
    @JsonProperty("retryTimes")
    private Integer retryTimes;
    /** 任务执行状态 */
    @JsonProperty("taskState")
    private TaskExecutionStatus taskState;
    /** 任务开始时间 */
    @JsonProperty("taskStartTime")
    private Date taskStartTime;
    /** 任务结束时间 */
    @JsonProperty("taskEndTime")
    private Date taskEndTime;
    /** 任务执行主机 */
    @JsonProperty("taskHost")
    private String taskHost;
    /** 日志路径 */
    @JsonProperty("logPath")
    private String logPath;

}
