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

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 定时调度实体，映射到 t_ds_schedules 表，表示工作流的一次定时调度配置。
 * 包含 Cron 表达式、生效时间范围、时区、告警配置、Worker 分组等调度参数。
 */
@Data
@TableName("t_ds_schedules")
public class Schedule {

    /** 调度主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 流程定义编码 */
    private long processDefinitionCode;

    /** 非数据库字段：流程定义名称 */
    @TableField(exist = false)
    private String processDefinitionName;

    /** 非数据库字段：项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 非数据库字段：流程定义描述 */
    @TableField(exist = false)
    private String definitionDescription;

    /** 调度开始时间 */
    private Date startTime;

    /** 调度结束时间 */
    private Date endTime;

    /** 时区 ID，参见 {@link java.util.TimeZone#getTimeZone(String)} */
    private String timezoneId;

    /** Cron 表达式 */
    private String crontab;

    /** 失败策略 */
    private FailureStrategy failureStrategy;

    /** 告警类型 */
    private WarningType warningType;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 创建者用户 ID */
    private int userId;

    /** 非数据库字段：创建者用户名 */
    @TableField(exist = false)
    private String userName;

    /** 发布状态 */
    private ReleaseState releaseState;

    /** 告警组 ID */
    private int warningGroupId;

    /** 流程实例优先级 */
    private Priority processInstancePriority;

    /** Worker 分组 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;
}
