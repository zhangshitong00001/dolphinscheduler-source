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

package org.apache.dolphinscheduler.spi.params.input;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;
import org.apache.dolphinscheduler.spi.params.base.ResizeType;

/**
 * 前端输入框组件的属性配置类，继承自 {@link ParamsProps}。
 * <p>
 * 定义输入框组件的所有可配置属性，包括输入类型、最大/最小长度、
 * 是否可清除、图标、行数、自适应高度、自动完成、只读模式、
 * 数字范围、缩放方式、自动聚焦等。
 */
public class InputParamProps extends ParamsProps {

    /** 输入框类型（text、password、textarea等） */
    private String type;

    /** 最大输入长度 */
    private Integer maxlength;

    /** 最小输入长度 */
    private Integer minlength;

    /** 是否可清空，默认false */
    private Boolean clearable;

    /** 输入框头部图标 */
    private String prefixIcon;

    /** 输入框尾部图标 */
    private String suffixIcon;

    /** 输入框文本行数，仅在type="textarea"时有效 */
    private Integer rows;

    /** 自适应内容高度，仅在type="textarea"时有效，可传入{minRows: 2, maxRows: 6}等对象 */
    private Object autosize;

    /** 自动完成属性：on/off */
    private String autocomplete;

    /** name属性 */
    private String name;

    /** 是否只读，默认false */
    private Boolean readonly;

    /** 最大值 */
    private Integer max;

    /** 最小值 */
    private Integer min;

    /** 设置输入字段的合法数字间隔步长 */
    private Integer step;

    /** 控制是否可由用户缩放，值为none、both、horizontal、vertical */
    private ResizeType resize;

    /** 是否自动获取焦点，默认false */
    private Boolean autofocus;

    /** 表单标识 */
    private String form;

    /** 与输入框关联的label文本 */
    private String label;

    /** 输入框的tabindex */
    private String tabindex;

    /** 输入时是否触发表单校验，默认true */
    private Boolean validateEvent;

    /** 是否显示密码切换图标 */
    private Boolean showPassword;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(Integer maxlength) {
        this.maxlength = maxlength;
    }

    public Integer getMinlength() {
        return minlength;
    }

    public void setMinlength(Integer minlength) {
        this.minlength = minlength;
    }

    public Boolean getClearable() {
        return clearable;
    }

    public void setClearable(Boolean clearable) {
        this.clearable = clearable;
    }

    public String getPrefixIcon() {
        return prefixIcon;
    }

    public void setPrefixIcon(String prefixIcon) {
        this.prefixIcon = prefixIcon;
    }

    public String getSuffixIcon() {
        return suffixIcon;
    }

    public void setSuffixIcon(String suffixIcon) {
        this.suffixIcon = suffixIcon;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Object getAutosize() {
        return autosize;
    }

    public void setAutosize(Object autosize) {
        this.autosize = autosize;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public ResizeType getResize() {
        return resize;
    }

    public void setResize(ResizeType resize) {
        this.resize = resize;
    }

    public Boolean getAutofocus() {
        return autofocus;
    }

    public void setAutofocus(Boolean autofocus) {
        this.autofocus = autofocus;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTabindex() {
        return tabindex;
    }

    public void setTabindex(String tabindex) {
        this.tabindex = tabindex;
    }

    public Boolean getValidateEvent() {
        return validateEvent;
    }

    public void setValidateEvent(Boolean validateEvent) {
        this.validateEvent = validateEvent;
    }

    public Boolean getShowPassword() {
        return showPassword;
    }

    public void setShowPassword(Boolean showPassword) {
        this.showPassword = showPassword;
    }
}
