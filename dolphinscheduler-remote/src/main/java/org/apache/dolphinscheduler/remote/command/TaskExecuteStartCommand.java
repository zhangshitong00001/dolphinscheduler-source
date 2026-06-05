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

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 任务执行启动命令，由API服务发送给Master，用于手动启动一个任务执行。
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteStartCommand extends BaseCommand {

    /** 执行者ID */
    private int executorId;

    /** 执行者名称 */
    private String executorName;

    /** 项目编码 */
    private long projectCode;

    /** 任务定义编码 */
    private long taskDefinitionCode;

    /** 任务定义版本号 */
    private int taskDefinitionVersion;

    /** 告警组ID */
    private int warningGroupId;

    /** Worker分组 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;

    /** 启动参数 */
    private Map<String, String> startParams;

    /** 租户ID */
    private int tenantId;

    /** 是否试运行 */
    private int dryRun;

    public TaskExecuteStartCommand(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        super(messageSenderAddress, messageReceiverAddress, messageSendTime);
    }

    /**
     * 将当前命令对象打包为通用的Command对象。
     *
     * @return command 打包后的命令对象
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.TASK_EXECUTE_START);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
