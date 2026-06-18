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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_USER_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_USER_INFO_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_USER_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.REVOKE_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.USER_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_USERNAME_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 用户控制器，提供用户的创建、更新、删除、查询、注册、激活及项目/资源/数据源授权等 REST API。
 */
@Api(tags = "USERS_TAG")
@RestController
@RequestMapping("/users")
public class UsersController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UsersService usersService;

    /**
     * 创建用户。
     *
     * @param loginUser 登录用户
     * @param userName 用户名
     * @param userPassword 用户密码
     * @param tenantId 租户 ID
     * @param queue 队列
     * @param email 邮箱
     * @param phone 电话
     * @return 创建结果
     */
    @ApiOperation(value = "createUser", notes = "CREATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "tenantId", value = "TENANT_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "queue", value = "QUEUE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "email", value = "EMAIL", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "phone", value = "PHONE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "state", value = "STATE", dataTypeClass = int.class, example = "1")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "userPassword"})
    public Result createUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "userName") String userName,
                             @RequestParam(value = "userPassword") String userPassword,
                             @RequestParam(value = "tenantId") int tenantId,
                             @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "state", required = false) int state) throws Exception {
        Map<String, Object> result =
                usersService.createUser(loginUser, userName, userPassword, email, tenantId, phone, queue, state);
        return returnDataList(result);
    }

    /**
     * 查询用户分页列表。
     *
     * @param loginUser 登录用户
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param searchVal 搜索关键词
     * @return 用户分页列表
     */
    @ApiOperation(value = "queryUserList", notes = "QUERY_USER_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class)
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_USER_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUserList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("pageNo") Integer pageNo,
                                @RequestParam("pageSize") Integer pageSize,
                                @RequestParam(value = "searchVal", required = false) String searchVal) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;

        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = usersService.queryUserList(loginUser, searchVal, pageNo, pageSize);
        return result;
    }

    /**
     * 更新用户。
     *
     * @param loginUser 登录用户
     * @param id 用户 ID
     * @param userName 用户名
     * @param userPassword 用户密码
     * @param queue 队列
     * @param email 邮箱
     * @param tenantId 租户 ID
     * @param phone 电话
     * @return 更新结果
     */
    @ApiOperation(value = "updateUser", notes = "UPDATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "tenantId", value = "TENANT_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "queue", value = "QUEUE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "email", value = "EMAIL", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "phone", value = "PHONE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "state", value = "STATE", dataTypeClass = int.class, example = "1")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"loginUser", "userPassword"})
    public Result updateUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "id") int id,
                             @RequestParam(value = "userName") String userName,
                             @RequestParam(value = "userPassword") String userPassword,
                             @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "tenantId") int tenantId,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "state", required = false) int state,
                             @RequestParam(value = "timeZone", required = false) String timeZone) throws Exception {
        Map<String, Object> result = usersService.updateUser(loginUser, id, userName, userPassword, email, tenantId,
                phone, queue, state, timeZone);
        return returnDataList(result);
    }

    /**
     * 根据 ID 删除用户。
     *
     * @param loginUser 登录用户
     * @param id 用户 ID
     * @return 删除结果
     */
    @ApiOperation(value = "delUserById", notes = "DELETE_USER_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_USER_BY_ID_ERROR)
    @AccessLogAnnotation
    public Result delUserById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "id") int id) throws Exception {
        Map<String, Object> result = usersService.deleteUserById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * 为用户授权项目。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @param projectIds 项目 ID 列表
     * @return 授权结果
     */
    @ApiOperation(value = "grantProject", notes = "GRANT_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "projectIds", value = "PROJECT_IDS", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/grant-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_PROJECT_ERROR)
    @AccessLogAnnotation
    public Result grantProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "userId") int userId,
                               @RequestParam(value = "projectIds") String projectIds) {
        Map<String, Object> result = usersService.grantProject(loginUser, userId, projectIds);
        return returnDataList(result);
    }

    /**
     * 根据项目编码为用户授权。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @param projectCode 项目编码
     * @return 授权结果
     */
    @ApiOperation(value = "grantProjectByCode", notes = "GRANT_PROJECT_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class)
    })
    @PostMapping(value = "/grant-project-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_PROJECT_ERROR)
    @AccessLogAnnotation
    public Result grantProjectByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "userId") int userId,
                                     @RequestParam(value = "projectCode") long projectCode) {
        Map<String, Object> result = this.usersService.grantProjectByCode(loginUser, userId, projectCode);
        return this.returnDataList(result);
    }

    /**
     * 撤销用户对项目的权限。
     *
     * @param loginUser   登录用户
     * @param userId      用户 ID
     * @param projectCode 项目编码
     * @return 撤销结果
     */
    @ApiOperation(value = "revokeProject", notes = "REVOKE_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @PostMapping(value = "/revoke-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(REVOKE_PROJECT_ERROR)
    @AccessLogAnnotation
    public Result revokeProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "userId") int userId,
                                @RequestParam(value = "projectCode") long projectCode) {
        Map<String, Object> result = this.usersService.revokeProject(loginUser, userId, projectCode);
        return returnDataList(result);
    }

    /**
     * 为用户授权资源。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @param resourceIds 资源 ID 列表
     * @return 授权结果
     */
    @ApiOperation(value = "grantResource", notes = "GRANT_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "resourceIds", value = "RESOURCE_IDS", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/grant-file")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_RESOURCE_ERROR)
    @AccessLogAnnotation
    public Result grantResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "userId") int userId,
                                @RequestParam(value = "resourceIds") String resourceIds) {
        Map<String, Object> result = usersService.grantResources(loginUser, userId, resourceIds);
        return returnDataList(result);
    }

    /**
     * 为用户授权 UDF 函数。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @param udfIds UDF 函数 ID 列表
     * @return 授权结果
     */
    @ApiOperation(value = "grantUDFFunc", notes = "GRANT_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "udfIds", value = "UDF_IDS", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/grant-udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result grantUDFFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "userId") int userId,
                               @RequestParam(value = "udfIds") String udfIds) {
        Map<String, Object> result = usersService.grantUDFFunction(loginUser, userId, udfIds);
        return returnDataList(result);
    }

    /**
     * 为用户授权 K8s 命名空间。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @param namespaceIds 命名空间 ID 列表
     * @return 授权结果
     */
    @ApiOperation(value = "grantNamespace", notes = "GRANT_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "namespaceIds", value = "NAMESPACE_IDS", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/grant-namespace")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation
    public Result grantNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "userId") int userId,
                                 @RequestParam(value = "namespaceIds") String namespaceIds) {
        Map<String, Object> result = usersService.grantNamespaces(loginUser, userId, namespaceIds);
        return returnDataList(result);
    }

    /**
     * 为用户授权数据源。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @param datasourceIds 数据源 ID 列表
     * @return 授权结果
     */
    @ApiOperation(value = "grantDataSource", notes = "GRANT_DATASOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "datasourceIds", value = "DATASOURCE_IDS", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/grant-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_DATASOURCE_ERROR)
    @AccessLogAnnotation
    public Result grantDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "userId") int userId,
                                  @RequestParam(value = "datasourceIds") String datasourceIds) {
        Map<String, Object> result = usersService.grantDataSource(loginUser, userId, datasourceIds);
        return returnDataList(result);
    }

    /**
     * 获取当前登录用户信息。
     *
     * @param loginUser 登录用户
     * @return 用户信息
     */
    @ApiOperation(value = "getUserInfo", notes = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/get-user-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_USER_INFO_ERROR)
    @AccessLogAnnotation
    public Result getUserInfo(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = usersService.getUserInfo(loginUser);
        return returnDataList(result);
    }

    /**
     * 查询所有普通用户列表（不分页）。
     *
     * @param loginUser 登录用户
     * @return 用户列表
     */
    @ApiOperation(value = "listUser", notes = "LIST_USER_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    @AccessLogAnnotation
    public Result listUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = usersService.queryAllGeneralUsers(loginUser);
        return returnDataList(result);
    }

    /**
     * 查询所有用户列表（不分页）。
     *
     * @param loginUser 登录用户
     * @return 用户列表
     */
    @GetMapping(value = "/list-all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    @AccessLogAnnotation
    public Result listAll(@RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = usersService.queryUserList(loginUser);
        return returnDataList(result);
    }

    /**
     * 验证用户名是否可用。
     *
     * @param loginUser 登录用户
     * @param userName 用户名
     * @return 用户名不存在则返回 true，否则返回 false
     */
    @ApiOperation(value = "verifyUserName", notes = "VERIFY_USER_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataTypeClass = String.class)
    })
    @GetMapping(value = "/verify-user-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_USERNAME_ERROR)
    @AccessLogAnnotation
    public Result verifyUserName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "userName") String userName) {
        return usersService.verifyUserName(userName);
    }

    /**
     * 查询未授权给定告警组的用户。
     *
     * @param loginUser 登录用户
     * @param alertgroupId 告警组 ID
     * @return 未授权用户列表
     */
    @ApiOperation(value = "unauthorizedUser", notes = "UNAUTHORIZED_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertgroupId", value = "ALERT_GROUP_ID", required = true, dataTypeClass = String.class)
    })
    @GetMapping(value = "/unauth-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UNAUTHORIZED_USER_ERROR)
    @AccessLogAnnotation
    public Result unauthorizedUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("alertgroupId") Integer alertgroupId) {
        Map<String, Object> result = usersService.unauthorizedUser(loginUser, alertgroupId);
        return returnDataList(result);
    }

    /**
     * 查询已授权给定告警组的用户。
     *
     * @param loginUser 登录用户
     * @param alertgroupId 告警组 ID
     * @return 已授权用户列表
     */
    @ApiOperation(value = "authorizedUser", notes = "AUTHORIZED_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertgroupId", value = "ALERT_GROUP_ID", required = true, dataTypeClass = String.class)
    })
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(AUTHORIZED_USER_ERROR)
    @AccessLogAnnotation
    public Result authorizedUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("alertgroupId") Integer alertgroupId) {
        try {
            Map<String, Object> result = usersService.authorizedUser(loginUser, alertgroupId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.AUTHORIZED_USER_ERROR.getMsg(), e);
            return error(Status.AUTHORIZED_USER_ERROR.getCode(), Status.AUTHORIZED_USER_ERROR.getMsg());
        }
    }

    /**
     * 用户注册。
     *
     * @param userName 用户名
     * @param userPassword 用户密码
     * @param repeatPassword 重复密码
     * @param email 邮箱
     */
    @ApiOperation(value = "registerUser", notes = "REGISTER_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "repeatPassword", value = "REPEAT_PASSWORD", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "email", value = "EMAIL", required = true, dataTypeClass = String.class),
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_USER_ERROR)
    @AccessLogAnnotation
    public Result<Object> registerUser(@RequestParam(value = "userName") String userName,
                                       @RequestParam(value = "userPassword") String userPassword,
                                       @RequestParam(value = "repeatPassword") String repeatPassword,
                                       @RequestParam(value = "email") String email) throws Exception {
        userName = ParameterUtils.handleEscapes(userName);
        userPassword = ParameterUtils.handleEscapes(userPassword);
        repeatPassword = ParameterUtils.handleEscapes(repeatPassword);
        email = ParameterUtils.handleEscapes(email);
        Map<String, Object> result = usersService.registerUser(userName, userPassword, repeatPassword, email);
        return returnDataList(result);
    }

    /**
     * 激活用户。
     *
     * @param loginUser 登录用户
     * @param userName 用户名
     */
    @ApiOperation(value = "activateUser", notes = "ACTIVATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", dataTypeClass = String.class),
    })
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @AccessLogAnnotation
    public Result<Object> activateUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "userName") String userName) {
        userName = ParameterUtils.handleEscapes(userName);
        Map<String, Object> result = usersService.activateUser(loginUser, userName);
        return returnDataList(result);
    }

    /**
     * 批量激活用户。
     *
     * @param loginUser 登录用户
     * @param userNames 用户名列表
     */
    @ApiOperation(value = "batchActivateUser", notes = "BATCH_ACTIVATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "USER_NAMES", required = true, dataTypeClass = List.class),
    })
    @PostMapping("/batch/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @AccessLogAnnotation
    public Result<Object> batchActivateUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestBody List<String> userNames) {
        List<String> formatUserNames =
                userNames.stream().map(ParameterUtils::handleEscapes).collect(Collectors.toList());
        Map<String, Object> result = usersService.batchActivateUser(loginUser, formatUserNames);
        return returnDataList(result);
    }
}
