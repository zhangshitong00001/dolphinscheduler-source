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

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * 任务运行状态命令，表示任务正在Worker节点上运行，由Worker发送给Master。
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteRunningCommand extends BaseCommand {

    /** 任务实例ID */
    private int taskInstanceId;

    /** 流程实例ID */
    private int processInstanceId;

    /** 开始时间 */
    private Date startTime;

    /** 执行主机 */
    private String host;

    /** 任务执行状态 */
    private TaskExecutionStatus status;

    /** 日志路径 */
    private String logPath;

    /** 执行路径 */
    private String executePath;

    /** 进程ID */
    private int processId;

    /** 应用ID列表 */
    private String appIds;

    public TaskExecuteRunningCommand(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        super(messageSenderAddress, messageReceiverAddress, messageSendTime);
    }

    /**
     * 将当前命令对象打包为通用的Command对象。
     *
     * @return command 打包后的命令对象
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.TASK_EXECUTE_RUNNING);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
