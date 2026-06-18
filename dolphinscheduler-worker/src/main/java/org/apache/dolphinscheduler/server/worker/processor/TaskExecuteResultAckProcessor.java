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

package org.apache.dolphinscheduler.server.worker.processor;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务执行结果确认处理器。处理Master返回的任务执行结果确认消息。
 * 当Master成功接收并处理任务执行结果后，Worker收到此确认，从重试队列中移除对应消息，
 * 避免不必要的重试。如果确认状态为失败，则保留消息等待后续重试。
 */
@Component
public class TaskExecuteResultAckProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskExecuteResultAckProcessor.class);

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    /**
     * 处理任务执行结果确认命令。解析Master返回的确认消息，如果确认成功则移除重试消息，
     * 否则保留消息以便后续重试发送。
     *
     * @param channel Netty通道
     * @param command 任务执行结果确认命令
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_RESULT_ACK == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskExecuteAckCommand taskExecuteAckMessage = JSONUtils.parseObject(command.getBody(),
                TaskExecuteAckCommand.class);

        if (taskExecuteAckMessage == null) {
            logger.error("task execute response ack command is null");
            return;
        }

        try {
            LoggerUtils.setTaskInstanceIdMDC(taskExecuteAckMessage.getTaskInstanceId());
            logger.info("Receive task execute response ack command : {}", taskExecuteAckMessage);
            if (taskExecuteAckMessage.isSuccess()) {
                messageRetryRunner.removeRetryMessage(taskExecuteAckMessage.getTaskInstanceId(),
                        CommandType.TASK_EXECUTE_RESULT);
                logger.debug("remove REMOTE_CHANNELS, task instance id:{}", taskExecuteAckMessage.getTaskInstanceId());
            } else {
                // master handle worker response error, will still retry
                logger.error("Receive task execute result ack message, the message status is not success, message: {}",
                        taskExecuteAckMessage);
            }
        } finally {
            LoggerUtils.removeTaskInstanceIdMDC();

        }
    }

}
