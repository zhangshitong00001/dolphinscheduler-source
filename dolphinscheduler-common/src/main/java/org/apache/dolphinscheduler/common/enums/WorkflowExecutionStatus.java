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
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流执行状态枚举。
 * 定义工作流实例从提交到结束的完整生命周期状态。
 * 该类从 ExecutionStatus 拆分而来（#11339），为兼容旧值，状态码不连续。
 */
public enum WorkflowExecutionStatus {
    // This class is split from <code>ExecutionStatus</code> #11339.
    // In order to compatible with the old value, the code is not consecutive
    /** 提交成功 */
    SUBMITTED_SUCCESS(0, "submit success"),
    /** 正在运行 */
    RUNNING_EXECUTION(1, "running"),
    /** 准备暂停 */
    READY_PAUSE(2, "ready pause"),
    /** 已暂停 */
    PAUSE(3, "pause"),
    /** 准备停止 */
    READY_STOP(4, "ready stop"),
    /** 已停止 */
    STOP(5, "stop"),
    /** 失败 */
    FAILURE(6, "failure"),
    /** 成功 */
    SUCCESS(7, "success"),
    /** 延迟执行 */
    DELAY_EXECUTION(12, "delay execution"),
    /** 串行等待 */
    SERIAL_WAIT(14, "serial wait"),
    /** 准备阻塞 */
    READY_BLOCK(15, "ready block"),
    /** 已阻塞 */
    BLOCK(16, "block"),
    ;

    private static final Map<Integer, WorkflowExecutionStatus> CODE_MAP = new HashMap<>();
    private static final int[] NEED_FAILOVER_STATES = new int[]{
            SUBMITTED_SUCCESS.getCode(),
            RUNNING_EXECUTION.getCode(),
            DELAY_EXECUTION.getCode(),
            READY_PAUSE.getCode(),
            READY_STOP.getCode()
    };

    static {
        for (WorkflowExecutionStatus executionStatus : WorkflowExecutionStatus.values()) {
            CODE_MAP.put(executionStatus.getCode(), executionStatus);
        }
    }

    /**
     * Get <code>WorkflowExecutionStatus</code> by code, if the code is invalidated will throw {@link IllegalArgumentException}.
     */
    public static @NonNull WorkflowExecutionStatus of(int code) {
        WorkflowExecutionStatus workflowExecutionStatus = CODE_MAP.get(code);
        if (workflowExecutionStatus == null) {
            throw new IllegalArgumentException(String.format("The workflow execution status code: %s is invalidated",
                    code));
        }
        return workflowExecutionStatus;
    }

    public boolean isRunning() {
        return this == RUNNING_EXECUTION;
    }

    public boolean canStop() {
        return this == RUNNING_EXECUTION || this == READY_PAUSE;
    }

    public boolean isFinished() {
        // todo: do we need to remove pause/block in finished judge?
        return isSuccess() || isFailure() || isStop() || isPause() || isBlock();
    }

    /**
     * status is success
     *
     * @return status
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == FAILURE;
    }

    public boolean isPause() {
        return this == PAUSE;
    }

    public boolean isReadyStop() {
        return this == READY_STOP;
    }

    public boolean isStop() {
        return this == STOP;
    }

    public boolean isBlock() {
        return this == BLOCK;
    }

    public static int[] getNeedFailoverWorkflowInstanceState() {
        return NEED_FAILOVER_STATES;
    }

    @EnumValue
    private final int code;

    private final String desc;

    WorkflowExecutionStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "WorkflowExecutionStatus{" + "code=" + code + ", desc='" + desc + '\'' + '}';
    }
}
