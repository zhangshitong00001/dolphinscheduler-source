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

package org.apache.dolphinscheduler.remote.command;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RPC消息基类。采用异步通信模式，客户端发送消息后等待目标服务器的确认（ack），若超时未收到确认则进行重试。当发生网络错误导致客户端关闭通道时，服务器需要通过消息发送者地址回传确认信息，因此需要记录消息的源地址和目标地址。
 */
@Data
@NoArgsConstructor
public abstract class BaseCommand implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 消息发送者的地址。当消息接收方需要向发送方回复确认（ack）时使用。
     */
    protected String messageSenderAddress;

    /**
     * 消息接收者的地址。
     */
    protected String messageReceiverAddress;

    /** 消息发送时间戳 */
    protected long messageSendTime;

    protected BaseCommand(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        this.messageSenderAddress = messageSenderAddress;
        this.messageReceiverAddress = messageReceiverAddress;
        this.messageSendTime = messageSendTime;
    }
}
