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
 * 数据质量执行结果实体，映射到 t_ds_dq_execute_result 表，记录每次数据质量检查任务的执行结果。
 * 包含统计值、比较值、阈值等核心数据质量指标，以及检查是否通过的判定信息。
 */
@Data
@TableName("t_ds_dq_execute_result")
public class DqExecuteResult implements Serializable {

    /** 执行结果主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 关联的工作流定义 ID */
    @TableField(value = "process_definition_id")
    private long processDefinitionId;
    /** 非数据库字段：工作流定义名称，通过 processDefinitionId 关联查询填充 */
    @TableField(exist = false)
    private String processDefinitionName;
    /** 非数据库字段：工作流定义编码，通过 processDefinitionId 关联查询填充 */
    @TableField(exist = false)
    private long processDefinitionCode;
    /** 关联的工作流实例 ID */
    @TableField(value = "process_instance_id")
    private long processInstanceId;
    /** 非数据库字段：工作流实例名称，通过 processInstanceId 关联查询填充 */
    @TableField(exist = false)
    private String processInstanceName;
    /** 非数据库字段：项目编码，通过关联的工作流查询填充 */
    @TableField(exist = false)
    private long projectCode;
    /** 关联的任务实例 ID */
    @TableField(value = "task_instance_id")
    private long taskInstanceId;
    /** 非数据库字段：任务名称，通过 taskInstanceId 关联查询填充 */
    @TableField(exist = false)
    private String taskName;
    /** 数据质量规则类型，对应 T_ds_dq_rule 中的类型 */
    @TableField(value = "rule_type")
    private int ruleType;
    /** 数据质量规则名称 */
    @TableField(value = "rule_name")
    private String ruleName;
    /** 统计值，即执行数据质量 SQL 得到的实际值 */
    @TableField(value = "statistics_value")
    private double statisticsValue;
    /** 比较值，即用于对比的期望值或阈值基准 */
    @TableField(value = "comparison_value")
    private double comparisonValue;
    /** 比较类型 ID，对应 t_ds_dq_comparison_type 表（如等于=1、大于=2、小于=3） */
    @TableField(value = "comparison_type")
    private int comparisonType;
    /** 非数据库字段：比较类型名称，通过 comparisonType 关联 t_ds_dq_comparison_type 查询填充 */
    @TableField(exist = false)
    private String comparisonTypeName;
    /** 检查类型，区分绝对值检查还是增量检查等 */
    @TableField(value = "check_type")
    private int checkType;
    /** 阈值，数据质量检查的临界值 */
    @TableField(value = "threshold")
    private double threshold;
    /** 操作符，定义统计值与阈值的比较逻辑（如大于、小于、等于） */
    @TableField(value = "operator")
    private int operator;
    /** 失败策略，定义质量检查失败时的处理方式（如告警、阻断等） */
    @TableField(value = "failure_strategy")
    private int failureStrategy;
    /** 操作用户 ID */
    @TableField(value = "user_id")
    private int userId;
    /** 非数据库字段：用户名，通过 userId 关联 t_ds_user 表查询填充 */
    @TableField(exist = false)
    private String userName;
    /** 执行结果状态（如成功、失败等） */
    @TableField(value = "state")
    private int state;
    /** 错误输出路径，质量检查失败时错误数据输出的 HDFS 路径 */
    @TableField(value = "error_output_path")
    private String errorOutputPath;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
}
