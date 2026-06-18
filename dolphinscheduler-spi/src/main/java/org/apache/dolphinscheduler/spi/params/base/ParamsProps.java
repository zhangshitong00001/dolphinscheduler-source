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
 * 参数属性类，对应 form-create JSON规则中的 props 字段。
 * <p>
 * 用于配置前端表单控件的样式和行为属性，
 * 如占位符文本、控件大小、是否禁用等。
 */
public class ParamsProps {

    /** 输入框占位符文本 */
    private String placeholder;

    /** 输入框尺寸，可选值 medium/small/mini */
    private String size = "small";

    /** 是否禁用，默认false */
    private Boolean disabled;

    public void setSize(String size) {
        this.size = size;
    }

    @JsonProperty("size")
    public String getSize() {
        return size;
    }

    @JsonProperty("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public ParamsProps setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
