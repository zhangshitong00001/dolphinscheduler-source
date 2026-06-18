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
import org.apache.dolphinscheduler.remote.command.TaskRejectCommand;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务拒绝消息发送器。当Worker线程池已满无法接收新任务时，负责构建任务拒绝消息并发送给Master，
 * 通知Master该任务需要重新调度到其他Worker执行。
 */
@Component
public class TaskRejectMessageSender implements MessageSender<TaskRejectCommand> {

    @Autowired
    private WorkerRpcClient workerRpcClient;

    @Autowired
    private WorkerConfig workerConfig;

    /**
     * 发送任务拒绝消息。通过RPC客户端将消息发送到指定的Master地址。
     *
     * @param message 任务拒绝消息
     * @throws RemotingException 无法连接到目标主机时抛出
     */
    @Override
    public void sendMessage(TaskRejectCommand message) throws RemotingException {
        workerRpcClient.send(Host.of(message.getMessageReceiverAddress()), message.convert2Command());
    }

    /**
     * 构建任务拒绝消息。从任务执行上下文中提取任务实例ID、流程实例ID和主机信息，
     * 封装为任务拒绝消息命令发送给Master。
     *
     * @param taskExecutionContext 任务执行上下文
     * @param masterAddress Master节点地址
     * @return 任务拒绝消息命令
     */
    public TaskRejectCommand buildMessage(TaskExecutionContext taskExecutionContext, String masterAddress) {
        TaskRejectCommand taskRejectMessage = new TaskRejectCommand(workerConfig.getWorkerAddress(),
                                                                    masterAddress,
                                                                    System.currentTimeMillis());
        taskRejectMessage.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskRejectMessage.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskRejectMessage.setHost(taskExecutionContext.getHost());
        return taskRejectMessage;
    }

    /**
     * 获取此发送器对应的命令类型。
     *
     * @return TASK_REJECT 命令类型
     */
    @Override
    public CommandType getMessageType() {
        return CommandType.TASK_REJECT;
    }
}
