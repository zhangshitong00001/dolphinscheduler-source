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

package org.apache.dolphinscheduler.spi.params.fswitch;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * 前端开关组件的属性配置类，继承自 {@link ParamsProps}。
 * <p>
 * 定义开关组件的所有可配置属性，包括开关宽度、开启/关闭状态的
 * 图标、文本、取值和颜色等。
 */
public class SwitchParamProps extends ParamsProps {

    /** 开关宽度（像素） */
    private Integer width;

    /** 开关打开时显示的图标类名，设置此项会忽略 activeText */
    private String activeIconClass;

    /** 开关关闭时显示的图标类名，设置此项会忽略 inactiveText */
    private String inactiveIconClass;

    /** 开关打开时的文本描述 */
    private String activeText;

    /** 开关关闭时的文本描述 */
    private String inactiveText;

    /** 开关打开时的值 */
    private Object activeValue;

    /** 开关关闭时的值 */
    private Object inactiveValue;

    /** 开关打开时的背景色 */
    private String activeColor;

    /** 开关关闭时的背景色 */
    private String inactiveColor;

    /** name属性 */
    private String name;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getActiveIconClass() {
        return activeIconClass;
    }

    public void setActiveIconClass(String activeIconClass) {
        this.activeIconClass = activeIconClass;
    }

    public String getInactiveIconClass() {
        return inactiveIconClass;
    }

    public void setInactiveIconClass(String inactiveIconClass) {
        this.inactiveIconClass = inactiveIconClass;
    }

    public String getActiveText() {
        return activeText;
    }

    public void setActiveText(String activeText) {
        this.activeText = activeText;
    }

    public String getInactiveText() {
        return inactiveText;
    }

    public void setInactiveText(String inactiveText) {
        this.inactiveText = inactiveText;
    }

    public Object getActiveValue() {
        return activeValue;
    }

    public void setActiveValue(Object activeValue) {
        this.activeValue = activeValue;
    }

    public Object getInactiveValue() {
        return inactiveValue;
    }

    public void setInactiveValue(Object inactiveValue) {
        this.inactiveValue = inactiveValue;
    }

    public String getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(String activeColor) {
        this.activeColor = activeColor;
    }

    public String getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(String inactiveColor) {
        this.inactiveColor = inactiveColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
