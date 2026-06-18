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

import org.apache.dolphinscheduler.common.enums.ReleaseState;

import java.util.Date;
import java.util.Map;

import lombok.Data;

/**
 * 任务主要信息，用于任务列表展示和上下游依赖查询。
 */
@Data
public class TaskMainInfo {

    /** 任务定义主键 ID */
    private long id;

    /** 任务名称 */
    private String taskName;

    /** 任务编码 */
    private long taskCode;

    /** 任务版本号 */
    private int taskVersion;

    /** 任务类型 */
    private String taskType;

    /** 任务创建时间 */
    private Date taskCreateTime;

    /** 任务更新时间 */
    private Date taskUpdateTime;

    /** 流程定义编码 */
    private long processDefinitionCode;

    /** 流程定义版本号 */
    private int processDefinitionVersion;

    /** 流程定义名称 */
    private String processDefinitionName;

    /** 流程发布状态 */
    private ReleaseState processReleaseState;

    /** 上游任务映射表（key: 编码，value: 名称） */
    private Map<Long, String> upstreamTaskMap;

    /** 上游任务编码 */
    private long upstreamTaskCode;

    /** 上游任务名称 */
    private String upstreamTaskName;
}
