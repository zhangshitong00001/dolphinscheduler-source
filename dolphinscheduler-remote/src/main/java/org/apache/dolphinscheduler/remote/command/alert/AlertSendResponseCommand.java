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

package org.apache.dolphinscheduler.remote.command.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import java.io.Serializable;
import java.util.List;

/**
 * 告警发送响应命令。封装告警发送的整体响应结果，包含成功状态（全部成功则为true，任一失败则为false）和各个告警通道的详细结果列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertSendResponseCommand implements Serializable {

    /**
     * 告警发送整体结果：true表示所有告警通道均成功，false表示至少有一个告警通道失败
     */
    private boolean success;

    /** 各告警通道的响应结果列表 */
    private List<AlertSendResponseResult> resResults;

    /**
     * package response command
     *
     * @param opaque request unique identification
     * @return command
     */
    public Command convert2Command(long opaque) {
        Command command = new Command(opaque);
        command.setType(CommandType.ALERT_SEND_RESPONSE);
        byte[] body = JsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
