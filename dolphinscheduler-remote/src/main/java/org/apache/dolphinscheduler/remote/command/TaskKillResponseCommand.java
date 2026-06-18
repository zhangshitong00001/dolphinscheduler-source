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

package org.apache.dolphinscheduler.remote.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.io.Serializable;
import java.util.List;

/**
 * 任务终止响应命令，用于返回任务终止操作的处理结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskKillResponseCommand implements Serializable {

    /** 任务实例ID */
    private int taskInstanceId;

    /** 执行主机 */
    private String host;

    /** 任务执行状态 */
    private TaskExecutionStatus status;

    /** 进程ID */
    private int processId;

    /** 其他资源管理器应用ID列表，例如YARN等 */
    private List<String> appIds;

    /**
     * 将当前命令对象打包为通用的Command对象。
     *
     * @return command 打包后的命令对象
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.TASK_KILL_RESPONSE);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
