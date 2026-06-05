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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import lombok.experimental.UtilityClass;

/**
 * 服务层常量定义类，定义工作流和任务执行过程中的状态码数组，用于判断流程和任务的运行状态。
 */
@UtilityClass
public final class Constants {

    /** 未终止状态码集合，包含已提交、已派发、运行中、延迟执行、准备暂停、准备停止、需容错等状态。 */
    public static final int[] NOT_TERMINATED_STATES = new int[]{
            WorkflowExecutionStatus.SUBMITTED_SUCCESS.getCode(),
            TaskExecutionStatus.DISPATCH.getCode(),
            WorkflowExecutionStatus.RUNNING_EXECUTION.getCode(),
            WorkflowExecutionStatus.DELAY_EXECUTION.getCode(),
            WorkflowExecutionStatus.READY_PAUSE.getCode(),
            WorkflowExecutionStatus.READY_STOP.getCode(),
            TaskExecutionStatus.NEED_FAULT_TOLERANCE.getCode(),
    };

    /** 运行中的流程状态码集合，包含运行中、已提交成功、已派发、串行等待等状态。 */
    public static final int[] RUNNING_PROCESS_STATE = new int[]{
            TaskExecutionStatus.RUNNING_EXECUTION.getCode(),
            TaskExecutionStatus.SUBMITTED_SUCCESS.getCode(),
            TaskExecutionStatus.DISPATCH.getCode(),
            WorkflowExecutionStatus.SERIAL_WAIT.getCode()
    };
}
