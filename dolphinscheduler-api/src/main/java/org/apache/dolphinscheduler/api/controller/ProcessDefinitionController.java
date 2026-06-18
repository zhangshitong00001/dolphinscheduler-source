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

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_COPY_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_MOVE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINE_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.IMPORT_PROCESS_DEFINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_ALL_VARIABLES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 流程定义控制器，提供工作流定义的创建、更新、删除、发布、导入导出等 REST API。
 */
@Api(tags = "PROCESS_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/process-definition")
public class ProcessDefinitionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 创建流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @param description 描述
     * @param globalParams 全局参数
     * @param locations 节点位置信息
     * @param timeout 超时时间
     * @param tenantCode 租户编码
     * @param taskRelationJson 节点关系 JSON
     * @param taskDefinitionJson 任务定义 JSON
     * @param otherParamsJson 其他参数 JSON
     * @return 创建结果
     */
    @ApiOperation(value = "createProcessDefinition", notes = "CREATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "otherParamsJson", value = "OTHER_PARAMS_JSON", required = false, dataTypeClass = String.class)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                          @RequestParam(value = "tenantCode", required = true) String tenantCode,
                                          @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                          @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                          @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                          @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType) {
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectCode, name,
                description, globalParams,
                locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
        return returnDataList(result);
    }

    /**
     * 批量复制流程定义到目标项目。
     *
     * @param loginUser 登录用户
     * @param projectCode 源项目编码
     * @param codes 流程定义编码列表
     * @param targetProjectCode 目标项目编码
     * @return 复制结果
     */
    @ApiOperation(value = "batchCopyByCodes", notes = "COPY_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "PROCESS_DEFINITION_CODES", required = true, dataTypeClass = String.class, example = "3,4"),
            @ApiImplicitParam(name = "targetProjectCode", value = "TARGET_PROJECT_CODE", required = true, dataTypeClass = long.class, example = "123")
    })
    @PostMapping(value = "/batch-copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result copyProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "codes", required = true) String codes,
                                        @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(
                processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, codes, targetProjectCode));
    }

    /**
     * 批量移动流程定义到目标项目。
     *
     * @param loginUser 登录用户
     * @param projectCode 源项目编码
     * @param codes 流程定义编码列表
     * @param targetProjectCode 目标项目编码
     * @return 移动结果
     */
    @ApiOperation(value = "batchMoveByCodes", notes = "MOVE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "PROCESS_DEFINITION_CODES", required = true, dataTypeClass = String.class, example = "3,4"),
            @ApiImplicitParam(name = "targetProjectCode", value = "TARGET_PROJECT_CODE", required = true, dataTypeClass = long.class, example = "123")
    })
    @PostMapping(value = "/batch-move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result moveProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @RequestParam(value = "codes", required = true) String codes,
                                        @RequestParam(value = "targetProjectCode", required = true) long targetProjectCode) {
        return returnDataList(
                processDefinitionService.batchMoveProcessDefinition(loginUser, projectCode, codes, targetProjectCode));
    }

    /**
     * 验证流程定义名称是否唯一。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @return 名称不存在则返回 true，否则返回 false
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_PROCESS_DEFINITION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = false, dataTypeClass = Long.class),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyProcessDefinitionName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @RequestParam(value = "name", required = true) String name,
                                              @RequestParam(value = "code", required = false, defaultValue = "0") long processDefinitionCode) {
        Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(loginUser, projectCode, name,
                processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * 更新流程定义，包含任务定义、任务关系和节点位置等完整信息。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @param code 流程定义编码
     * @param description 描述
     * @param globalParams 全局参数
     * @param locations 节点位置信息
     * @param timeout 超时时间
     * @param tenantCode 租户编码
     * @param taskRelationJson 节点关系 JSON
     * @param taskDefinitionJson 任务定义 JSON
     * @param otherParamsJson 其他参数 JSON
     * @return 更新结果
     */
    @ApiOperation(value = "update", notes = "UPDATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, dataTypeClass = ReleaseState.class),
            @ApiImplicitParam(name = "otherParamsJson", value = "OTHER_PARAMS_JSON", required = false, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "name", required = true) String name,
                                          @PathVariable(value = "code", required = true) long code,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                          @RequestParam(value = "tenantCode", required = true) String tenantCode,
                                          @RequestParam(value = "taskRelationJson", required = true) String taskRelationJson,
                                          @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson,
                                          @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                          @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType,
                                          @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectCode, name,
                code, description, globalParams,
                locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
        // If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        // Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            result = processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
        }
        return returnDataList(result);
    }

    /**
     * 查询流程定义版本分页列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param pageNo 当前页码
     * @param pageSize 每页大小
     * @param code 流程定义编码
     * @return 流程定义版本列表
     */
    @ApiOperation(value = "queryVersions", notes = "QUERY_PROCESS_DEFINITION_VERSIONS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1")
    })
    @GetMapping(value = "/{code}/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_VERSIONS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionVersions(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @RequestParam(value = "pageNo") int pageNo,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 @PathVariable(value = "code") long code) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        result = processDefinitionService.queryProcessDefinitionVersions(loginUser, projectCode, pageNo, pageSize,
                code);

        return result;
    }

    /**
     * 切换流程图到指定版本。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @param version 要切换到的版本号
     * @return 切换结果
     */
    @ApiOperation(value = "switchVersion", notes = "SWITCH_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result switchProcessDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable(value = "code") long code,
                                                 @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                processDefinitionService.switchProcessDefinitionVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * 删除指定流程定义的某个版本。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @param version 要删除的版本号
     * @return 删除结果
     */
    @ApiOperation(value = "deleteVersion", notes = "DELETE_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "1"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionVersion(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                 @PathVariable(value = "code") long code,
                                                 @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                processDefinitionService.deleteProcessDefinitionVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * 发布（上线/下线）流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @param releaseState 发布状态
     * @return 发布结果
     */
    @ApiOperation(value = "release", notes = "RELEASE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
            @ApiImplicitParam(name = "releaseState", value = "PROCESS_DEFINITION_RELEASE", required = true, dataTypeClass = ReleaseState.class),
    })
    @PostMapping(value = "/{code}/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                           @PathVariable(value = "code", required = true) long code,
                                           @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {
        Map<String, Object> result =
                processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
        return returnDataList(result);
    }

    /**
     * 根据编码查询流程定义详情。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @return 流程定义详情
     */
    @ApiOperation(value = "queryProcessDefinitionByCode", notes = "QUERY_PROCESS_DEFINITION_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789")
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @PathVariable(value = "code", required = true) long code) {
        Map<String, Object> result =
                processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * 根据名称查询流程定义详情。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @return 流程定义详情
     */
    @ApiOperation(value = "queryProcessDefinitionByName", notes = "QUERY_PROCESS_DEFINITION_BY_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class)
    })
    @GetMapping(value = "/query-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ProcessDefinition> queryProcessDefinitionByName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                  @RequestParam("name") String name) {
        Map<String, Object> result =
                processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, name);
        return returnDataList(result);
    }

    /**
     * 查询流程定义列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @return 流程定义列表
     */
    @ApiOperation(value = "queryList", notes = "QUERY_PROCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * 查询流程定义简单列表（不含任务详情）。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @return 流程定义简单列表
     */
    @ApiOperation(value = "querySimpleList", notes = "QUERY_PROCESS_DEFINITION_SIMPLE_LIST_NOTES")
    @GetMapping(value = "/simple-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProcessDefinitionSimpleList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionSimpleList(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * 查询流程定义分页列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param searchVal 搜索关键词
     * @param otherParamsJson 其他参数 JSON
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param userId 用户 ID
     * @return 流程定义分页数据
     */
    @ApiOperation(value = "queryListPaging", notes = "QUERY_PROCESS_DEFINITION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = false, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "otherParamsJson", value = "OTHER_PARAMS_JSON", required = false, dataTypeClass = String.class)
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<ProcessDefinition>> queryProcessDefinitionListPaging(
                                                                                @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                                @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                                @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                                                                @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                                                @RequestParam("pageNo") Integer pageNo,
                                                                                @RequestParam("pageSize") Integer pageSize) {

        Result<PageInfo<ProcessDefinition>> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        PageInfo<ProcessDefinition> pageInfo = processDefinitionService.queryProcessDefinitionListPaging(
                loginUser, projectCode, searchVal, otherParamsJson, userId, pageNo, pageSize);
        return Result.success(pageInfo);

    }

    /**
     * 封装树形视图结构，用于前端 DAG 展示。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @param limit 节点数量上限
     * @return 树形视图 JSON 数据
     */
    @ApiOperation(value = "viewTree", notes = "VIEW_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{code}/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                           @PathVariable("code") long code,
                           @RequestParam("limit") Integer limit) {
        Map<String, Object> result = processDefinitionService.viewTree(loginUser, projectCode, code, limit);
        return returnDataList(result);
    }

    /**
     * 根据流程定义编码获取任务节点列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @return 任务列表
     */
    @ApiOperation(value = "getTasksByDefinitionCode", notes = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/{code}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListByDefinitionCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable("code") long code) {
        Map<String, Object> result =
                processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * 根据多个流程定义编码批量获取任务节点列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param codes 流程定义编码列表
     * @return 任务节点列表数据
     */
    @ApiOperation(value = "getTaskListByDefinitionCodes", notes = "GET_TASK_LIST_BY_DEFINITION_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "PROCESS_DEFINITION_CODES", required = true, dataTypeClass = String.class, example = "100,200,300")
    })
    @GetMapping(value = "/batch-query-tasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getNodeListMapByDefinitionCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                  @RequestParam("codes") String codes) {
        Map<String, Object> result =
                processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, codes);
        return returnDataList(result);
    }

    /**
     * 根据项目编码获取流程定义列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @return 流程定义列表数据
     */
    @ApiOperation(value = "getProcessListByProjectCode", notes = "GET_PROCESS_LIST_BY_PROCESS_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/query-process-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getProcessListByProjectCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionListByProjectCode(projectCode);
        return returnDataList(result);
    }

    /**
     * 根据流程定义编码获取任务定义列表。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @return 流程定义列表数据
     */
    @ApiOperation(value = "getTaskListByProcessDefinitionCode", notes = "GET_TASK_LIST_BY_PROCESS_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class, example = "100"),
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "100"),
    })
    @GetMapping(value = "/query-task-definition-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    public Result getTaskListByProcessDefinitionCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                     @RequestParam(value = "processDefinitionCode") Long processDefinitionCode) {
        Map<String, Object> result = processDefinitionService
                .queryTaskDefinitionListByProcessDefinitionCode(projectCode, processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * 根据编码删除流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @return 删除结果
     */
    @ApiOperation(value = "deleteByCode", notes = "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteProcessDefinitionByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @PathVariable("code") long code) {
        Map<String, Object> result =
                processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * 批量删除流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param codes 流程定义编码列表
     * @return 删除结果
     */
    @ApiOperation(value = "batchDeleteByCodes", notes = "BATCH_DELETE_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchDeleteProcessDefinitionByCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                      @RequestParam("codes") String codes) {
        Map<String, Object> result = new HashMap<>();
        Set<String> deleteFailedCodeSet = new HashSet<>();
        if (!StringUtils.isEmpty(codes)) {
            String[] processDefinitionCodeArray = codes.split(",");
            for (String strProcessDefinitionCode : processDefinitionCodeArray) {
                long code = Long.parseLong(strProcessDefinitionCode);
                try {
                    Map<String, Object> deleteResult =
                            processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, code);
                    if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                        deleteFailedCodeSet.add((String) deleteResult.get(Constants.MSG));
                        logger.error((String) deleteResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    deleteFailedCodeSet.add(MessageFormat.format(Status.DELETE_PROCESS_DEFINE_BY_CODES_ERROR.getMsg(),
                            strProcessDefinitionCode));
                }
            }
        }

        if (!deleteFailedCodeSet.isEmpty()) {
            putMsg(result, BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR, String.join("\n", deleteFailedCodeSet));
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return returnDataList(result);
    }

    /**
     * 批量导出流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param codes 流程定义编码列表
     * @param response HTTP 响应
     */
    @ApiOperation(value = "batchExportByCodes", notes = "BATCH_EXPORT_PROCESS_DEFINITION_BY_CODES_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codes", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/batch-export")
    @ResponseBody
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "response"})
    public void batchExportProcessDefinitionByCodes(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                    @RequestParam("codes") String codes,
                                                    HttpServletResponse response) {
        try {
            processDefinitionService.batchExportProcessDefinitionByCodes(loginUser, projectCode, codes, response);
        } catch (Exception e) {
            logger.error(Status.BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * 查询项目下所有流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @return 流程定义列表
     */
    @ApiOperation(value = "queryAllByProjectCode", notes = "QUERY_PROCESS_DEFINITION_All_BY_PROJECT_CODE_NOTES")
    @GetMapping(value = "/all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllProcessDefinitionByProjectCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        Map<String, Object> result =
                processDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        return returnDataList(result);
    }

    /**
     * 导入流程定义。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param file 上传的资源文件
     * @return 导入结果
     */
    @ApiOperation(value = "importProcessDefinition", notes = "IMPORT_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataTypeClass = MultipartFile.class)
    })
    @PostMapping(value = "/import")
    @ApiException(IMPORT_PROCESS_DEFINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "file"})
    public Result importProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam("file") MultipartFile file) {
        Map<String, Object> result;
        if ("application/zip".equals(file.getContentType())) {
            result = processDefinitionService.importSqlProcessDefinition(loginUser, projectCode, file);
        } else {
            result = processDefinitionService.importProcessDefinition(loginUser, projectCode, file);
        }
        return returnDataList(result);
    }

    /**
     * 创建空的流程定义（不含任务节点）。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @param description 描述
     * @param globalParams 全局参数
     * @param timeout 超时时间
     * @param tenantCode 租户编码
     * @param scheduleJson 调度配置 JSON
     * @return 流程定义编码
     */
    @ApiOperation(value = "createEmptyProcessDefinition", notes = "CREATE_EMPTY_PROCESS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, dataTypeClass = String.class)
    })
    @PostMapping(value = "/empty")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createEmptyProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                               @RequestParam(value = "name", required = true) String name,
                                               @RequestParam(value = "description", required = false) String description,
                                               @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                               @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                               @RequestParam(value = "tenantCode", required = true) String tenantCode,
                                               @RequestParam(value = "scheduleJson", required = false) String scheduleJson,
                                               @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType) {
        return returnDataList(processDefinitionService.createEmptyProcessDefinition(loginUser, projectCode, name,
                description, globalParams,
                timeout, tenantCode, scheduleJson, executionType));
    }

    /**
     * 更新流程定义基本信息（不包含任务定义、任务关系和位置）。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @param code 流程定义编码
     * @param description 描述
     * @param globalParams 全局参数
     * @param timeout 超时时间
     * @param tenantCode 租户编码
     * @param scheduleJson 调度配置 JSON
     * @param executionType 执行类型
     * @param releaseState 发布状态
     * @param otherParamsJson 其他参数 JSON
     * @return 更新结果
     */
    @ApiOperation(value = "updateBasicInfo", notes = "UPDATE_PROCESS_DEFINITION_BASIC_INFO_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, dataTypeClass = ReleaseState.class),
            @ApiImplicitParam(name = "otherParamsJson", value = "OTHER_PARAMS_JSON", required = false, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{code}/basic-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProcessDefinitionBasicInfo(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                   @RequestParam(value = "name", required = true) String name,
                                                   @PathVariable(value = "code", required = true) long code,
                                                   @RequestParam(value = "description", required = false) String description,
                                                   @RequestParam(value = "globalParams", required = false, defaultValue = "[]") String globalParams,
                                                   @RequestParam(value = "timeout", required = false, defaultValue = "0") int timeout,
                                                   @RequestParam(value = "tenantCode", required = true) String tenantCode,
                                                   @RequestParam(value = "scheduleJson", required = false) String scheduleJson,
                                                   @RequestParam(value = "otherParamsJson", required = false) String otherParamsJson,
                                                   @RequestParam(value = "executionType", defaultValue = "PARALLEL") ProcessExecutionTypeEnum executionType,
                                                   @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {
        Map<String, Object> result = processDefinitionService.updateProcessDefinitionBasicInfo(loginUser, projectCode,
                name, code, description, globalParams,
                timeout, tenantCode, scheduleJson, otherParamsJson, executionType);
        // If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        // Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            result = processDefinitionService.releaseWorkflowAndSchedule(loginUser, projectCode, code, releaseState);
        }
        return returnDataList(result);
    }

    /**
     * 同时发布流程定义和调度配置。
     *
     * @param loginUser 登录用户
     * @param projectCode 项目编码
     * @param code 流程定义编码
     * @param releaseState 发布状态
     * @return 更新结果
     */
    @ApiOperation(value = "releaseWorkflowAndSchedule", notes = "RELEASE_WORKFLOW_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "code", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
            @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = true, dataTypeClass = ReleaseState.class)
    })
    @PostMapping(value = "/{code}/release-workflow")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result releaseWorkflowAndSchedule(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @PathVariable(value = "code", required = true) long code,
                                             @RequestParam(value = "releaseState", required = true, defaultValue = "OFFLINE") ReleaseState releaseState) {
        return returnDataList(
                processDefinitionService.releaseWorkflowAndSchedule(loginUser, projectCode, code, releaseState));
    }

    /**
     * 查询流程定义的全局变量和局部变量。
     *
     * @param loginUser 登录用户
     * @param code 流程定义编码
     * @return 变量数据
     */
    @Operation(summary = "viewVariables", description = "QUERY_PROCESS_DEFINITION_GLOBAL_VARIABLES_AND_LOCAL_VARIABLES_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/{code}/view-variables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_ALL_VARIABLES_ERROR)
    @AccessLogAnnotation
    public Result viewVariables(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                @PathVariable("code") Long code) {
        Map<String, Object> result = processDefinitionService.viewVariables(loginUser, projectCode, code);
        return returnDataList(result);
    }

}
