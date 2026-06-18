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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.HostUpdateCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * 主机更新处理器。当Master发生故障转移时，接收新的Master主机地址，
 * 并更新所有待重试消息的目标地址，确保消息能正确发送到新的Master节点。
 */
@Component
public class HostUpdateProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(HostUpdateProcessor.class);

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    /**
     * 处理主机更新命令。解析Master故障转移后发送的主机更新请求，
     * 更新消息重试运行器中对应任务实例的消息目标地址。
     *
     * @param channel Netty通道
     * @param command 主机更新命令
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.PROCESS_HOST_UPDATE_REQUEST == command.getType(),
                                    String.format("invalid command type : %s", command.getType()));
        HostUpdateCommand updateCommand = JSONUtils.parseObject(command.getBody(), HostUpdateCommand.class);
        if (updateCommand == null) {
            logger.error("host update command is null");
            return;
        }
        logger.info("received host update command : {}", updateCommand);
        messageRetryRunner.updateMessageHost(updateCommand.getTaskInstanceId(), updateCommand.getProcessHost());
    }
}
