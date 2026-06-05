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

import static org.apache.dolphinscheduler.api.enums.Status.COMMAND_STATE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.COUNT_PROCESS_DEFINITION_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.COUNT_PROCESS_INSTANCE_STATE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUEUE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_INSTANCE_STATE_COUNT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 数据分析控制器。提供任务状态统计、工作流实例状态统计、用户定义数量统计、命令状态统计和队列统计等数据分析REST API。
 */
@Api(tags = "DATA_ANALYSIS_TAG")
@RestController
@RequestMapping("projects/analysis")
public class DataAnalysisController extends BaseController {

    @Autowired
    DataAnalysisService dataAnalysisService;

    /**
     * 统计任务实例状态数据。按项目和时间范围统计各状态的任务实例数量。
     *
     * @param loginUser 当前登录用户
     * @param startDate 统计开始日期
     * @param endDate 统计结束日期
     * @param projectCode 项目编码
     * @return 任务实例状态统计数据
     */
    @ApiOperation(value = "countTaskState", notes = "COUNT_TASK_STATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/task-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_INSTANCE_STATE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countTaskState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "startDate", required = false) String startDate,
                                 @RequestParam(value = "endDate", required = false) String endDate,
                                 @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        Map<String, Object> result =
                dataAnalysisService.countTaskStateByProject(loginUser, projectCode, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * 统计工作流实例状态数据。按项目和时间范围统计各状态的工作流实例数量。
     *
     * @param loginUser 当前登录用户
     * @param startDate 统计开始日期
     * @param endDate 统计结束日期
     * @param projectCode 项目编码
     * @return 工作流实例状态统计数据
     */
    @ApiOperation(value = "countProcessInstanceState", notes = "COUNT_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/process-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_INSTANCE_STATE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countProcessInstanceState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate,
                                            @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        Map<String, Object> result =
                dataAnalysisService.countProcessInstanceStateByProject(loginUser, projectCode, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * 统计指定用户的流程定义数量。按项目统计每个用户创建的流程定义数。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @return 用户流程定义数量统计
     */
    @ApiOperation(value = "countDefinitionByUser", notes = "COUNT_PROCESS_DEFINITION_BY_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/define-user-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_DEFINITION_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countDefinitionByUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "projectCode", required = false, defaultValue = "0") long projectCode) {

        Map<String, Object> result = dataAnalysisService.countDefinitionByUser(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * 统计命令状态数据。按用户项目统计各状态的命令数量。
     *
     * @param loginUser 当前登录用户
     * @return 命令状态统计数据
     */
    @ApiOperation(value = "countCommandState", notes = "COUNT_COMMAND_STATE_NOTES")
    @GetMapping(value = "/command-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COMMAND_STATE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countCommandState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        Map<String, Object> result = dataAnalysisService.countCommandState(loginUser);
        return returnDataList(result);
    }

    /**
     * 统计队列任务数量。按用户项目统计各队列中的任务数量。
     *
     * @param loginUser 当前登录用户
     * @return 队列状态统计数据
     */
    @ApiOperation(value = "countQueueState", notes = "COUNT_QUEUE_STATE_NOTES")
    @GetMapping(value = "/queue-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUEUE_COUNT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result countQueueState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {

        Map<String, Object> result = dataAnalysisService.countQueueState(loginUser);
        return returnDataList(result);
    }
}
