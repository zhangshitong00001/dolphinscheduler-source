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
package org.apache.dolphinscheduler.remote.processor;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.command.Command;

/**
 * Netty请求处理器接口。定义命令处理的标准契约，所有业务处理器需实现此接口。
 */
public interface NettyRequestProcessor {

    /**
     * 处理接收到的命令。实现类在此方法中编写具体的业务处理逻辑。
     *
     * @param channel channel
     * @param command command
     */
    void process(final Channel channel, final Command command);
}
