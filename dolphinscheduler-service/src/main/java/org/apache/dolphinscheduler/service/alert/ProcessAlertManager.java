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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResultAlertContent;
import org.apache.dolphinscheduler.dao.entity.ProcessAlertContent;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskAlertContent;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 流程告警管理器，负责根据流程实例和任务实例的状态生成并发送各类告警。
 * <p>支持以下告警类型：
 * <ul>
 *   <li>流程实例成功/失败告警</li>
 *   <li>Worker容错告警</li>
 *   <li>流程超时告警</li>
 *   <li>流程阻塞告警</li>
 *   <li>数据质量任务结果告警</li>
 *   <li>任务执行失败告警</li>
 *   <li>任务超时告警</li>
 * </ul></p>
 */
@Component
public class ProcessAlertManager {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAlertManager.class);

    /** 告警数据访问对象 */
    @Autowired
    private AlertDao alertDao;

    /**
     * 将命令类型转换为中文名称。
     *
     * @param commandType 命令类型
     * @return 命令类型对应的中文名称
     */
    private String getCommandCnName(CommandType commandType) {
        switch (commandType) {
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                return "recover tolerance fault process";
            case RECOVER_SUSPENDED_PROCESS:
                return "recover suspended process";
            case START_CURRENT_TASK_PROCESS:
                return "start current task process";
            case START_FAILURE_TASK_PROCESS:
                return "start failure task process";
            case START_PROCESS:
                return "start process";
            case REPEAT_RUNNING:
                return "repeat running";
            case SCHEDULER:
                return "scheduler";
            case COMPLEMENT_DATA:
                return "complement data";
            case PAUSE:
                return "pause";
            case STOP:
                return "stop";
            default:
                return "unknown type";
        }
    }

    /**
     * 获取流程实例的告警内容，根据流程状态生成成功或失败的任务列表JSON。
     *
     * @param processInstance 流程实例
     * @param taskInstances 任务实例列表
     * @param projectUser 项目用户信息
     * @return 流程实例告警内容JSON字符串
     */
    public String getContentProcessInstance(ProcessInstance processInstance,
                                            List<TaskInstance> taskInstances,
                                            ProjectUser projectUser) {

        String res = "";
        if (processInstance.getState().isSuccess()) {
            List<ProcessAlertContent> successTaskList = new ArrayList<>(1);
            ProcessAlertContent processAlertContent = ProcessAlertContent.builder()
                    .projectCode(projectUser.getProjectCode())
                    .projectName(projectUser.getProjectName())
                    .owner(projectUser.getUserName())
                    .processId(processInstance.getId())
                    .processDefinitionCode(processInstance.getProcessDefinitionCode())
                    .processName(processInstance.getName())
                    .processType(processInstance.getCommandType())
                    .processState(processInstance.getState())
                    .recovery(processInstance.getRecovery())
                    .runTimes(processInstance.getRunTimes())
                    .processStartTime(processInstance.getStartTime())
                    .processEndTime(processInstance.getEndTime())
                    .processHost(processInstance.getHost())
                    .build();
            successTaskList.add(processAlertContent);
            res = JSONUtils.toJsonString(successTaskList);
        } else if (processInstance.getState().isFailure()) {

            List<ProcessAlertContent> failedTaskList = new ArrayList<>();
            for (TaskInstance task : taskInstances) {
                if (task.getState().isSuccess()) {
                    continue;
                }
                ProcessAlertContent processAlertContent = ProcessAlertContent.builder()
                        .projectCode(projectUser.getProjectCode())
                        .projectName(projectUser.getProjectName())
                        .owner(projectUser.getUserName())
                        .processId(processInstance.getId())
                        .processDefinitionCode(processInstance.getProcessDefinitionCode())
                        .processName(processInstance.getName())
                        .taskCode(task.getTaskCode())
                        .taskName(task.getName())
                        .taskType(task.getTaskType())
                        .taskState(task.getState())
                        .taskStartTime(task.getStartTime())
                        .taskEndTime(task.getEndTime())
                        .taskHost(task.getHost())
                        .logPath(task.getLogPath())
                        .build();
                failedTaskList.add(processAlertContent);
            }
            res = JSONUtils.toJsonString(failedTaskList);
        }

        return res;
    }

    /**
     * getting worker fault tolerant content
     *
     * @param processInstance process instance
     * @param toleranceTaskList tolerance task list
     * @return worker tolerance content
     */
    private String getWorkerToleranceContent(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList) {

        List<ProcessAlertContent> toleranceTaskInstanceList = new ArrayList<>();

        for (TaskInstance taskInstance : toleranceTaskList) {
            ProcessAlertContent processAlertContent = ProcessAlertContent.builder()
                    .processId(processInstance.getId())
                    .processDefinitionCode(processInstance.getProcessDefinitionCode())
                    .processName(processInstance.getName())
                    .taskCode(taskInstance.getTaskCode())
                    .taskName(taskInstance.getName())
                    .taskHost(taskInstance.getHost())
                    .retryTimes(taskInstance.getRetryTimes())
                    .build();
            toleranceTaskInstanceList.add(processAlertContent);
        }
        return JSONUtils.toJsonString(toleranceTaskInstanceList);
    }

    /**
     * 发送Worker容错告警，当Worker故障时记录需要容错处理的任务列表。
     *
     * @param processInstance 流程实例
     * @param toleranceTaskList 需要容错处理的任务实例列表
     */
    public void sendAlertWorkerToleranceFault(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList) {
        try {
            Alert alert = new Alert();
            alert.setTitle("worker fault tolerance");
            String content = getWorkerToleranceContent(processInstance, toleranceTaskList);
            alert.setContent(content);
            alert.setWarningType(WarningType.FAILURE);
            alert.setCreateTime(new Date());
            alert.setAlertGroupId(
                    processInstance.getWarningGroupId() == null ? 1 : processInstance.getWarningGroupId());
            alert.setAlertType(AlertType.FAULT_TOLERANCE_WARNING);
            alertDao.addAlert(alert);
            logger.info("add alert to db , alert : {}", alert);

        } catch (Exception e) {
            logger.error("send alert failed:{} ", e.getMessage());
        }

    }

    /**
     * 发送流程实例告警，根据流程执行结果（成功/失败）发送相应的告警通知。
     *
     * @param processInstance 流程实例
     * @param taskInstances 任务实例列表
     * @param projectUser 项目用户信息
     */
    public void sendAlertProcessInstance(ProcessInstance processInstance,
                                         List<TaskInstance> taskInstances,
                                         ProjectUser projectUser) {

        if (!isNeedToSendWarning(processInstance)) {
            return;
        }

        Alert alert = new Alert();

        String cmdName = getCommandCnName(processInstance.getCommandType());
        String success = processInstance.getState().isSuccess() ? "success" : "failed";
        alert.setTitle(cmdName + " " + success);
        alert.setWarningType(processInstance.getState().isSuccess() ? WarningType.SUCCESS : WarningType.FAILURE);
        String content = getContentProcessInstance(processInstance, taskInstances, projectUser);
        alert.setContent(content);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        alert.setAlertType(processInstance.getState().isSuccess() ? AlertType.PROCESS_INSTANCE_SUCCESS
                : AlertType.PROCESS_INSTANCE_FAILURE);
        alertDao.addAlert(alert);
        logger.info("add alert to db , alert: {}", alert);
    }

    /**
     * 判断是否需要发送告警，根据流程实例的告警类型和状态决定。
     *
     * @param processInstance 流程实例
     * @return 需要发送告警返回true，否则返回false
     */
    public boolean isNeedToSendWarning(ProcessInstance processInstance) {
        if (Flag.YES == processInstance.getIsSubProcess()) {
            return false;
        }
        boolean sendWarning = false;
        WarningType warningType = processInstance.getWarningType();
        switch (warningType) {
            case ALL:
                if (processInstance.getState().isFinished()) {
                    sendWarning = true;
                }
                break;
            case SUCCESS:
                if (processInstance.getState().isSuccess()) {
                    sendWarning = true;
                }
                break;
            case FAILURE:
                if (processInstance.getState().isFailure()) {
                    sendWarning = true;
                }
                break;
            default:
        }
        return sendWarning;
    }

    /**
     * 发送关闭告警事件。如果该流程实例之前已发送过告警，则插入一条关闭事件记录。
     *
     * @param processInstance 已成功的流程实例
     */
    public void closeAlert(ProcessInstance processInstance) {
        List<Alert> alerts = alertDao.listAlerts(processInstance.getId());
        if (CollectionUtils.isEmpty(alerts)) {
            // no need to close alert
            return;
        }

        Alert alert = new Alert();
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setUpdateTime(new Date());
        alert.setCreateTime(new Date());
        alert.setProjectCode(processInstance.getProcessDefinition().getProjectCode());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        alert.setAlertType(AlertType.CLOSE_ALERT);
        alertDao.addAlert(alert);
    }

    /**
     * 发送流程超时告警。
     *
     * @param processInstance 流程实例
     * @param projectUser 项目用户信息
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProjectUser projectUser) {
        alertDao.sendProcessTimeoutAlert(processInstance, projectUser);
    }

    /**
     * 发送数据质量任务执行结果告警。
     *
     * @param result 数据质量执行结果
     * @param processInstance 流程实例
     */
    public void sendDataQualityTaskExecuteResultAlert(DqExecuteResult result, ProcessInstance processInstance) {
        Alert alert = new Alert();
        String state = DqTaskState.of(result.getState()).getDescription();
        alert.setTitle("DataQualityResult [" + result.getTaskName() + "] " + state);
        String content = getDataQualityAlterContent(result);
        alert.setContent(content);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(result.getProjectCode());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        // might need to change to data quality status
        alert.setAlertType(processInstance.getState().isSuccess() ? AlertType.PROCESS_INSTANCE_SUCCESS
                : AlertType.PROCESS_INSTANCE_FAILURE);
        alertDao.addAlert(alert);
        logger.info("add alert to db , alert: {}", alert);
    }

    /**
     * 发送任务执行失败告警。
     *
     * @param taskInstance 任务实例
     * @param processInstance 流程实例
     */
    public void sendTaskErrorAlert(TaskInstance taskInstance, ProcessInstance processInstance) {
        Alert alert = new Alert();
        alert.setTitle("Task [" + taskInstance.getName() + "] Failure Warning");
        String content = getTaskAlterContent(taskInstance);
        alert.setContent(content);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        alert.setAlertType(AlertType.TASK_FAILURE);
        alertDao.addAlert(alert);
        logger.info("add alert to db , alert: {}", alert);
    }

    /**
     * 获取数据质量告警的内容JSON字符串。
     *
     * @param result 数据质量执行结果
     * @return 数据质量告警内容JSON字符串
     */
    public String getDataQualityAlterContent(DqExecuteResult result) {

        DqExecuteResultAlertContent content = DqExecuteResultAlertContent.newBuilder()
                .processDefinitionId(result.getProcessDefinitionId())
                .processDefinitionName(result.getProcessDefinitionName())
                .processInstanceId(result.getProcessInstanceId())
                .processInstanceName(result.getProcessInstanceName())
                .taskInstanceId(result.getTaskInstanceId())
                .taskName(result.getTaskName())
                .ruleType(result.getRuleType())
                .ruleName(result.getRuleName())
                .statisticsValue(result.getStatisticsValue())
                .comparisonValue(result.getComparisonValue())
                .checkType(result.getCheckType())
                .threshold(result.getThreshold())
                .operator(result.getOperator())
                .failureStrategy(result.getFailureStrategy())
                .userId(result.getUserId())
                .userName(result.getUserName())
                .state(result.getState())
                .errorDataPath(result.getErrorOutputPath())
                .build();

        return JSONUtils.toJsonString(content);
    }

    /**
     * 获取任务告警的内容JSON字符串。
     *
     * @param taskInstance 任务实例
     * @return 任务告警内容JSON字符串
     */
    public String getTaskAlterContent(TaskInstance taskInstance) {

        TaskAlertContent content = TaskAlertContent.builder()
                .processInstanceName(taskInstance.getProcessInstanceName())
                .processInstanceId(taskInstance.getProcessInstanceId())
                .taskInstanceId(taskInstance.getId())
                .taskName(taskInstance.getName())
                .taskType(taskInstance.getTaskType())
                .state(taskInstance.getState())
                .startTime(taskInstance.getStartTime())
                .endTime(taskInstance.getEndTime())
                .host(taskInstance.getHost())
                .logPath(taskInstance.getLogPath())
                .build();

        return JSONUtils.toJsonString(content);
    }

    public void sendTaskTimeoutAlert(ProcessInstance processInstance, TaskInstance taskInstance,
                                     ProjectUser projectUser) {
        alertDao.sendTaskTimeoutAlert(processInstance, taskInstance, projectUser);
    }

    /**
     * 检查节点类型和流程阻塞标志，将阻塞记录插入数据库。
     *
     * @param processInstance 流程实例
     * @param projectUser 项目所属用户
     */
    public void sendProcessBlockingAlert(ProcessInstance processInstance,
                                         ProjectUser projectUser) {
        Alert alert = new Alert();
        String cmdName = getCommandCnName(processInstance.getCommandType());
        List<ProcessAlertContent> blockingNodeList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.builder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .processId(processInstance.getId())
                .processName(processInstance.getName())
                .processType(processInstance.getCommandType())
                .processState(processInstance.getState())
                .runTimes(processInstance.getRunTimes())
                .processStartTime(processInstance.getStartTime())
                .processEndTime(processInstance.getEndTime())
                .processHost(processInstance.getHost())
                .build();
        blockingNodeList.add(processAlertContent);
        String content = JSONUtils.toJsonString(blockingNodeList);
        alert.setTitle(cmdName + " Blocked");
        alert.setContent(content);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        alert.setAlertType(AlertType.PROCESS_INSTANCE_BLOCKED);
        alertDao.addAlert(alert);
        logger.info("add alert to db, alert: {}", alert);
    }
}
