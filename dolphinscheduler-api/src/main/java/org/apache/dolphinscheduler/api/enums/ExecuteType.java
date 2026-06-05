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

package org.apache.dolphinscheduler.api.enums;

/**
 * 工作流执行类型枚举。定义工作流实例的各种操作类型，如重复运行、恢复暂停、恢复失败、停止和暂停等。
 */
public enum ExecuteType {

    /** 无操作 */
    NONE,
    /** 重复运行 */
    REPEAT_RUNNING,
    /** 恢复暂停的流程 */
    RECOVER_SUSPENDED_PROCESS,
    /** 启动失败任务流程 */
    START_FAILURE_TASK_PROCESS,
    /** 停止 */
    STOP,
    /** 暂停 */
    PAUSE;

    /**
     * 根据序号获取对应的执行类型
     * @param value 枚举序号
     * @return 对应的ExecuteType枚举值，未找到则返回null
     */
    public static ExecuteType getEnum(int value) {
        for (ExecuteType e: ExecuteType.values()) {
            if (e.ordinal() == value) {
                return e;
            }
        }
        return null;
    }
}
