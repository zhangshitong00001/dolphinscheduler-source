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

package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * 任务实例的唯一标识键，用于在状态轮询线程的检查列表中标识特定的任务实例。
 * 由工作流实例 ID、任务编码和任务版本号组成。
 */
@Data
@AllArgsConstructor
public class TaskInstanceKey {
    private final int processInstanceId;
    private final long taskCode;
    private final int taskVersion;

    /**
     * 从工作流实例和任务实例构造 TaskInstanceKey。
     *
     * @param processInstance 工作流实例
     * @param taskInstance    任务实例
     * @return TaskInstanceKey
     */
    public static TaskInstanceKey getTaskInstanceKey(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        return new TaskInstanceKey(processInstance.getId(), taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
    }

}
