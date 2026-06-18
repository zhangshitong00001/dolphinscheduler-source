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
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * 任务运行中状态处理器。接收Worker返回的任务运行中状态命令，将其转换为运行中事件并添加到任务事件服务中。
 */
@Component
public class TaskExecuteRunningProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskExecuteRunningProcessor.class);

    @Autowired
    private TaskEventService taskEventService;

    /**
     * 处理任务运行中状态命令。解析命令并创建运行中事件，添加到任务事件队列。
     *
     * @param channel Netty通道
     * @param command 任务执行运行中命令
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_RUNNING == command.getType(), String.format("invalid command type : %s", command.getType()));
        TaskExecuteRunningCommand taskExecuteRunningMessage = JSONUtils.parseObject(command.getBody(), TaskExecuteRunningCommand.class);
        logger.info("taskExecuteRunningCommand: {}", taskExecuteRunningMessage);

        TaskEvent taskEvent = TaskEvent.newRunningEvent(taskExecuteRunningMessage,
                                                        channel,
                                                        taskExecuteRunningMessage.getMessageSenderAddress());
        taskEventService.addEvent(taskEvent);
    }

}
