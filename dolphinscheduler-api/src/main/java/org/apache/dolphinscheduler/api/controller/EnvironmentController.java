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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ENVIRONMENT_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ENVIRONMENT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_ENVIRONMENT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

/**
 * 环境配置控制器。提供环境配置的增删改查REST API，包括环境创建、更新、查询详情、分页列表、删除和名称校验操作。
 * 环境配置关联Worker组，用于任务运行时环境隔离。
 */
@Api(tags = "ENVIRONMENT_TAG")
@RestController
@RequestMapping("environment")
public class EnvironmentController extends BaseController {

    @Autowired
    private EnvironmentService environmentService;

    /**
     * 创建环境配置。新增环境配置信息并关联Worker组。
     *
     * @param loginUser 当前登录用户
     * @param name 环境名称
     * @param config 环境配置
     * @param description 环境描述
     * @return 创建结果（若名称已存在则返回错误）
     */
    @ApiOperation(value = "createEnvironment", notes = "CREATE_ENVIRONMENT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "ENVIRONMENT_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "config", value = "ENVIRONMENT_CONFIG", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "ENVIRONMENT_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "workerGroups", value = "WORKER_GROUP_LIST", dataTypeClass = String.class)
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createEnvironment(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("name") String name,
                                    @RequestParam("config") String config,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam(value = "workerGroups", required = false) String workerGroups) {

        Map<String, Object> result =
                environmentService.createEnvironment(loginUser, name, config, description, workerGroups);
        return returnDataList(result);
    }

    /**
     * 更新环境配置。根据环境编码修改环境配置信息和关联的Worker组。
     *
     * @param loginUser 当前登录用户
     * @param code 环境编码
     * @param name 环境名称
     * @param config 环境配置
     * @param description 环境描述
     * @return 更新结果状态码
     */
    @ApiOperation(value = "updateEnvironment", notes = "UPDATE_ENVIRONMENT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "ENVIRONMENT_CODE", required = true, dataTypeClass = long.class, example = "100"),
            @ApiImplicitParam(name = "name", value = "ENVIRONMENT_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "config", value = "ENVIRONMENT_CONFIG", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "ENVIRONMENT_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "workerGroups", value = "WORKER_GROUP_LIST", dataTypeClass = String.class)
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateEnvironment(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("code") Long code,
                                    @RequestParam("name") String name,
                                    @RequestParam("config") String config,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam(value = "workerGroups", required = false) String workerGroups) {
        Map<String, Object> result =
                environmentService.updateEnvironmentByCode(loginUser, code, name, config, description, workerGroups);
        return returnDataList(result);
    }

    /**
     * 根据编码查询环境配置详情。
     *
     * @param environmentCode 环境编码
     * @return 环境配置详细信息
     */
    @ApiOperation(value = "queryEnvironmentByCode", notes = "QUERY_ENVIRONMENT_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/query-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ENVIRONMENT_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryEnvironmentByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("environmentCode") Long environmentCode) {

        Map<String, Object> result = environmentService.queryEnvironmentByCode(environmentCode);
        return returnDataList(result);
    }

    /**
     * 分页查询环境配置列表。
     *
     * @param searchVal 搜索值
     * @param pageSize 每页大小
     * @param pageNo 页码
     * @return 环境配置分页列表
     */
    @ApiOperation(value = "queryEnvironmentListPaging", notes = "QUERY_ENVIRONMENT_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryEnvironmentListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam(value = "searchVal", required = false) String searchVal,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam("pageNo") Integer pageNo) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = environmentService.queryEnvironmentListPaging(loginUser, pageNo, pageSize, searchVal);
        return result;
    }

    /**
     * 根据编码删除环境配置。
     *
     * @param loginUser 当前登录用户
     * @param environmentCode 环境编码
     * @return 删除结果状态码
     */
    @ApiOperation(value = "deleteEnvironmentByCode", notes = "DELETE_ENVIRONMENT_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteEnvironment(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("environmentCode") Long environmentCode) {

        Map<String, Object> result = environmentService.deleteEnvironmentByCode(loginUser, environmentCode);
        return returnDataList(result);
    }

    /**
     * 查询所有环境配置列表。
     *
     * @param loginUser 当前登录用户
     * @return 全部环境配置列表
     */
    @ApiOperation(value = "queryAllEnvironmentList", notes = "QUERY_ALL_ENVIRONMENT_LIST_NOTES")
    @GetMapping(value = "/query-environment-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllEnvironmentList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = environmentService.queryAllEnvironmentList(loginUser);
        return returnDataList(result);
    }

    /**
     * 校验环境名称是否已存在。
     *
     * @param loginUser 当前登录用户
     * @param environmentName 环境名称
     * @return 校验结果，名称不存在返回true，否则返回false
     */
    @ApiOperation(value = "verifyEnvironment", notes = "VERIFY_ENVIRONMENT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "environmentName", value = "ENVIRONMENT_NAME", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/verify-environment")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_ENVIRONMENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyEnvironment(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "environmentName") String environmentName) {
        Map<String, Object> result = environmentService.verifyEnvironment(environmentName);
        return returnDataList(result);
    }
}
