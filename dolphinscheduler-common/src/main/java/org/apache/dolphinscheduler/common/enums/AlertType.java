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

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 告警类型枚举。
 * 描述触发告警的原因（流程失败/成功/阻塞/超时，任务失败/成功/超时，容错告警，关闭告警等）。
 */
public enum AlertType {
    /** 流程实例失败告警（0） */
    PROCESS_INSTANCE_FAILURE(0, "process instance failure"),
    /** 流程实例成功告警（1） */
    PROCESS_INSTANCE_SUCCESS(1, "process instance success"),
    /** 流程实例阻塞告警（2） */
    PROCESS_INSTANCE_BLOCKED(2, "process instance blocked"),
    /** 流程实例超时告警（3） */
    PROCESS_INSTANCE_TIMEOUT(3, "process instance timeout"),
    /** 容错告警（4） */
    FAULT_TOLERANCE_WARNING(4, "fault tolerance warning"),
    /** 任务失败告警（5） */
    TASK_FAILURE(5, "task failure"),
    /** 任务成功告警（6） */
    TASK_SUCCESS(6, "task success"),
    /** 任务超时告警（7） */
    TASK_TIMEOUT(7, "task timeout"),
    /** 关闭告警：流程成功后关闭先前告警（8） */
    CLOSE_ALERT(8, "the process instance success, can close the before alert")
    ;

    AlertType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    /** MyBatis-Plus枚举值映射：告警类型码 */
    @EnumValue
    private final int code;
    /** 告警类型描述 */
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
