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

import lombok.NonNull;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.remote.command.BaseCommand;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;

import org.apache.commons.collections.MapUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息重试运行器。作为守护线程运行，负责管理Worker向Master发送消息失败时的重试机制。
 * 维护需要重试的消息映射表，定期扫描并重新发送超时未确认的消息。
 */
@Component
public class MessageRetryRunner extends BaseDaemonThread {

    private final Logger logger = LoggerFactory.getLogger(MessageRetryRunner.class);

    protected MessageRetryRunner() {
        super("WorkerMessageRetryRunnerThread");
    }

    private static long MESSAGE_RETRY_WINDOW = Duration.ofMinutes(5L).toMillis();

    @Lazy
    @Autowired
    private List<MessageSender> messageSenders;

    private Map<CommandType, MessageSender<BaseCommand>> messageSenderMap = new HashMap<>();

    private Map<Integer, Map<CommandType, BaseCommand>> needToRetryMessages = new ConcurrentHashMap<>();

    /**
     * 启动消息重试运行器。初始化所有MessageSender，将其注册到消息发送器映射表中，然后启动守护线程。
     */
    @Override
    public synchronized void start() {
        logger.info("Message retry runner staring");
        messageSenders.forEach(messageSender -> {
            messageSenderMap.put(messageSender.getMessageType(), messageSender);
            logger.info("Injected message sender: {}", messageSender.getClass().getName());
        });
        super.start();
        logger.info("Message retry runner started");
    }

    /**
     * 添加需要重试的消息。将指定的消息添加到重试队列中，按任务实例ID和消息类型进行分组管理。
     *
     * @param taskInstanceId 任务实例ID
     * @param messageType 消息类型
     * @param baseCommand 待重试的消息命令
     */
    public void addRetryMessage(int taskInstanceId, @NonNull CommandType messageType, BaseCommand baseCommand) {
        needToRetryMessages.computeIfAbsent(taskInstanceId, k -> new ConcurrentHashMap<>()).put(messageType,
                baseCommand);
    }

    /**
     * 移除指定类型的重试消息。当Master确认收到消息后，从重试队列中移除对应类型的消息。
     *
     * @param taskInstanceId 任务实例ID
     * @param messageType 要移除的消息类型
     */
    public void removeRetryMessage(int taskInstanceId, @NonNull CommandType messageType) {
        Map<CommandType, BaseCommand> retryMessages = needToRetryMessages.get(taskInstanceId);
        if (retryMessages != null) {
            retryMessages.remove(messageType);
        }
    }

    /**
     * 移除指定任务实例的所有重试消息。当任务完成或终止后，清理该任务的所有待重试消息。
     *
     * @param taskInstanceId 任务实例ID
     */
    public void removeRetryMessages(int taskInstanceId) {
        needToRetryMessages.remove(taskInstanceId);
    }

    /**
     * 更新消息的目标主机地址。当Master发生故障转移时，更新所有待重试消息的接收方地址。
     *
     * @param taskInstanceId 任务实例ID
     * @param messageReceiverHost 新的消息接收方主机地址
     */
    public void updateMessageHost(int taskInstanceId, String messageReceiverHost) {
        Map<CommandType, BaseCommand> needToRetryMessages = this.needToRetryMessages.get(taskInstanceId);
        if (needToRetryMessages != null) {
            needToRetryMessages.values().forEach(baseMessage -> {
                baseMessage.setMessageReceiverAddress(messageReceiverHost);
            });
        }
    }

    /**
     * 守护线程主循环。定期扫描待重试消息队列，对超过重试窗口时间的消息进行重新发送。
     * 在服务停止前持续运行，直到线程被中断。
     */
    public void run() {
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                if (MapUtils.isEmpty(needToRetryMessages)) {
                    Thread.sleep(MESSAGE_RETRY_WINDOW);
                }

                long now = System.currentTimeMillis();
                for (Map.Entry<Integer, Map<CommandType, BaseCommand>> taskEntry : needToRetryMessages.entrySet()) {
                    Integer taskInstanceId = taskEntry.getKey();
                    LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
                    try {
                        for (Map.Entry<CommandType, BaseCommand> messageEntry : taskEntry.getValue().entrySet()) {
                            CommandType messageType = messageEntry.getKey();
                            BaseCommand message = messageEntry.getValue();
                            if (now - message.getMessageSendTime() > MESSAGE_RETRY_WINDOW) {
                                logger.info("Begin retry send message to master, message: {}", message);
                                message.setMessageSendTime(now);
                                messageSenderMap.get(messageType).sendMessage(message);
                                logger.info("Success send message to master, message: {}", message);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Retry send message to master error", e);
                    } finally {
                        LoggerUtils.removeTaskInstanceIdMDC();
                    }
                }
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (InterruptedException instance) {
                logger.warn("The message retry thread is interrupted, will break this loop", instance);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                logger.error("Retry send message failed, get an known exception.", ex);
            }
        }
    }

    /**
     * 清空所有待重试消息。移除重试队列中的所有消息记录。
     */
    public void clearMessage() {
        needToRetryMessages.clear();
    }
}
