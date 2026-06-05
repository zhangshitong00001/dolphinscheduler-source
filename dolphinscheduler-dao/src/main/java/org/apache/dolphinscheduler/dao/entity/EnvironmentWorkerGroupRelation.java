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

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 环境-工作组关联实体，映射到 t_ds_environment_worker_group_relation 表，表示环境与工作组之间的绑定关系。
 * 通过此关联表指定某个环境下的任务应该在哪个工作组执行，实现环境级别的资源调度控制。
 */
@Data
@TableName("t_ds_environment_worker_group_relation")
public class EnvironmentWorkerGroupRelation {

    /** 关联关系主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 环境编码，对应 t_ds_environment 表的 code */
    private Long environmentCode;

    /** 工作组名称，对应 t_ds_worker_group 表的 name */
    private String workerGroup;

    /** 操作者用户 ID */
    private Integer operator;

    /** 创建时间 */
    private Date createTime;

    /** 最后更新时间 */
    private Date updateTime;
}
