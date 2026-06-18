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

package org.apache.dolphinscheduler.spi.params.base;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Functions;

/**
 * 表单控件类型枚举，定义了前端动态表单支持的所有输入控件类型。
 * <p>
 * 这些类型对应 form-create 库中的组件类型，用于在前端页面上渲染
 * 不同类型的表单输入控件（文本框、数字框、单选框、下拉框等）。
 */
public enum FormType {

    /** 文本输入框 */
    INPUT("input"),
    /** 数字输入框 */
    INPUTNUMBER("inputNumber"),
    /** 单选框 */
    RADIO("radio"),
    /** 下拉选择框 */
    SELECT("select"),
    /** 开关 */
    SWITCH("switch"),
    /** 复选框 */
    CHECKBOX("checkbox"),
    /** 时间选择器 */
    TIMEPICKER("timePicker"),
    /** 日期选择器 */
    DATEPICKER("datePicker"),
    /** 滑块 */
    SLIDER("slider"),
    /** 评分 */
    RATE("rate"),
    /** 颜色选择器 */
    COLORPICKER("colorPicker"),
    /** 级联选择器 */
    CASCADER("cascader"),
    /** 文件上传 */
    UPLOAD("upload"),
    /** 穿梭框 */
    ELTRANSFER("el-transfer"),
    /** 树形控件 */
    TREE("tree"),
    /** 多行文本框 */
    TEXTAREA("textarea"),
    /** 分组 */
    GROUP("group");

    private String formType;

    FormType(String formType) {
        this.formType = formType;
    }

    @JsonValue
    public String getFormType() {
        return this.formType;
    }

    private static final Map<String, FormType> FORM_TYPE_MAP =
            Arrays.stream(FormType.values()).collect(toMap(FormType::getFormType, Functions.identity()));

    public static FormType of(String type) {
        if (FORM_TYPE_MAP.containsKey(type)) {
            return FORM_TYPE_MAP.get(type);
        }
        return null;
    }
}
