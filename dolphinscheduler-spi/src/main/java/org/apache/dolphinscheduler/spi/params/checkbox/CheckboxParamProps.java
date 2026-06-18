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

package org.apache.dolphinscheduler.spi.params.checkbox;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * 前端复选框组件的属性配置类，继承自 {@link ParamsProps}。
 * <p>
 * 定义复选框组件特有的属性：最少/最多可选数量、激活态文本颜色和填充色等。
 */
public class CheckboxParamProps extends ParamsProps {

    /** 最少可选中复选框的数量 */
    private Integer min;

    /** 最多可选中复选框的数量 */
    private Integer max;

    /** 按钮形式复选框激活时的文本颜色 */
    private String textColor;

    /** 按钮形式复选框激活时的填充色和边框颜色 */
    private String fill;

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }
}
