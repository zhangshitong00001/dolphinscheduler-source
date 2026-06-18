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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ACCESS_TOKEN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ACCESS_TOKEN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GENERATE_TOKEN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ACCESSTOKEN_BY_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ACCESSTOKEN_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ACCESS_TOKEN_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

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

/**
 * 访问令牌控制器。提供访问令牌的增删改查REST API，包括令牌创建、查询、更新和删除操作。
 */
@Api(tags = "ACCESS_TOKEN_TAG")
@RestController
@RequestMapping("/access-tokens")
public class AccessTokenController extends BaseController {

    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * 创建访问令牌。为指定用户生成访问令牌，支持自定义过期时间和令牌字符串（不提供则自动生成）。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @param expireTime 令牌过期时间
     * @param token 令牌字符串（可选，不提供则自动生成）
     * @return 创建结果状态码
     */
    @ApiOperation(value = "createToken", notes = "CREATE_TOKEN_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class),
            @ApiImplicitParam(name = "expireTime", value = "EXPIRE_TIME", required = true, dataTypeClass = String.class, example = "2021-12-31 00:00:00"),
            @ApiImplicitParam(name = "token", value = "TOKEN", required = false, dataTypeClass = String.class, example = "xxxx")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ACCESS_TOKEN_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createToken(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "userId") int userId,
                              @RequestParam(value = "expireTime") String expireTime,
                              @RequestParam(value = "token", required = false) String token) {

        return accessTokenService.createToken(loginUser, userId, expireTime, token);
    }

    /**
     * 生成令牌字符串。为指定用户生成访问令牌并返回令牌字符串。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @param expireTime 令牌过期时间
     * @return 生成的令牌字符串
     */
    @ApiIgnore
    @PostMapping(value = "/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(GENERATE_TOKEN_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result generateToken(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "userId") int userId,
                                @RequestParam(value = "expireTime") String expireTime) {
        Map<String, Object> result = accessTokenService.generateToken(loginUser, userId, expireTime);
        return returnDataList(result);
    }

    /**
     * 分页查询访问令牌列表。
     *
     * @param loginUser 当前登录用户
     * @param pageNo 页码
     * @param searchVal 搜索值
     * @param pageSize 每页大小
     * @return 指定页码和大小的令牌列表
     */
    @ApiOperation(value = "queryAccessTokenList", notes = "QUERY_ACCESS_TOKEN_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ACCESSTOKEN_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAccessTokenList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam(value = "searchVal", required = false) String searchVal,
                                       @RequestParam("pageSize") Integer pageSize) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = accessTokenService.queryAccessTokenList(loginUser, searchVal, pageNo, pageSize);
        return result;
    }

    /**
     * 查询指定用户的访问令牌。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @return 指定用户的令牌列表
     */
    @ApiOperation(value = "queryAccessTokenByUser", notes = "QUERY_ACCESS_TOKEN_BY_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataTypeClass = int.class)
    })
    @GetMapping(value = "/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ACCESSTOKEN_BY_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAccessTokenByUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable("userId") Integer userId) {
        Map<String, Object> result = this.accessTokenService.queryAccessTokenByUser(loginUser, userId);
        return this.returnDataList(result);
    }

    /**
     * 根据ID删除访问令牌。
     *
     * @param loginUser 当前登录用户
     * @param id 令牌ID
     * @return 删除结果状态码
     */
    @ApiIgnore
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ACCESS_TOKEN_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result delAccessTokenById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @PathVariable(value = "id") int id) {
        Map<String, Object> result = accessTokenService.delAccessTokenById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * 更新访问令牌。修改令牌的所属用户、过期时间和令牌字符串。
     *
     * @param loginUser 当前登录用户
     * @param id 令牌ID
     * @param userId 目标用户ID
     * @param expireTime 令牌过期时间
     * @param token 令牌字符串（可选，不提供则自动生成）
     * @return 更新后的访问令牌实体
     */
    @ApiOperation(value = "updateToken", notes = "UPDATE_TOKEN_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "TOKEN_ID", required = true, dataTypeClass = int.class),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class),
            @ApiImplicitParam(name = "expireTime", value = "EXPIRE_TIME", required = true, dataTypeClass = String.class, example = "2021-12-31 00:00:00"),
            @ApiImplicitParam(name = "token", value = "TOKEN", required = false, dataTypeClass = String.class, example = "xxxx")
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ACCESS_TOKEN_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateToken(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @PathVariable(value = "id") int id,
                              @RequestParam(value = "userId") int userId,
                              @RequestParam(value = "expireTime") String expireTime,
                              @RequestParam(value = "token", required = false) String token) {

        Map<String, Object> result = accessTokenService.updateToken(loginUser, id, userId, expireTime, token);
        return returnDataList(result);
    }

}
