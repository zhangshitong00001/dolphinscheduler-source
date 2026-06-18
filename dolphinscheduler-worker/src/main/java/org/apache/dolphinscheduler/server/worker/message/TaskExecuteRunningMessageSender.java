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

package org.apache.dolphinscheduler.server.worker.message;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningCommand;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务执行运行中消息发送器。负责在任务开始执行时，构建任务运行状态消息并发送给Master，
 * 通知Master该任务已开始运行，包含任务实例ID、执行状态、日志路径、主机信息和应用ID等。
 */
@Component
public class TaskExecuteRunningMessageSender implements MessageSender<TaskExecuteRunningCommand> {

    @Autowired
    private WorkerRpcClient workerRpcClient;

    @Autowired
    private WorkerConfig workerConfig;

    /**
     * 发送任务执行运行中消息。通过RPC客户端将消息发送到指定的Master地址。
     *
     * @param message 任务执行运行中消息
     * @throws RemotingException 无法连接到目标主机时抛出
     */
    @Override
    public void sendMessage(TaskExecuteRunningCommand message) throws RemotingException {
        workerRpcClient.send(Host.of(message.getMessageReceiverAddress()), message.convert2Command());
    }

    /**
     * 构建任务执行运行中消息。从任务执行上下文中提取任务实例ID、流程实例ID、执行状态、
     * 日志路径、主机地址、开始时间、执行路径和应用ID等信息，封装为运行状态消息命令。
     *
     * @param taskExecutionContext 任务执行上下文
     * @param messageReceiverAddress 消息接收方（Master）的地址
     * @return 任务执行运行中消息命令
     */
    public TaskExecuteRunningCommand buildMessage(@NonNull TaskExecutionContext taskExecutionContext,
                                                  @NonNull String messageReceiverAddress) {
        TaskExecuteRunningCommand taskExecuteRunningMessage =
                new TaskExecuteRunningCommand(workerConfig.getWorkerAddress(),
                        messageReceiverAddress,
                        System.currentTimeMillis());
        taskExecuteRunningMessage.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecuteRunningMessage.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskExecuteRunningMessage.setStatus(taskExecutionContext.getCurrentExecutionStatus());
        taskExecuteRunningMessage.setLogPath(taskExecutionContext.getLogPath());
        taskExecuteRunningMessage.setHost(taskExecutionContext.getHost());
        taskExecuteRunningMessage.setStartTime(taskExecutionContext.getStartTime());
        taskExecuteRunningMessage.setExecutePath(taskExecutionContext.getExecutePath());
        taskExecuteRunningMessage.setAppIds(taskExecutionContext.getAppIds());
        return taskExecuteRunningMessage;
    }

    /**
     * 获取此发送器对应的命令类型。
     *
     * @return TASK_EXECUTE_RUNNING 命令类型
     */
    @Override
    public CommandType getMessageType() {
        return CommandType.TASK_EXECUTE_RUNNING;
    }
}
