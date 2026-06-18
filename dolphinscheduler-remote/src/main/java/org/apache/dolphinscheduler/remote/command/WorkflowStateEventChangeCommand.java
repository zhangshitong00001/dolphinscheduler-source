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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;

/**
 * 工作流状态事件变更命令，用于通知工作流实例状态发生变更的事件。
 */
@Data
@NoArgsConstructor
public class WorkflowStateEventChangeCommand implements Serializable {

    /** 事件键 */
    private String key;

    /** 源状态 */
    private WorkflowExecutionStatus sourceStatus;

    /** 源流程实例ID */
    private int sourceProcessInstanceId;

    /** 源任务实例ID */
    private int sourceTaskInstanceId;

    /** 目标流程实例ID */
    private int destProcessInstanceId;

    /** 目标任务实例ID */
    private int destTaskInstanceId;

    public WorkflowStateEventChangeCommand(int sourceProcessInstanceId,
                                           int sourceTaskInstanceId,
                                           WorkflowExecutionStatus sourceStatus,
                                           int destProcessInstanceId,
                                           int destTaskInstanceId) {
        this.key = String.format("%d-%d-%d-%d",
                sourceProcessInstanceId,
                sourceTaskInstanceId,
                destProcessInstanceId,
                destTaskInstanceId);

        this.sourceStatus = sourceStatus;
        this.sourceProcessInstanceId = sourceProcessInstanceId;
        this.sourceTaskInstanceId = sourceTaskInstanceId;
        this.destProcessInstanceId = destProcessInstanceId;
        this.destTaskInstanceId = destTaskInstanceId;
    }

    /**
     * 将当前命令对象打包为通用的Command对象。
     *
     * @return command 打包后的命令对象
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.STATE_EVENT_REQUEST);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
