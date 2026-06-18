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

/**
 * 告警发送请求命令。封装告警发送请求的数据传输对象，包含告警分组ID、标题、内容和告警类型，用于跨节点告警发送通信。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertSendRequestCommand implements Serializable {

    /** 告警分组ID */
    private int groupId;

    /** 告警标题 */
    private String title;

    /** 告警内容 */
    private String content;

    /** 告警类型 */
    private int warnType;

    /**
     * package request command
     *
     * @return command
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.ALERT_SEND_REQUEST);
        byte[] body = JsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
