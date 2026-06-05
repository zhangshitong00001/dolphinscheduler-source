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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;

import lombok.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务回调实现类，负责在任务执行过程中向Master更新远程应用信息。
 * 当任务产生新的ApplicationInfo时，通过WorkerMessageSender发送运行状态更新到Master。
 */
@Builder
public class TaskCallbackImpl implements TaskCallBack {

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, TaskCallbackImpl.class));

    private final WorkerMessageSender workerMessageSender;

    private final String masterAddress;

    public TaskCallbackImpl(WorkerMessageSender workerMessageSender, String masterAddress) {
        this.workerMessageSender = workerMessageSender;
        this.masterAddress = masterAddress;
    }

    /**
     * 更新远程应用信息并通知Master，将ApplicationInfo中的appIds设置到任务执行上下文中。
     *
     * @param taskInstanceId 任务实例ID
     * @param applicationInfo 应用信息，包含appIds
     */
    @Override
    public void updateRemoteApplicationInfo(int taskInstanceId, ApplicationInfo applicationInfo) {
        TaskExecutionContext taskExecutionContext =
                TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            logger.error("task execution context is empty, taskInstanceId: {}, applicationInfo:{}", taskInstanceId,
                    applicationInfo);
            return;
        }

        logger.info("send remote application info {}", applicationInfo);
        taskExecutionContext.setAppIds(applicationInfo.getAppIds());
        workerMessageSender.sendMessageWithRetry(taskExecutionContext, masterAddress, CommandType.TASK_EXECUTE_RUNNING);
    }
}
