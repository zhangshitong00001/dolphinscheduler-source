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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.BaseCommand;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.message.MessageSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * Worker消息发送器，负责向Master发送任务执行状态等消息。
 * 支持普通发送和带重试的发送两种模式，根据消息类型路由到对应的MessageSender实现。
 */
@Component
public class WorkerMessageSender {

    private final Logger logger = LoggerFactory.getLogger(WorkerMessageSender.class);

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Autowired
    private List<MessageSender> messageSenders;

    private Map<CommandType, MessageSender> messageSenderMap = new HashMap<>();

    /**
     * 初始化消息发送器映射表，将CommandType与对应的MessageSender关联。
     */
    @PostConstruct
    public void init() {
        messageSenders.forEach(messageSender -> messageSenderMap.put(messageSender.getMessageType(),
                messageSender));
    }

    /**
     * 发送消息并支持失败重试，消息会被加入重试队列。
     *
     * @param taskExecutionContext 任务执行上下文
     * @param messageReceiverAddress 消息接收方地址
     * @param messageType 消息类型
     */
    // todo: use message rather than context
    public void sendMessageWithRetry(@NonNull TaskExecutionContext taskExecutionContext,
                                     @NonNull String messageReceiverAddress,
                                     @NonNull CommandType messageType) {
        MessageSender messageSender = messageSenderMap.get(messageType);
        if (messageSender == null) {
            throw new IllegalArgumentException("The messageType is invalidated, messageType: " + messageType);
        }
        BaseCommand baseCommand = messageSender.buildMessage(taskExecutionContext, messageReceiverAddress);
        try {
            messageRetryRunner.addRetryMessage(taskExecutionContext.getTaskInstanceId(), messageType, baseCommand);
            messageSender.sendMessage(baseCommand);
        } catch (RemotingException e) {
            logger.error("Send message error, messageType: {}, message: {}", messageType, baseCommand);
        }
    }

    /**
     * 发送消息，不包含重试机制，失败仅记录日志。
     *
     * @param taskExecutionContext 任务执行上下文
     * @param messageReceiverAddress 消息接收方地址
     * @param messageType 消息类型
     */
    public void sendMessage(@NonNull TaskExecutionContext taskExecutionContext,
                            @NonNull String messageReceiverAddress,
                            @NonNull CommandType messageType) {
        MessageSender messageSender = messageSenderMap.get(messageType);
        if (messageSender == null) {
            throw new IllegalArgumentException("The messageType is invalidated, messageType: " + messageType);
        }
        BaseCommand baseCommand = messageSender.buildMessage(taskExecutionContext, messageReceiverAddress);
        try {
            messageSender.sendMessage(baseCommand);
        } catch (RemotingException e) {
            logger.error("Send message error, messageType: {}, message: {}", messageType, baseCommand);
        }
    }

}
