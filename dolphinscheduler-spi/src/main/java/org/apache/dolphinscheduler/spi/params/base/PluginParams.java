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

import static java.util.Objects.requireNonNull;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_EMIT;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_FIELD;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_NAME;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_PROPS;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_TITLE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_TYPE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_VALIDATE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_VALUE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * 插件参数类，定义了前端动态表单中单个表单项的完整描述信息。
 * <p>
 * 包含字段名、显示标题、表单控件类型、初始值、校验规则、属性配置等。
 * 使用Builder模式构建，支持JSON序列化/反序列化，用于前后端之间的参数传递。
 */
@JsonDeserialize(builder = PluginParams.Builder.class)
public class PluginParams {

    /** 参数字段名（前端使用的key） */
    @JsonProperty(STRING_PLUGIN_PARAM_FIELD)
    protected String name;

    /** 参数名称（显示用） */
    @JsonProperty(STRING_PLUGIN_PARAM_NAME)
    protected String fieldName;

    /** 控件属性配置 */
    @JsonProperty(STRING_PLUGIN_PARAM_PROPS)
    protected ParamsProps props;

    /** 表单控件类型 */
    @JsonProperty(STRING_PLUGIN_PARAM_TYPE)
    protected String formType;

    /** 页面显示的标题 */
    @JsonProperty(STRING_PLUGIN_PARAM_TITLE)
    protected String title;

    /** 提示信息 */
    protected String info;

    /** 默认值或用户在页面输入的值 */
    @JsonProperty(STRING_PLUGIN_PARAM_VALUE)
    protected Object value;

    /** 校验规则列表 */
    @JsonProperty(STRING_PLUGIN_PARAM_VALIDATE)
    protected List<Validate> validateList;

    /** 依赖触发的字段名列表 */
    @JsonProperty(STRING_PLUGIN_PARAM_EMIT)
    protected List<String> emit;

    /** 是否隐藏，默认false */
    protected Boolean hidden;

    /** 是否显示，默认true */
    protected Boolean display;

    protected PluginParams(Builder builder) {

        requireNonNull(builder, "builder is null");
        requireNonNull(builder.name, "name is null");
        requireNonNull(builder.formType, "formType is null");
        requireNonNull(builder.title, "title is null");

        this.name = builder.name;
        this.formType = builder.formType.getFormType();
        this.title = builder.title;

        if (null == builder.props) {
            builder.props = new ParamsProps();
        }
        this.fieldName = builder.title;
        this.props = builder.props;
        this.value = builder.value;
        this.validateList = builder.validateList;
        this.info = builder.info;
        this.display = builder.display;
        this.hidden = builder.hidden;
        this.emit = builder.emit;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    public static class Builder {
        //Must have
        protected String name;

        protected FormType formType;

        protected String title;

        protected String fieldName;

        //option params
        protected ParamsProps props;

        protected Object value;

        protected String info;

        protected List<Validate> validateList;

        protected List<String> emit;

        protected Boolean hidden;

        protected Boolean display;

        public Builder(String name,
                       FormType formType,
                       String title) {
            requireNonNull(name, "name is null");
            requireNonNull(formType, "formType is null");
            requireNonNull(title, "title is null");
            this.name = name;
            this.formType = formType;
            this.title = title;
            this.fieldName = title;
        }

        //for json deserialize to POJO
        @JsonCreator
        public Builder(@JsonProperty("field") String name,
                       @JsonProperty("type") FormType formType,
                       @JsonProperty("title") String title,
                       @JsonProperty("props") ParamsProps props,
                       @JsonProperty("value") Object value,
                       @JsonProperty("name") String fieldName,
                       @JsonProperty("validate") List<Validate> validateList,
                       @JsonProperty("emit") List<String> emit,
                       @JsonProperty("info") String info,
                       @JsonProperty("hidden") Boolean hidden,
                       @JsonProperty("display") Boolean display
        ) {
            requireNonNull(name, "name is null");
            requireNonNull(formType, "formType is null");
            requireNonNull(title, "title is null");
            this.name = name;
            this.formType = formType;
            this.title = title;
            this.props = props;
            this.value = value;
            this.validateList = validateList;
            this.fieldName = fieldName;
            this.emit = emit;
            this.info = info;
            this.hidden = hidden;
            this.display = display;
        }

        public PluginParams build() {
            return new PluginParams(this);
        }
    }

    public String getName() {
        return name;
    }

    public ParamsProps getProps() {
        return props;
    }

    public String getFormType() {
        return formType;
    }

    public String getTitle() {
        return title;
    }

    public Object getValue() {
        return value;
    }

    public List<Validate> getValidateList() {
        return validateList;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<String> getEmit() {
        return emit;
    }

}


