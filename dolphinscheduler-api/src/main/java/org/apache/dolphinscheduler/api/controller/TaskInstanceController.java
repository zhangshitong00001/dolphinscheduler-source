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

import static org.apache.dolphinscheduler.api.enums.Status.FORCE_TASK_SUCCESS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_SAVEPOINT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_STOP_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

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
 * 任务实例控制器，提供任务实例的查询、强制成功、停止及 savepoint 操作 REST API。
 */
@Api(tags = "TASK_INSTANCE_TAG")
@RestController
@RequestMapping("/projects/{projectCode}/task-instances")
public class TaskInstanceController extends BaseController {

    @Autowired
    private TaskInstanceService taskInstanceService;

    /**
     * 查询任务实例分页列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param processInstanceId 流程实例 ID
     * @param processInstanceName 流程实例名称
     * @param processDefinitionName 流程定义名称
     * @param searchVal 搜索关键词
     * @param taskName 任务名称
     * @param executorName 执行者名称
     * @param stateType 状态类型
     * @param host 主机
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param taskExecuteType 任务执行类型
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 任务实例分页数据
     */
    @ApiOperation(value = "queryTaskListPaging", notes = "QUERY_TASK_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = false, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "processInstanceName", value = "PROCESS_INSTANCE_NAME", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "taskName", value = "TASK_NAME", dataTypeClass = String.class),
            @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", dataTypeClass = String.class),
            @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", dataTypeClass = TaskExecutionStatus.class),
            @ApiImplicitParam(name = "host", value = "HOST", dataTypeClass = String.class),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "taskExecuteType", value = "TASK_EXECUTE_TYPE", required = false, dataTypeClass = TaskExecuteType.class, example = "STREAM"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20"),
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                      @RequestParam(value = "processInstanceId", required = false, defaultValue = "0") Integer processInstanceId,
                                      @RequestParam(value = "processInstanceName", required = false) String processInstanceName,
                                      @RequestParam(value = "processDefinitionName", required = false) String processDefinitionName,
                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                      @RequestParam(value = "taskName", required = false) String taskName,
                                      @RequestParam(value = "executorName", required = false) String executorName,
                                      @RequestParam(value = "stateType", required = false) TaskExecutionStatus stateType,
                                      @RequestParam(value = "host", required = false) String host,
                                      @RequestParam(value = "startDate", required = false) String startTime,
                                      @RequestParam(value = "endDate", required = false) String endTime,
                                      @RequestParam(value = "taskExecuteType", required = false, defaultValue = "BATCH") TaskExecuteType taskExecuteType,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = taskInstanceService.queryTaskListPaging(loginUser, projectCode, processInstanceId, processInstanceName,
                processDefinitionName,
                taskName, executorName, startTime, endTime, searchVal, stateType, host, taskExecuteType, pageNo,
                pageSize);
        return result;
    }

    /**
     * 将任务实例状态从 FAILURE 强制切换为 FORCED_SUCCESS。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param id 任务实例 ID
     * @return 操作结果
     */
    @ApiOperation(value = "force-success", notes = "FORCE_TASK_SUCCESS")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "TASK_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "12")
    })
    @PostMapping(value = "/{id}/force-success")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(FORCE_TASK_SUCCESS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> forceTaskSuccess(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @PathVariable(value = "id") Integer id) {
        Map<String, Object> result = taskInstanceService.forceTaskSuccess(loginUser, projectCode, id);
        return returnDataList(result);
    }

    /**
     * 任务 savepoint 操作，用于流式任务。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param id 任务实例 ID
     * @return 操作结果
     */
    @ApiOperation(value = "savepoint", notes = "TASK_SAVEPOINT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "TASK_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "12")
    })
    @PostMapping(value = "/{id}/savepoint")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_SAVEPOINT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> taskSavePoint(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @PathVariable(value = "id") Integer id) {
        return taskInstanceService.taskSavePoint(loginUser, projectCode, id);
    }

    /**
     * 停止任务，用于流式任务。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param id 任务实例 ID
     * @return 操作结果
     */
    @ApiOperation(value = "stop", notes = "TASK_INSTANCE_STOP")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "TASK_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "12")
    })
    @PostMapping(value = "/{id}/stop")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_STOP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> stopTask(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                   @PathVariable(value = "id") Integer id) {
        return taskInstanceService.stopTask(loginUser, projectCode, id);
    }
}
