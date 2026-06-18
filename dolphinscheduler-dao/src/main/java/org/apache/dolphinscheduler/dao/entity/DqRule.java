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
 * 数据质量规则实体，映射到 t_ds_dq_rule 表，定义数据质量检查的规则配置。
 * 规则包含输入参数、检查 SQL、比较方式和期望值等，是数据质量任务的核心配置。
 */
@Data
@TableName("t_ds_dq_rule")
public class DqRule implements Serializable {

    /** 数据质量规则主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 规则名称，用户自定义的标识名 */
    @TableField(value = "name")
    private String name;
    /** 规则类型，区分不同的数据质量检查类别（如表行数检查、空值检查等） */
    @TableField(value = "type")
    private int type;
    /** 非数据库字段：规则 JSON 配置，序列化后的完整规则参数，从 t_ds_dq_rule_input_entry 表拼装填充 */
    @TableField(exist = false)
    private String ruleJson;
    /** 规则创建者用户 ID */
    @TableField(value = "user_id")
    private int userId;
    /** 非数据库字段：创建者用户名，通过 userId 关联 t_ds_user 表查询填充 */
    @TableField(exist = false)
    private String userName;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
}
