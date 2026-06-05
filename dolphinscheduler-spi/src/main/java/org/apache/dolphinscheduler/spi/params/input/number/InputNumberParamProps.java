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

package org.apache.dolphinscheduler.spi.params.input.number;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * 前端数字输入框组件的属性配置类，继承自 {@link ParamsProps}。
 * <p>
 * 定义数字输入框的数值范围、步长、精度、控制按钮等属性。
 */
public class InputNumberParamProps extends ParamsProps {

    /** 计数器允许的最小值 */
    private Integer min;

    /** 计数器允许的最大值 */
    private Integer max;

    /** 计数器步长 */
    private Integer step;

    /** 数值精度（小数位数） */
    private Integer precision;

    /** 是否使用控制按钮，默认true */
    private Boolean controls;

    /** 控制按钮位置，默认right */
    private String controlsPosition;

    /** name属性 */
    private String name;

    /** 与输入框关联的label文本 */
    private String label;

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

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Boolean getControls() {
        return controls;
    }

    public void setControls(Boolean controls) {
        this.controls = controls;
    }

    public String getControlsPosition() {
        return controlsPosition;
    }

    public void setControlsPosition(String controlsPosition) {
        this.controlsPosition = controlsPosition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
