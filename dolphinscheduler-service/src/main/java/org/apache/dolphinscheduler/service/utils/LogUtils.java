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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.service.log.TaskLogDiscriminator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

/**
 * 日志路径工具类，用于根据任务上下文信息构建任务日志文件的存储路径。
 * 日志路径格式为：基础日志目录/日期/流程定义编码_版本-流程实例ID-任务实例ID.log
 */
public class LogUtils {

    public static final String LOG_TAILFIX = ".log";

    private LogUtils() throws IllegalStateException {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 根据流程和任务信息构建任务日志文件的完整路径。
     *
     * @param firstSubmitTime the first submit time
     * @param processDefineCode the process definition code
     * @param processDefineVersion the process definition version
     * @param processInstanceId the process instance id
     * @param taskInstanceId the task instance id
     * @return the full task log file path
     */
    public static String getTaskLogPath(Date firstSubmitTime, Long processDefineCode, int processDefineVersion,
                                        int processInstanceId, int taskInstanceId) {
        // format /logs/YYYYMMDD/defintion-code_defintion_version-processInstanceId-taskInstanceId.log
        final String taskLogFileName = new StringBuilder(String.valueOf(processDefineCode))
                .append(Constants.UNDERLINE)
                .append(processDefineVersion)
                .append(Constants.SUBTRACT_CHAR)
                .append(processInstanceId)
                .append(Constants.SUBTRACT_CHAR)
                .append(taskInstanceId)
                .append(LOG_TAILFIX)
                .toString();
        // Optional.map will be skipped if null
        return Optional.of(LoggerFactory.getILoggerFactory())
                .map(e -> (AppenderAttachable<ILoggingEvent>) (e.getLogger("ROOT")))
                .map(e -> (SiftingAppender) (e.getAppender("TASKLOGFILE")))
                .map(e -> ((TaskLogDiscriminator) (e.getDiscriminator())))
                .map(TaskLogDiscriminator::getLogBase)
                .map(e -> Paths.get(e)
                        .toAbsolutePath()
                        .resolve(DateUtils.format(firstSubmitTime, DateConstants.YYYYMMDD, null))
                        .resolve(taskLogFileName))
                .map(Path::toString)
                .orElse("");
    }

    /**
     * 根据任务执行上下文构建任务日志文件的完整路径。
     *
     * @param taskExecutionContext the task execution context
     * @return the full task log file path
     */
    public static String getTaskLogPath(TaskExecutionContext taskExecutionContext) {
        return getTaskLogPath(taskExecutionContext.getFirstSubmitTime(), taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }

}
