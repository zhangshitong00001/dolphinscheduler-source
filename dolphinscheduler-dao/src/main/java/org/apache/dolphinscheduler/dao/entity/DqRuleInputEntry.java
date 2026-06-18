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

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 数据质量规则输入项实体，映射到 t_ds_dq_rule_input_entry 表，定义数据质量规则的表单输入字段配置。
 * 每个输入项对应规则配置表单中的一个字段，包含字段名、标题、类型、默认值、校验规则等，用于动态渲染前端表单和收集用户输入。
 */
@Data
@TableName("t_ds_dq_rule_input_entry")
public class DqRuleInputEntry implements Serializable {

    /** 输入项主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 表单字段名，前端表单中的字段标识 */
    @TableField(value = "field")
    private String field;
    /** 表单控件类型，如 Input、Select、InputNumber 等 */
    @TableField(value = "type")
    private String type;
    /** 表单字段标题，前端显示的标签文本 */
    @TableField(value = "title")
    private String title;
    /** 默认值，可为空 */
    @TableField(value = "value")
    private String value;
    /** 可选项列表，JSON 数组格式：[{label:"",value:""}]，供 Select 等组件使用 */
    @TableField(value = "options")
    private String options;
    /** 占位符提示文本 */
    @TableField(value = "placeholder")
    private String placeholder;
    /** 选项来源类型：DEFAULT（使用默认选项）、OTHERS（其他来源） */
    @TableField(value = "option_source_type")
    private int optionSourceType = OptionSourceType.DEFAULT.getCode();
    /** 值类型：STRING、ARRAY、NUMBER 等 */
    @TableField(value = "value_type")
    private int valueType = ValueType.NUMBER.getCode();
    /** 输入项类型：DEFAULT（默认）、STATISTICS（统计）、COMPARISON（比较） */
    @TableField(value = "input_type")
    private int inputType = InputType.DEFAULT.getCode();
    /** 是否在前端显示 */
    @TableField(value = "is_show")
    private Boolean isShow;
    /** 是否在前端可编辑 */
    @TableField(value = "can_edit")
    private Boolean canEdit;
    /** 是否触发事件（表单联动） */
    @TableField(value = "is_emit")
    private Boolean isEmit;
    /** 是否需要校验 */
    @TableField(value = "is_validate")
    private Boolean isValidate;
    /** 非数据库字段：值映射 JSON 字符串 */
    @TableField(exist = false)
    private String valuesMap;

    /** 非数据库字段：排序索引，用于前端表单字段排序 */
    @TableField(exist = false)
    private Integer index;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
}
