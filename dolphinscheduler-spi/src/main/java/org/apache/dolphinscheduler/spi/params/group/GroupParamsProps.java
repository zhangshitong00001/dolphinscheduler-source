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

package org.apache.dolphinscheduler.spi.params.group;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分组组件的属性配置类，继承自 {@link ParamsProps}。
 * <p>
 * 定义分组组件的子控件列表（rules）和字体大小等属性。
 * 通过 rules 可以将多个表单控件组织在一个分组容器内。
 */
public class GroupParamsProps extends ParamsProps {

    /** 分组内的子控件规则列表 */
    private List<PluginParams> rules;

    /** 字体大小 */
    private int fontSize;

    @JsonProperty("rules")
    public List<PluginParams> getRules() {
        return rules;
    }

    public GroupParamsProps setRules(List<PluginParams> rules) {
        this.rules = rules;
        return this;
    }

    @JsonProperty("fontSize")
    public int getFontSize() {
        return fontSize;
    }

    public GroupParamsProps setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }
}
