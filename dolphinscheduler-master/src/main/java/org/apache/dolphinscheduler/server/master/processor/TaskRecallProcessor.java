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
import org.apache.dolphinscheduler.remote.command.TaskRejectCommand;
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
 * 任务召回处理器。接收Worker返回的任务拒绝/召回命令，将其转换为召回事件并添加到任务事件服务中重新调度。
 */
@Component
public class TaskRecallProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskRecallProcessor.class);

    @Autowired
    private TaskEventService taskEventService;

    /**
     * 处理任务召回命令。解析Worker拒绝命令并创建召回事件，添加到任务事件队列等待重新调度。
     *
     * @param channel Netty通道
     * @param command 任务拒绝命令
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_REJECT == command.getType(), String.format("invalid command type : %s", command.getType()));
        TaskRejectCommand recallCommand = JSONUtils.parseObject(command.getBody(), TaskRejectCommand.class);
        TaskEvent taskEvent = TaskEvent.newRecallEvent(recallCommand, channel);
        try {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(recallCommand.getProcessInstanceId(), recallCommand.getTaskInstanceId());
            logger.info("Receive task recall command: {}", recallCommand);
            taskEventService.addEvent(taskEvent);
        } finally {
            LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }
}
