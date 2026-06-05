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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_EXECUTE_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CHECK_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EXECUTE_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_EXECUTING_WORKFLOW_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_PROCESS_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;

import springfox.documentation.annotations.ApiIgnore;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 执行控制器。提供工作流和任务执行的REST API，包括启动工作流实例、批量启动、执行动作（暂停/停止/恢复等）、
 * 启动检查、查询正在执行的工作流详情以及启动任务实例等操作。
 */
@Api(tags = "EXECUTOR_TAG")
@RestController
@RequestMapping("projects/{projectCode}/executors")
public class ExecutorController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorController.class);

    @Autowired
    private ExecutorService execService;

    /**
     * 启动工作流实例。支持定时调度、补数和重跑等多种执行类型，可配置失败策略、告警方式和运行模式等参数。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processDefinitionCode 流程定义编码
     * @param scheduleTime 调度时间（补数模式下支持日期范围和手动输入两种方式）
     * @param failureStrategy 失败策略
     * @param startNodeList 启动节点列表
     * @param taskDependType 任务依赖类型
     * @param execType 执行类型
     * @param warningType 告警类型
     * @param warningGroupId 告警组ID
     * @param runMode 运行模式
     * @param processInstancePriority 工作流实例优先级
     * @param workerGroup Worker组
     * @param timeout 超时时间
     * @param expectedParallelismNumber 补数并行数
     * @return 启动结果状态码
     */
    @ApiOperation(value = "startProcessInstance", notes = "RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataTypeClass = String.class, example = "2022-04-06 00:00:00,2022-04-06 00:00:00"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataTypeClass = FailureStrategy.class),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataTypeClass = String.class),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataTypeClass = TaskDependType.class),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataTypeClass = CommandType.class),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", required = true, dataTypeClass = WarningType.class),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE", dataTypeClass = RunMode.class),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataTypeClass = Priority.class),
            @ApiImplicitParam(name = "workerGroup", value = "WORKER_GROUP", dataTypeClass = String.class, example = "default"),
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", dataTypeClass = long.class, example = "-1"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "expectedParallelismNumber", value = "EXPECTED_PARALLELISM_NUMBER", dataTypeClass = int.class, example = "8"),
            @ApiImplicitParam(name = "dryRun", value = "DRY_RUN", dataTypeClass = int.class, example = "0"),
            @ApiImplicitParam(name = "complementDependentMode", value = "COMPLEMENT_DEPENDENT_MODE", dataTypeClass = ComplementDependentMode.class)
    })
    @PostMapping(value = "start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result startProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @RequestParam(value = "processDefinitionCode") long processDefinitionCode,
                                       @RequestParam(value = "scheduleTime") String scheduleTime,
                                       @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                       @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                       @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                       @RequestParam(value = "execType", required = false) CommandType execType,
                                       @RequestParam(value = "warningType") WarningType warningType,
                                       @RequestParam(value = "warningGroupId", required = false, defaultValue = "0") Integer warningGroupId,
                                       @RequestParam(value = "runMode", required = false) RunMode runMode,
                                       @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                       @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                       @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                       @RequestParam(value = "timeout", required = false) Integer timeout,
                                       @RequestParam(value = "startParams", required = false) String startParams,
                                       @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                       @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                       @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode) {

        if (timeout == null) {
            timeout = Constants.MAX_TASK_TIMEOUT;
        }
        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }

        if (complementDependentMode == null) {
            complementDependentMode = ComplementDependentMode.OFF_MODE;
        }

        Map<String, Object> result = execService.execProcessInstance(loginUser, projectCode, processDefinitionCode,
                scheduleTime, execType, failureStrategy,
                startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority,
                workerGroup, environmentCode, timeout, startParamMap, expectedParallelismNumber, dryRun,
                complementDependentMode);
        return returnDataList(result);
    }

    /**
     * 批量启动工作流实例。对多个流程定义批量执行启动操作，任意流程定义编码不存在时返回失败信息但不影响其他成功任务。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processDefinitionCodes 流程定义编码列表（逗号分隔）
     * @param scheduleTime 调度时间
     * @param failureStrategy 失败策略
     * @param startNodeList 启动节点列表
     * @param taskDependType 任务依赖类型
     * @param execType 执行类型
     * @param warningType 告警类型
     * @param warningGroupId 告警组ID
     * @param runMode 运行模式
     * @param processInstancePriority 工作流实例优先级
     * @param workerGroup Worker组
     * @param timeout 超时时间
     * @param expectedParallelismNumber 补数并行数
     * @return 批量启动结果状态码
     */
    @ApiOperation(value = "batchStartProcessInstance", notes = "BATCH_RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCodes", value = "PROCESS_DEFINITION_CODE_LIST", required = true, dataTypeClass = String.class, example = "1,2,3"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataTypeClass = String.class, example = "2022-04-06 00:00:00,2022-04-06 00:00:00"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataTypeClass = FailureStrategy.class),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataTypeClass = String.class),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataTypeClass = TaskDependType.class),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataTypeClass = CommandType.class),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", required = true, dataTypeClass = WarningType.class),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE", dataTypeClass = RunMode.class),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataTypeClass = Priority.class),
            @ApiImplicitParam(name = "workerGroup", value = "WORKER_GROUP", dataTypeClass = String.class, example = "default"),
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", dataTypeClass = long.class, example = "-1"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "expectedParallelismNumber", value = "EXPECTED_PARALLELISM_NUMBER", dataTypeClass = int.class, example = "8"),
            @ApiImplicitParam(name = "dryRun", value = "DRY_RUN", dataTypeClass = int.class, example = "0"),
            @ApiImplicitParam(name = "complementDependentMode", value = "COMPLEMENT_DEPENDENT_MODE", dataTypeClass = ComplementDependentMode.class)
    })
    @PostMapping(value = "batch-start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchStartProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @RequestParam(value = "processDefinitionCodes") String processDefinitionCodes,
                                            @RequestParam(value = "scheduleTime") String scheduleTime,
                                            @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                            @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                            @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                            @RequestParam(value = "execType", required = false) CommandType execType,
                                            @RequestParam(value = "warningType") WarningType warningType,
                                            @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                            @RequestParam(value = "runMode", required = false) RunMode runMode,
                                            @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                            @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                            @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                            @RequestParam(value = "timeout", required = false) Integer timeout,
                                            @RequestParam(value = "startParams", required = false) String startParams,
                                            @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                            @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                            @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode) {

        if (timeout == null) {
            timeout = Constants.MAX_TASK_TIMEOUT;
        }

        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }

        if (complementDependentMode == null) {
            complementDependentMode = ComplementDependentMode.OFF_MODE;
        }

        Map<String, Object> result = new HashMap<>();
        List<String> processDefinitionCodeArray = Arrays.asList(processDefinitionCodes.split(Constants.COMMA));
        List<String> startFailedProcessDefinitionCodeList = new ArrayList<>();

        processDefinitionCodeArray = processDefinitionCodeArray.stream().distinct().collect(Collectors.toList());

        for (String strProcessDefinitionCode : processDefinitionCodeArray) {
            long processDefinitionCode = Long.parseLong(strProcessDefinitionCode);
            result = execService.execProcessInstance(loginUser, projectCode, processDefinitionCode, scheduleTime,
                    execType, failureStrategy,
                    startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority,
                    workerGroup, environmentCode, timeout, startParamMap, expectedParallelismNumber, dryRun,
                    complementDependentMode);

            if (!Status.SUCCESS.equals(result.get(Constants.STATUS))) {
                startFailedProcessDefinitionCodeList.add(String.valueOf(processDefinitionCode));
            }
        }

        if (!startFailedProcessDefinitionCodeList.isEmpty()) {
            putMsg(result, Status.BATCH_START_PROCESS_INSTANCE_ERROR,
                    String.join(Constants.COMMA, startFailedProcessDefinitionCodeList));
        }

        return returnDataList(result);
    }

    /**
     * 对工作流实例执行操作。支持暂停、停止、重跑、恢复暂停、恢复停止等操作。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processInstanceId 工作流实例ID
     * @param executeType 执行类型
     * @return 执行结果状态码
     */
    @ApiOperation(value = "execute", notes = "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataTypeClass = ExecuteType.class)
    })
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result execute(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                          @RequestParam("processInstanceId") Integer processInstanceId,
                          @RequestParam("executeType") ExecuteType executeType) {
        Map<String, Object> result = execService.execute(loginUser, projectCode, processInstanceId, executeType);
        return returnDataList(result);
    }

    /**
     * 批量对工作流实例执行操作。对多个工作流实例执行相同的操作类型。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processInstanceIds 工作流实例ID列表（逗号分隔）
     * @param executeType 执行类型
     * @return 批量执行结果状态码
     */
    @ApiOperation(value = "batchExecute", notes = "BATCH_EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = int.class),
            @ApiImplicitParam(name = "processInstanceIds", value = "PROCESS_INSTANCE_IDS", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataTypeClass = ExecuteType.class)
    })
    @PostMapping(value = "/batch-execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchExecute(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @PathVariable long projectCode,
                               @RequestParam("processInstanceIds") String processInstanceIds,
                               @RequestParam("executeType") ExecuteType executeType) {
        Map<String, Object> result = new HashMap<>();
        List<String> executeFailedIdList = new ArrayList<>();
        if (!StringUtils.isEmpty(processInstanceIds)) {
            String[] processInstanceIdArray = processInstanceIds.split(Constants.COMMA);

            for (String strProcessInstanceId : processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    Map<String, Object> singleResult =
                            execService.execute(loginUser, projectCode, processInstanceId, executeType);
                    if (!Status.SUCCESS.equals(singleResult.get(Constants.STATUS))) {
                        executeFailedIdList.add((String) singleResult.get(Constants.MSG));
                        logger.error((String) singleResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    executeFailedIdList
                            .add(MessageFormat.format(Status.PROCESS_INSTANCE_ERROR.getMsg(), strProcessInstanceId));
                }
            }
        }
        if (!executeFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_EXECUTE_PROCESS_INSTANCE_ERROR, String.join("\n", executeFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return returnDataList(result);
    }

    /**
     * 检查流程定义及其子流程是否均已上线。启动工作流实例前的预检查。
     *
     * @param processDefinitionCode 流程定义编码
     * @return 检查结果状态码
     */
    @ApiOperation(value = "startCheckProcessDefinition", notes = "START_CHECK_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @PostMapping(value = "/start-check")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CHECK_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result startCheckProcessDefinition(@RequestParam(value = "processDefinitionCode") long processDefinitionCode) {
        Map<String, Object> result = execService.startCheckByProcessDefinedCode(processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * 查询正在执行的工作流实例数据。从Master节点获取工作流实例的实时执行信息。
     *
     * @param processInstanceId 工作流实例ID
     * @return 工作流执行数据
     */
    @ApiOperation(value = "queryExecutingWorkflow", notes = "QUERY_WORKFLOW_EXECUTE_DATA")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/query-executing-workflow")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXECUTING_WORKFLOW_ERROR)
    @AccessLogAnnotation
    public Result queryExecutingWorkflow(@RequestParam("id") Integer processInstanceId) {
        WorkflowExecuteDto workflowExecuteDto =
                execService.queryExecutingWorkflowByProcessInstanceId(processInstanceId);
        return Result.success(workflowExecuteDto);
    }

    /**
     * 启动任务实例。直接启动指定版本的任务定义实例，适用于流式任务场景。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @param version 任务定义版本
     * @param warningGroupId 告警组ID
     * @param workerGroup Worker组
     * @return 启动任务结果状态码
     */
    @ApiOperation(value = "startTaskInstance", notes = "RUN_TASK_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "version", value = "VERSION", dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataTypeClass = FailureStrategy.class),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataTypeClass = CommandType.class),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", required = true, dataTypeClass = WarningType.class),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "workerGroup", value = "WORKER_GROUP", dataTypeClass = String.class, example = "default"),
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", dataTypeClass = long.class, example = "-1"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "dryRun", value = "DRY_RUN", dataTypeClass = int.class, example = "0"),
    })
    @PostMapping(value = "/task-instance/{code}/start")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result startStreamTaskInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @ApiParam(name = "code", value = "TASK_CODE", required = true) @PathVariable long code,
                                          @RequestParam(value = "version", required = true) int version,
                                          @RequestParam(value = "warningGroupId", required = false, defaultValue = "0") Integer warningGroupId,
                                          @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                          @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                          @RequestParam(value = "startParams", required = false) String startParams,
                                          @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun) {

        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }

        Map<String, Object> result = execService.execStreamTaskInstance(loginUser, projectCode, code, version,
                warningGroupId, workerGroup, environmentCode, startParamMap, dryRun);
        return returnDataList(result);
    }
}
