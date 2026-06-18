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
 * 状态事件类型枚举。
 * 定义工作流和任务状态流转中触发的事件类型。
 */
public enum StateEventType {

    /** 流程状态变更 */
    PROCESS_STATE_CHANGE(0, "process state change"),
    /** 任务状态变更 */
    TASK_STATE_CHANGE(1, "task state change"),
    /** 流程超时 */
    PROCESS_TIMEOUT(2, "process timeout"),
    /** 任务超时 */
    TASK_TIMEOUT(3, "task timeout"),
    /** 唤醒任务组 */
    WAKE_UP_TASK_GROUP(4, "wait task group"),
    /** 任务重试 */
    TASK_RETRY(5, "task retry"),
    /** 流程阻塞 */
    PROCESS_BLOCKED(6, "process blocked"),
    /** 流程提交失败 */
    PROCESS_SUBMIT_FAILED(7, "process submit failed");

    StateEventType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
