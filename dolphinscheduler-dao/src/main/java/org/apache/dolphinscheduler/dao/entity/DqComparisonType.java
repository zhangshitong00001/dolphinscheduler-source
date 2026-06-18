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
 * 数据质量比较类型实体，映射到 t_ds_dq_comparison_type 表，定义数据质量检查的比较方式（如等于、大于、小于等）。
 * 每种比较类型包含对应的执行 SQL 模板和输出表名，用于在数据质量任务中动态生成比较 SQL。
 */
@Data
@TableName("t_ds_dq_comparison_type")
public class DqComparisonType implements Serializable {

    /** 比较类型主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 比较类型标识，如 "==", ">", "<", ">=", "<=" 等 */
    @TableField(value = "type")
    private String type;
    /** 执行 SQL 模板，用于动态生成数据质量比较的 SQL 语句 */
    @TableField(value = "execute_sql")
    private String executeSql;
    /** 输出表名，比较结果输出的目标表 */
    @TableField(value = "output_table")
    private String outputTable;
    /** 比较类型名称，用于前端展示，如 "等于"、"大于"、"小于" */
    @TableField(value = "name")
    private String name;
    /** 是否为内置比较类型，内置类型由系统预设，用户不可修改 */
    @TableField(value = "is_inner_source")
    private Boolean isInnerSource;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
}
