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
import org.apache.dolphinscheduler.remote.command.TaskExecuteResultCommand;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务执行结果消息发送器。负责构建任务执行结果消息，并通过RPC客户端将任务完成后的状态、
 * 输出日志路径、应用ID、变量池等结果信息发送给Master节点。
 */
@Component
public class TaskExecuteResultMessageSender implements MessageSender<TaskExecuteResultCommand> {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private WorkerRpcClient workerRpcClient;

    /**
     * 发送任务执行结果消息。通过RPC客户端将消息发送到指定的Master地址。
     *
     * @param message 任务执行结果消息
     * @throws RemotingException 无法连接到目标主机时抛出
     */
    @Override
    public void sendMessage(TaskExecuteResultCommand message) throws RemotingException {
        workerRpcClient.send(Host.of(message.getMessageReceiverAddress()), message.convert2Command());
    }

    /**
     * 构建任务执行结果消息。从任务执行上下文中提取流程实例ID、任务实例ID、执行状态、
     * 日志路径、应用ID、进程ID、主机地址、起止时间和变量池等信息，封装为结果消息命令。
     *
     * @param taskExecutionContext 任务执行上下文
     * @param messageReceiverAddress 消息接收方（Master）的地址
     * @return 任务执行结果消息命令
     */
    public TaskExecuteResultCommand buildMessage(TaskExecutionContext taskExecutionContext,
                                                 String messageReceiverAddress) {
        TaskExecuteResultCommand taskExecuteResultMessage
            = new TaskExecuteResultCommand(workerConfig.getWorkerAddress(),
                                           messageReceiverAddress,
                                           System.currentTimeMillis());
        taskExecuteResultMessage.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskExecuteResultMessage.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecuteResultMessage.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
        taskExecuteResultMessage.setLogPath(taskExecutionContext.getLogPath());
        taskExecuteResultMessage.setExecutePath(taskExecutionContext.getExecutePath());
        taskExecuteResultMessage.setAppIds(taskExecutionContext.getAppIds());
        taskExecuteResultMessage.setProcessId(taskExecutionContext.getProcessId());
        taskExecuteResultMessage.setHost(taskExecutionContext.getHost());
        taskExecuteResultMessage.setStartTime(taskExecutionContext.getStartTime());
        taskExecuteResultMessage.setEndTime(taskExecutionContext.getEndTime());
        taskExecuteResultMessage.setVarPool(taskExecutionContext.getVarPool());
        taskExecuteResultMessage.setExecutePath(taskExecutionContext.getExecutePath());
        return taskExecuteResultMessage;
    }

    /**
     * 获取此发送器对应的命令类型。
     *
     * @return TASK_EXECUTE_RESULT 命令类型
     */
    @Override
    public CommandType getMessageType() {
        return CommandType.TASK_EXECUTE_RESULT;
    }
}
