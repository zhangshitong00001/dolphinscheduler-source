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

package org.apache.dolphinscheduler.spi.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令类型枚举，定义工作流执行过程中可以下发的各类操作命令。
 * <p>
 * 每种命令类型对应一个整数编码（code），用于在Master和Worker之间传递指令，
 * 控制工作流的不同执行模式（启动、恢复、暂停、停止、补数等）。
 */
public enum CommandType {

    /** 启动一个新的工作流实例 */
    START_PROCESS(0, "start a new process"),
    /** 从当前节点启动一个新的工作流实例 */
    START_CURRENT_TASK_PROCESS(1, "start a new process from current nodes"),
    /** 恢复容错的工作流实例 */
    RECOVER_TOLERANCE_FAULT_PROCESS(2, "recover tolerance fault process"),
    /** 恢复被挂起的工作流实例 */
    RECOVER_SUSPENDED_PROCESS(3, "recover suspended process"),
    /** 从失败的任务节点重新启动工作流 */
    START_FAILURE_TASK_PROCESS(4, "start process from failure task nodes"),
    /** 补数（补充历史数据） */
    COMPLEMENT_DATA(5, "complement data"),
    /** 由调度器触发的启动 */
    SCHEDULER(6, "start a new process from scheduler"),
    /** 重复运行工作流 */
    REPEAT_RUNNING(7, "repeat running a process"),
    /** 暂停工作流 */
    PAUSE(8, "pause a process"),
    /** 停止工作流 */
    STOP(9, "stop a process"),
    /** 恢复等待中的线程 */
    RECOVER_WAITING_THREAD(10, "recover waiting thread"),
    /** 恢复串行等待 */
    RECOVER_SERIAL_WAIT(11, "recover serial wait");

    CommandType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    private static final Map<Integer, CommandType> COMMAND_TYPE_MAP = new HashMap<>();

    static {
        for (CommandType commandType : CommandType.values()) {
            COMMAND_TYPE_MAP.put(commandType.code,commandType);
        }
    }

    public static CommandType of(Integer status) {
        if (COMMAND_TYPE_MAP.containsKey(status)) {
            return COMMAND_TYPE_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }
}
