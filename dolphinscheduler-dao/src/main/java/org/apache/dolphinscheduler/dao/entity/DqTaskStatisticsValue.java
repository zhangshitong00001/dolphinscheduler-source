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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 数据质量任务统计值实体，映射到 t_ds_dq_task_statistics_value 表，记录数据质量任务执行时采集到的统计数值。
 * 每次数据质量检查会生成一个或多个统计值（如行数、空值数、重复数等），用于与阈值比较来判断数据质量是否达标。
 */
@Data
@TableName("t_ds_dq_task_statistics_value")
public class DqTaskStatisticsValue implements Serializable {

    /** 统计值主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 关联的工作流定义 ID */
    @TableField(value = "process_definition_id")
    private long processDefinitionId;
    /** 非数据库字段：工作流定义名称，通过 processDefinitionId 关联查询填充 */
    @TableField(exist = false)
    private String processDefinitionName;
    /** 关联的任务实例 ID */
    @TableField(value = "task_instance_id")
    private long taskInstanceId;
    /** 非数据库字段：任务名称，通过 taskInstanceId 关联查询填充 */
    @TableField(exist = false)
    private String taskName;
    /** 关联的规则 ID，对应 t_ds_dq_rule 表的 id */
    @TableField(value = "rule_id")
    private long ruleId;
    /** 非数据库字段：规则类型，通过 ruleId 关联查询填充 */
    @TableField(exist = false)
    private int ruleType;
    /** 非数据库字段：规则名称，通过 ruleId 关联查询填充 */
    @TableField(exist = false)
    private String ruleName;
    /** 统计值，SQL 执行得到的实际数值（如行数为 1000） */
    @TableField(value = "statistics_value")
    private double statisticsValue;
    /** 统计指标名称，描述该统计值的含义（如 "row_count"、"null_count"） */
    @TableField(value = "statistics_name")
    private String statisticsName;
    /** 数据时间，统计值对应的业务数据时间 */
    @TableField(value = "data_time")
    private Date dataTime;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
}
