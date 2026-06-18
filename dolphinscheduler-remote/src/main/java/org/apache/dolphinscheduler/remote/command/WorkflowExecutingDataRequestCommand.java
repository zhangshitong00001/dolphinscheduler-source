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

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流执行数据请求命令，由API服务发送给Master，用于查询工作流实例的执行数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutingDataRequestCommand implements Serializable {

    /** 流程实例ID */
    private Integer processInstanceId;

    /**
     * 将当前命令对象打包为通用的Command对象。
     *
     * @return command 打包后的命令对象
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.WORKFLOW_EXECUTING_DATA_REQUEST);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }
}
