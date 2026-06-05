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

package org.apache.dolphinscheduler.server.master.dispatch.context;

import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_WORKER_GROUP;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;

import lombok.Data;

/**
 * 执行上下文。封装任务分发所需的命令、主机、执行器类型和工作组等信息。
 */
@Data
public class ExecutionContext {

    /**
     * 目标主机。
     */
    private Host host;

    /**
     * 待执行的命令。
     */
    private final Command command;

    /** 任务实例。 */
    private final TaskInstance taskInstance;

    /**
     * 执行器类型：WORKER 或 CLIENT。
     */
    private final ExecutorType executorType;

    /**
     * Worker 分组。
     */
    private final String workerGroup;

    public ExecutionContext(Command command, ExecutorType executorType, TaskInstance taskInstance) {
        this(command, executorType, DEFAULT_WORKER_GROUP, taskInstance);
    }

    public ExecutionContext(Command command, ExecutorType executorType, String workerGroup, TaskInstance taskInstance) {
        this.command = command;
        this.executorType = executorType;
        this.workerGroup = workerGroup;
        this.taskInstance = taskInstance;
    }
}
