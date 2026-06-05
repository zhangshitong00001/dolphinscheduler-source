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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * 表单校验规则类，定义前端表单输入项的校验约束。
 * <p>
 * 支持必填校验、数据类型校验、最小/最大值约束以及校验触发时机等。
 * 使用Builder模式构建，支持JSON序列化/反序列化。
 */
@JsonDeserialize(builder = Validate.Builder.class)
public class Validate {

    /** 是否必填 */
    @JsonProperty("required")
    private boolean required;

    /** 校验失败时的提示消息 */
    @JsonProperty("message")
    private String message;

    /** 数据类型（string/number） */
    @JsonProperty("type")
    private String type;

    /** 校验触发时机（blur/change） */
    @JsonProperty("trigger")
    private String trigger;

    /** 最小值 */
    @JsonProperty("min")
    private Double min;

    /** 最大值 */
    @JsonProperty("max")
    private Double max;

    private Validate() {

    }

    private Validate(Builder builder) {
        this.required = builder.required;
        this.message = builder.message;
        this.type = builder.type;
        this.trigger = builder.trigger;
        this.min = builder.min;
        this.max = builder.max;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    public static class Builder {
        private boolean required = false;

        private String message;

        private String type = DataType.STRING.getDataType();

        private String trigger = TriggerType.BLUR.getTriggerType();

        private Double min;

        private Double max;

        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setTrigger(String trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder setMin(Double min) {
            this.min = min;
            return this;
        }

        public Builder setMax(Double max) {
            this.max = max;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Validate build() {
            return new Validate(this);
        }
    }

    public boolean isRequired() {
        return required;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getTrigger() {
        return trigger;
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }
}
