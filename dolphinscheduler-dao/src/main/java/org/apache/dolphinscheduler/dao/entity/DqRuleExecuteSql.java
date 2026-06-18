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

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 数据质量规则执行 SQL 实体，映射到 t_ds_dq_rule_execute_sql 表，存储数据质量规则中需要执行的 SQL 语句。
 * 一条规则可能包含多条 SQL，通过 index 字段控制执行顺序。SQL 类型分为统计 SQL、比较 SQL 和错误输出 SQL。
 */
@Data
@TableName("t_ds_dq_rule_execute_sql")
public class DqRuleExecuteSql implements Serializable {

    /** 执行 SQL 主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 执行顺序索引，确保多条 SQL 按指定顺序执行 */
    @TableField(value = "index")
    private int index;
    /** SQL 语句内容 */
    @TableField(value = "sql")
    private String sql;
    /** 表别名，用于在生成 SQL 时引用临时表 */
    @TableField(value = "table_alias")
    private String tableAlias;
    /** SQL 类型，枚举值：MIDDLE（中间表）、STATISTICS（统计表）、COMPARISON（比较表）、CHECK（检查表） */
    @TableField(value = "type")
    private int type = ExecuteSqlType.MIDDLE.getCode();
    /** 是否为错误输出 SQL，用于输出质量检查不通过的错误数据 */
    @TableField(value = "is_error_output_sql")
    private boolean isErrorOutputSql;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
}
