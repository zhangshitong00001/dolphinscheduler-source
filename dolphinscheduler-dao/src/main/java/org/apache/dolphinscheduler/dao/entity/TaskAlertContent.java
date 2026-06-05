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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * 任务告警内容，封装任务实例的告警上下文信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaskAlertContent implements Serializable {

    /** 任务实例 ID */
    @JsonProperty("taskInstanceId")
    private int taskInstanceId;
    /** 任务名称 */
    @JsonProperty("taskName")
    private String taskName;
    /** 任务类型 */
    @JsonProperty("taskType")
    private String taskType;
    /** 流程定义 ID */
    @JsonProperty("processDefinitionId")
    private int processDefinitionId;
    /** 流程定义名称 */
    @JsonProperty("processDefinitionName")
    private String processDefinitionName;
    /** 流程实例 ID */
    @JsonProperty("processInstanceId")
    private int processInstanceId;
    /** 流程实例名称 */
    @JsonProperty("processInstanceName")
    private String processInstanceName;
    /** 任务执行状态 */
    @JsonProperty("state")
    private TaskExecutionStatus state;
    /** 任务开始时间 */
    @JsonProperty("startTime")
    private Date startTime;
    /** 任务结束时间 */
    @JsonProperty("endTime")
    private Date endTime;
    /** 执行主机地址 */
    @JsonProperty("host")
    private String host;
    /** 任务日志路径 */
    @JsonProperty("logPath")
    private String logPath;

}
