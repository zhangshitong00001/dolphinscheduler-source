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

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 任务组实体，映射到 t_ds_task_group 表，用于管理任务组的容量和并发控制。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_ds_task_group")
public class TaskGroup implements Serializable {

    /** 任务组主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 任务组名称 */
    private String name;

    /** 任务组描述 */
    private String description;
    /** 任务组容量大小 */
    private int groupSize;
    /** 已使用的任务组容量 */
    private int useSize;
    /** 创建者用户 ID */
    private int userId;
    /** 状态：0 不可用，1 可用 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    /** 所属项目编码 */
    private long projectCode;

}
