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

import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 任务组队列实体，映射到 t_ds_task_group_queue 表，管理任务组中的任务排队和执行顺序。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_task_group_queue")
public class TaskGroupQueue implements Serializable {

    /** 队列记录主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 任务实例 ID */
    private int taskId;
    /** 任务实例名称 */
    private String taskName;
    /** 非数据库字段：项目名称 */
    @TableField(exist = false)
    private String projectName;
    /** 非数据库字段：项目编码 */
    @TableField(exist = false)
    private String projectCode;
    /** 非数据库字段：流程实例名称 */
    @TableField(exist = false)
    private String processInstanceName;
    /** 任务组 ID */
    private int groupId;
    /** 流程实例 ID */
    private int processId;
    /** 任务实例优先级 */
    private int priority;
    /** 是否强制启动：0 否，1 是 */
    private int forceStart;
    /** 是否在队列中等待：0 否，1 是 */
    private int inQueue;
    /** 队列状态：-1 等待中，1 运行中，2 已完成 */
    private TaskGroupQueueStatus status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
