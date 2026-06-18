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

package org.apache.dolphinscheduler.spi.params.select;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * 前端下拉选择框组件的属性配置类，继承自 {@link ParamsProps}。
 * <p>
 * 定义下拉选择框的完整属性配置，包括多选、可清空、可搜索、
 * 可创建新条目、最大可选数、过滤与远程搜索等丰富配置。
 */
public class SelectParamProps extends ParamsProps {

    /** 是否多选，默认false */
    private Boolean multiple;

    /** 唯一标识值的键名，绑定值为对象类型时必填 */
    private String valueKey;

    /** 选择框尺寸，可选值medium/small/mini */
    private String size;

    /** 选项是否可清空，默认false */
    private Boolean clearable;

    /** 多选时是否将选中值以文字形式展示，默认false */
    private Boolean collapseTags;

    /** 多选时用户最多可选择的项目数，为0则不限制 */
    private Integer multipleLimit;

    /** select输入框的name属性 */
    private String name;

    /** select输入框的autocomplete属性，默认off */
    private String autocomplete;

    /** 是否可搜索，默认false */
    private Boolean filterable;

    /** 是否允许用户创建新条目，需与filterable配合使用，默认false */
    private Boolean allowCreate;

    /** 搜索条件无匹配时显示的文本 */
    private String noMatchText;

    /** 选项为空时显示的文本 */
    private String noDataText;

    /** 下拉框的CSS类名 */
    private String popperClass;

    /** 多选且可搜索时，选中选项后是否保留当前搜索关键词，默认false */
    private Boolean reserveKeyword;

    /** 在输入框中按回车选择第一个匹配项，需与filterable或remote配合使用，默认false */
    private Boolean defaultFirstOption;

    /** 是否将弹出框插入到body元素中，弹出框定位有问题时可设为false */
    private Boolean popperAppendToBody;

    /** 对于非可搜索的Select，输入框获取焦点后是否自动弹出选项菜单，默认false */
    private Boolean automaticDropdown;

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public String getValueKey() {
        return valueKey;
    }

    public void setValueKey(String valueKey) {
        this.valueKey = valueKey;
    }

    public Boolean getClearable() {
        return clearable;
    }

    public void setClearable(Boolean clearable) {
        this.clearable = clearable;
    }

    public Boolean getCollapseTags() {
        return collapseTags;
    }

    public void setCollapseTags(Boolean collapseTags) {
        this.collapseTags = collapseTags;
    }

    public Integer getMultipleLimit() {
        return multipleLimit;
    }

    public void setMultipleLimit(Integer multipleLimit) {
        this.multipleLimit = multipleLimit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    public Boolean getAllowCreate() {
        return allowCreate;
    }

    public void setAllowCreate(Boolean allowCreate) {
        this.allowCreate = allowCreate;
    }

    public String getNoMatchText() {
        return noMatchText;
    }

    public void setNoMatchText(String noMatchText) {
        this.noMatchText = noMatchText;
    }

    public String getNoDataText() {
        return noDataText;
    }

    public void setNoDataText(String noDataText) {
        this.noDataText = noDataText;
    }

    public String getPopperClass() {
        return popperClass;
    }

    public void setPopperClass(String popperClass) {
        this.popperClass = popperClass;
    }

    public Boolean getReserveKeyword() {
        return reserveKeyword;
    }

    public void setReserveKeyword(Boolean reserveKeyword) {
        this.reserveKeyword = reserveKeyword;
    }

    public Boolean getDefaultFirstOption() {
        return defaultFirstOption;
    }

    public void setDefaultFirstOption(Boolean defaultFirstOption) {
        this.defaultFirstOption = defaultFirstOption;
    }

    public Boolean getPopperAppendToBody() {
        return popperAppendToBody;
    }

    public void setPopperAppendToBody(Boolean popperAppendToBody) {
        this.popperAppendToBody = popperAppendToBody;
    }

    public Boolean getAutomaticDropdown() {
        return automaticDropdown;
    }

    public void setAutomaticDropdown(Boolean automaticDropdown) {
        this.automaticDropdown = automaticDropdown;
    }
}
