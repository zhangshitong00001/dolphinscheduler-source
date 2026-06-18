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

import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.storage.impl.HadoopUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 流程进程工具类，主要用于获取和管理进程的命令行操作。提供YARN应用的取消、Kerberos认证命令构建、
 * 进程PID查询以及YARN任务的日志查找和终止等功能。
 */
public class ProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-]-\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSATTERN = Pattern.compile("\\w+\\((\\d+)\\)");

    /**
     * 取消（终止）指定的YARN应用列表。对于未完成的应用，构建kill命令并执行。
     *
     * @param appIds the list of YARN application ids to cancel
     * @param logger the logger instance
     * @param tenantCode the tenant code
     * @param executePath the execution path
     */
    public static void cancelApplication(List<String> appIds, Logger logger, String tenantCode, String executePath) {
        if (appIds == null || appIds.isEmpty()) {
            return;
        }

        for (String appId : appIds) {
            try {
                TaskExecutionStatus applicationStatus = HadoopUtils.getInstance().getApplicationStatus(appId);

                if (!applicationStatus.isFinished()) {
                    String commandFile = String.format("%s/%s.kill", executePath, appId);
                    String cmd = getKerberosInitCommand() + "yarn application -kill " + appId;
                    execYarnKillCommand(logger, tenantCode, appId, commandFile, cmd);
                }
            } catch (Exception e) {
                logger.error("Get yarn application app id [{}}] status failed", appId, e);
            }
        }
    }

    /**
     * 构建Kerberos初始化命令，用于在进行YARN操作前进行Kerberos认证。如果未启用Kerberos则返回空字符串。
     *
     * @return the Kerberos init command string
     */
    static String getKerberosInitCommand() {
        logger.info("get kerberos init command");
        StringBuilder kerberosCommandBuilder = new StringBuilder();
        boolean hadoopKerberosState =
                PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        if (hadoopKerberosState) {
            kerberosCommandBuilder.append("export KRB5_CONFIG=")
                    .append(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH))
                    .append("\n\n")
                    .append(String.format("kinit -k -t %s %s || true",
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH),
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME)))
                    .append("\n\n");
            logger.info("kerberos init command: {}", kerberosCommandBuilder);
        }
        return kerberosCommandBuilder.toString();
    }

    /**
     * build kill command for yarn application
     *
     * @param logger logger
     * @param tenantCode tenant code
     * @param appId app id
     * @param commandFile command file
     * @param cmd cmd
     */
    private static void execYarnKillCommand(Logger logger, String tenantCode, String appId, String commandFile,
                                            String cmd) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("#!/bin/sh\n");
            sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
            sb.append("cd $BASEDIR\n");
            if (CommonUtils.getSystemEnvPath() != null) {
                sb.append("source ").append(CommonUtils.getSystemEnvPath()).append("\n");
            }
            sb.append("\n\n");
            sb.append(cmd);

            File f = new File(commandFile);

            if (!f.exists()) {
                org.apache.commons.io.FileUtils.writeStringToFile(new File(commandFile), sb.toString(),
                        StandardCharsets.UTF_8);
            }

            String runCmd = String.format("%s %s", Constants.SH, commandFile);
            runCmd = OSUtils.getSudoCmd(tenantCode, runCmd);
            logger.info("kill cmd:{}", runCmd);
            OSUtils.exeCmd(runCmd);
        } catch (Exception e) {
            logger.error(String.format("Kill yarn application app id [%s] failed: [%s]", appId, e.getMessage()));
        }
    }

    /**
     * 获取指定进程及其子进程的PID列表字符串。根据操作系统类型（Mac/Linux）使用不同的命令解析方式。
     *
     * @param processId the parent process id
     * @return the space-separated PID string
     * @throws Exception if command execution fails
     */
    public static String getPidsStr(int processId) throws Exception {
        List<String> pidList = new ArrayList<>();
        Matcher mat = null;
        // pstree pid get sub pids
        if (SystemUtils.IS_OS_MAC) {
            String pids = OSUtils.exeCmd(String.format("%s -sp %d", Constants.PSTREE, processId));
            if (null != pids) {
                mat = MACPATTERN.matcher(pids);
            }
        } else {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", Constants.PSTREE, processId));
            mat = WINDOWSATTERN.matcher(pids);
        }

        if (null != mat) {
            while (mat.find()) {
                pidList.add(mat.group(1));
            }
        }

        if (OSUtils.isSudoEnable() && !pidList.isEmpty()) {
            pidList = pidList.subList(1, pidList.size());
        }
        return String.join(" ", pidList).trim();
    }

    /**
     * 查找并终止任务相关的YARN应用。从日志客户端获取appId列表后，依次取消未完成的应用。
     *
     * @param logClient the log client for retrieving application ids
     * @param taskExecutionContext the task execution context
     * @return the list of killed YARN application ids
     */
    public static @Nullable List<String> killYarnJob(@NonNull LogClient logClient,
                                                     @NonNull TaskExecutionContext taskExecutionContext) {
        if (taskExecutionContext.getLogPath() == null) {
            return Collections.emptyList();
        }
        try {
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            Host host = Host.of(taskExecutionContext.getHost());
            List<String> appIds = logClient.getAppIds(host.getIp(), host.getPort(), taskExecutionContext.getLogPath());
            if (CollectionUtils.isNotEmpty(appIds)) {
                if (StringUtils.isEmpty(taskExecutionContext.getExecutePath())) {
                    taskExecutionContext
                            .setExecutePath(FileUtils.getProcessExecDir(
                                    taskExecutionContext.getTenantCode(),
                                    taskExecutionContext.getProjectCode(),
                                    taskExecutionContext.getProcessDefineCode(),
                                    taskExecutionContext.getProcessDefineVersion(),
                                    taskExecutionContext.getProcessInstanceId(),
                                    taskExecutionContext.getTaskInstanceId()));
                }
                FileUtils.createWorkDirIfAbsent(taskExecutionContext.getExecutePath());
                cancelApplication(appIds, logger, taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());
                return appIds;
            } else {
                logger.info("The current appId is empty, don't need to kill the yarn job, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId());
            }
        } catch (Exception e) {
            logger.error("Kill yarn job failure, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        return Collections.emptyList();
    }
}
