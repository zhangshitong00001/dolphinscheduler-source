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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResultCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * 任务执行响应处理器。接收Worker返回的任务执行结果命令，将其转换为结果事件并添加到任务事件服务中。
 */
@Component
public class TaskExecuteResponseProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskExecuteResponseProcessor.class);

    @Autowired
    private TaskEventService taskEventService;

    /**
     * 处理任务执行结果响应。解析任务执行结果命令，创建结果事件并添加到任务事件队列中。
     *
     * @param channel Netty通道
     * @param command 任务执行结果命令
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_RESULT == command.getType(),
                                    String.format("invalid command type : %s", command.getType()));

        TaskExecuteResultCommand taskExecuteResultMessage = JSONUtils.parseObject(command.getBody(),
                                                                                  TaskExecuteResultCommand.class);
        TaskEvent taskResultEvent = TaskEvent.newResultEvent(taskExecuteResultMessage,
                                                             channel,
                                                             taskExecuteResultMessage.getMessageSenderAddress());
        try {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskResultEvent.getProcessInstanceId(),
                                                        taskResultEvent.getTaskInstanceId());
            logger.info("Received task execute result, event: {}", taskResultEvent);

            taskEventService.addEvent(taskResultEvent);
        } finally {
            LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
