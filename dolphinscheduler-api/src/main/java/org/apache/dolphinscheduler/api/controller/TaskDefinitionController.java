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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_DEFINE_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_TASK_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TASK_DEFINITION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
 * 任务定义控制器，提供任务定义的创建、更新、删除、版本管理、发布等 REST API。
 */
@Api(tags = "TASK_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/task-definition")
public class TaskDefinitionController extends BaseController {

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    /**
     * 创建任务定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param taskDefinitionJson 任务定义 JSON
     * @return 创建结果
     */
    @ApiOperation(value = "save", notes = "CREATE_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "taskDefinitionJson", value = "TASK_DEFINITION_JSON", required = true, dataTypeClass = String.class)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson) {
        Map<String, Object> result =
                taskDefinitionService.createTaskDefinition(loginUser, projectCode, taskDefinitionJson);
        return returnDataList(result);
    }

    /**
     * 创建绑定到工作流的单个任务定义。
     *
     * @param loginUser             登录用户
     * @param projectCode           项目编码
     * @param processDefinitionCode 流程定义编码
     * @param taskDefinitionJsonObj 任务定义 JSON 对象
     * @param upstreamCodes         上游任务编码列表，逗号分隔
     * @return 创建结果
     */
    @ApiOperation(value = "saveSingle", notes = "CREATE_SINGLE_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "taskDefinitionJsonObj", value = "TASK_DEFINITION_JSON", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "upstreamCodes", value = "UPSTREAM_CODES", required = false, dataTypeClass = String.class)
    })
    @PostMapping("/save-single")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createTaskBindsWorkFlow(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "processDefinitionCode", required = true) long processDefinitionCode,
                                          @RequestParam(value = "taskDefinitionJsonObj", required = true) String taskDefinitionJsonObj,
                                          @RequestParam(value = "upstreamCodes", required = false) String upstreamCodes) {
        Map<String, Object> result = taskDefinitionService.createTaskBindsWorkFlow(loginUser, projectCode,
                processDefinitionCode, taskDefinitionJsonObj, StringUtils.defaultString(upstreamCodes));
        return returnDataList(result);
    }

    /**
     * 更新任务定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @param taskDefinitionJsonObj 任务定义 JSON 对象
     * @return 更新结果
     */
    @ApiOperation(value = "update", notes = "UPDATE_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "taskDefinitionJsonObj", value = "TASK_DEFINITION_JSON", required = true, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @PathVariable(value = "code") long code,
                                       @RequestParam(value = "taskDefinitionJsonObj", required = true) String taskDefinitionJsonObj) {
        Map<String, Object> result =
                taskDefinitionService.updateTaskDefinition(loginUser, projectCode, code, taskDefinitionJsonObj);
        return returnDataList(result);
    }

    /**
     * 更新任务定义及上游依赖关系。
     *
     * @param loginUser             登录用户
     * @param projectCode           项目编码
     * @param code                  任务定义编码
     * @param taskDefinitionJsonObj 任务定义 JSON 对象
     * @param upstreamCodes         上游任务编码列表，逗号分隔
     * @return 更新结果
     */
    @ApiOperation(value = "updateWithUpstream", notes = "UPDATE_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "taskDefinitionJsonObj", value = "TASK_DEFINITION_JSON", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "upstreamCodes", value = "UPSTREAM_CODES", required = false, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{code}/with-upstream")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateTaskWithUpstream(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @PathVariable(value = "code") long code,
                                         @RequestParam(value = "taskDefinitionJsonObj", required = true) String taskDefinitionJsonObj,
                                         @RequestParam(value = "upstreamCodes", required = false) String upstreamCodes) {
        Map<String, Object> result = taskDefinitionService.updateTaskWithUpstream(loginUser, projectCode, code,
                taskDefinitionJsonObj, upstreamCodes);
        return returnDataList(result);
    }

    /**
     * 查询任务定义版本分页列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 任务定义版本列表
     */
    @ApiOperation(value = "queryVersions", notes = "QUERY_TASK_DEFINITION_VERSIONS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10")
    })
    @GetMapping(value = "/{code}/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_DEFINITION_VERSIONS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskDefinitionVersions(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable(value = "code") long code,
                                              @RequestParam(value = "pageNo") int pageNo,
                                              @RequestParam(value = "pageSize") int pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        return taskDefinitionService.queryTaskDefinitionVersions(loginUser, projectCode, code, pageNo, pageSize);
    }

    /**
     * 切换任务定义到指定版本。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @param version 要切换到的版本号
     * @return 切换结果
     */
    @ApiOperation(value = "switchVersion", notes = "SWITCH_TASK_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_TASK_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result switchTaskDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable(value = "code") long code,
                                              @PathVariable(value = "version") int version) {
        Map<String, Object> result = taskDefinitionService.switchVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * 删除指定任务定义的某个版本。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @param version 要删除的版本号
     * @return 删除结果
     */
    @ApiOperation(value = "deleteVersion", notes = "DELETE_TASK_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteTaskDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable(value = "code") long code,
                                              @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                taskDefinitionService.deleteByCodeAndVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * 根据编码删除任务定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @return 删除结果
     */
    @ApiOperation(value = "deleteTaskDefinition", notes = "DELETE_TASK_DEFINITION_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1")
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteTaskDefinitionByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @PathVariable(value = "code") long code) {
        Map<String, Object> result = taskDefinitionService.deleteTaskDefinitionByCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * 根据编码查询任务定义详情。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @return 任务定义详情
     */
    @ApiOperation(value = "queryTaskDefinitionByCode", notes = "QUERY_TASK_DEFINITION_DETAIL_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1")
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskDefinitionDetail(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @PathVariable(value = "code") long code) {
        Map<String, Object> result = taskDefinitionService.queryTaskDefinitionDetail(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * 查询任务定义分页列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param searchWorkflowName 搜索工作流名称
     * @param searchTaskName 搜索任务名称
     * @param taskType 任务类型
     * @param taskExecuteType 任务执行类型
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 任务定义分页数据
     */
    @ApiOperation(value = "queryTaskDefinitionListPaging", notes = "QUERY_TASK_DEFINITION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = false, dataTypeClass = long.class),
            @ApiImplicitParam(name = "searchWorkflowName", value = "SEARCH_WORKFLOW_NAME", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "searchTaskName", value = "SEARCH_TASK_NAME", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "taskType", value = "TASK_TYPE", required = false, dataTypeClass = String.class, example = "SHELL"),
            @ApiImplicitParam(name = "taskExecuteType", value = "TASK_EXECUTE_TYPE", required = false, dataTypeClass = TaskExecuteType.class, example = "STREAM"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_DEFINITION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryTaskDefinitionListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @RequestParam(value = "searchWorkflowName", required = false) String searchWorkflowName,
                                                @RequestParam(value = "searchTaskName", required = false) String searchTaskName,
                                                @RequestParam(value = "taskType", required = false) String taskType,
                                                @RequestParam(value = "taskExecuteType", required = false, defaultValue = "BATCH") TaskExecuteType taskExecuteType,
                                                @RequestParam("pageNo") Integer pageNo,
                                                @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchWorkflowName = ParameterUtils.handleEscapes(searchWorkflowName);
        searchTaskName = ParameterUtils.handleEscapes(searchTaskName);
        return taskDefinitionService.queryTaskDefinitionListPaging(loginUser, projectCode, searchWorkflowName,
                searchTaskName, taskType, taskExecuteType, pageNo, pageSize);
    }

    /**
     * 生成一批任务编码。
     *
     * @param loginUser 登录用户
     * @param genNum 生成数量
     * @return 任务编码列表
     */
    @ApiOperation(value = "genTaskCodeList", notes = "GEN_TASK_CODE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "genNum", value = "GEN_NUM", required = true, dataTypeClass = int.class, example = "1")
    })
    @GetMapping(value = "/gen-task-codes")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result genTaskCodeList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("genNum") Integer genNum) {
        Map<String, Object> result = taskDefinitionService.genTaskCodeList(genNum);
        return returnDataList(result);
    }

    /**
     * 发布（上线/下线）任务定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 任务定义编码
     * @param releaseState 发布状态
     * @return 更新结果
     */
    @ApiOperation(value = "releaseTaskDefinition", notes = "RELEASE_TASK_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "code", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
            @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = true, dataTypeClass = ReleaseState.class)
    })
    @PostMapping(value = "/{code}/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_TASK_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseTaskDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @PathVariable(value = "code", required = true) long code,
                                        @RequestParam(value = "releaseState", required = true, defaultValue = "OFFLINE") ReleaseState releaseState) {
        Map<String, Object> result =
                taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, code, releaseState);
        return returnDataList(result);
    }
}
