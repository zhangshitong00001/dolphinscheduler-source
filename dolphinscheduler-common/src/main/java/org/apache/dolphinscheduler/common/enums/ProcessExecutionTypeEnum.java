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

package org.apache.dolphinscheduler.common.enums;

import java.util.HashMap;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 流程执行类型枚举。
 * 定义同一流程定义下多个实例的执行策略，控制并发或串行行为。
 */
public enum ProcessExecutionTypeEnum {
    /** 并行执行 */
    PARALLEL(0, "parallel"),
    /** 串行等待，后续实例排队等待 */
    SERIAL_WAIT(1, "serial wait"),
    /** 串行丢弃，后续实例直接丢弃 */
    SERIAL_DISCARD(2, "serial discard"),
    /** 串行优先级，根据优先级决定执行顺序 */
    SERIAL_PRIORITY(3, "serial priority");

    ProcessExecutionTypeEnum(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    private static HashMap<Integer, ProcessExecutionTypeEnum> EXECUTION_STATUS_MAP = new HashMap<>();

    static {
        for (ProcessExecutionTypeEnum executionType : ProcessExecutionTypeEnum.values()) {
            EXECUTION_STATUS_MAP.put(executionType.code, executionType);
        }
    }

    public boolean typeIsSerial() {
        return this != PARALLEL;
    }

    public boolean typeIsSerialWait() {
        return this == SERIAL_WAIT;
    }

    public boolean typeIsSerialDiscard() {
        return this == SERIAL_DISCARD;
    }

    public boolean typeIsSerialPriority() {
        return this == SERIAL_PRIORITY;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    public static ProcessExecutionTypeEnum of(int executionType) {
        if (EXECUTION_STATUS_MAP.containsKey(executionType)) {
            return EXECUTION_STATUS_MAP.get(executionType);
        }
        throw new IllegalArgumentException("invalid status : " + executionType);
    }

}
