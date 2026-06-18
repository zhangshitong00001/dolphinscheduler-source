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
import org.apache.dolphinscheduler.remote.command.BaseCommand;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

/**
 * 消息发送器接口。定义Worker向Master发送各类消息的统一契约，支持消息的构建与发送。
 * 不同类型的消息（如任务执行结果、执行状态、拒绝通知等）由各自实现类处理。
 */
public interface MessageSender<T extends BaseCommand> {

    /**
     * 发送消息。将构建好的消息通过Netty通道发送给目标Master节点。
     *
     * @param message 待发送的消息命令
     * @throws RemotingException 无法连接到目标主机时抛出
     */
    void sendMessage(T message) throws RemotingException;

    /**
     * 根据任务上下文和消息接收方地址构建消息对象。将任务执行上下文中的关键信息封装为可传输的消息命令。
     *
     * @param taskExecutionContext 任务执行上下文，包含任务状态、日志路径、应用ID等信息
     * @param messageReceiverAddress 消息接收方（Master）的地址
     * @return 构建好的消息命令对象
     */
    T buildMessage(TaskExecutionContext taskExecutionContext, String messageReceiverAddress);

    /**
     * 获取此发送器支持的消息类型。每种MessageSender实现对应一种CommandType。
     *
     * @return 该发送器对应的命令类型
     */
    CommandType getMessageType();
}
