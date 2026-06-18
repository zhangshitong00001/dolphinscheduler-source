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

package org.apache.dolphinscheduler.spi.params.radio;

import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_OPTIONS;
import static org.apache.dolphinscheduler.spi.params.base.FormType.RADIO;

import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 前端单选框组件参数类，继承自 {@link PluginParams}。
 * <p>
 * 用于在前端动态表单中渲染单选框（radio）组件，
 * 通过 {@link ParamsOptions} 定义可选项列表，支持设置选中态的颜色样式等属性。
 */
public class RadioParam extends PluginParams {

    /** 单选框可选项列表 */
    @JsonProperty(STRING_PLUGIN_PARAM_OPTIONS)
    private List<ParamsOptions> options;

    /** 单选框组件专属属性 */
    private RadioParamProps props;

    private RadioParam(Builder builder) {
        super(builder);
        this.options = builder.options;
    }

    public static Builder newBuilder(String name, String title) {
        return new Builder(name, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String name, String title) {
            super(name, RADIO, title);
        }
        
        private List<ParamsOptions> options;

        private RadioParamProps props;
        
        public Builder setOptions(List<ParamsOptions> options) {
            this.options = options;
            return this;
        }

        public Builder addParamsOptions(ParamsOptions paramsOptions) {
            if (this.options == null) {
                this.options = new LinkedList<>();
            }

            this.options.add(paramsOptions);
            return this;
        }

        public Builder setProps(RadioParamProps props) {
            this.props = props;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setInfo(String info) {
            this.info = info;
            return this;
        }

        public Builder setValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder setValidateList(List<Validate> validateList) {
            this.validateList = validateList;
            return this;
        }

        public Builder addValidate(Validate validate) {
            if (this.validateList == null) {
                this.validateList = new LinkedList<>();
            }
            this.validateList.add(validate);
            return this;
        }

        public Builder setHidden(Boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder setDisplay(Boolean display) {
            this.display = display;
            return this;
        }

        @Override
        public RadioParam build() {
            return new RadioParam(this);
        }
    }

    public List<ParamsOptions> getOptions() {
        return options;
    }

    @Override
    public RadioParamProps getProps() {
        return props;
    }
}
