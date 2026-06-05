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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 参数选项类，对应 form-create JSON规则中的 options 字段。
 * <p>
 * 用于设置单选、下拉选择、复选框等组件的候选项，
 * 包含选项的显示标签、实际值和是否可选标记。
 */
public class ParamsOptions {

    /** 选项显示标签 */
    private String label;

    /** 选项实际值 */
    private Object value;

    /** 是否禁用（不可选） */
    private boolean disabled;

    public ParamsOptions() {}

    public ParamsOptions(String label, Object value, boolean disabled) {
        this.label = label;
        this.value = value;
        this.disabled = disabled;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public ParamsOptions setLabel(String label) {
        this.label = label;
        return this;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    public ParamsOptions setValue(Object value) {
        this.value = value;
        return this;
    }

    @JsonProperty("disabled")
    public boolean isDisabled() {
        return disabled;
    }

    public ParamsOptions setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
