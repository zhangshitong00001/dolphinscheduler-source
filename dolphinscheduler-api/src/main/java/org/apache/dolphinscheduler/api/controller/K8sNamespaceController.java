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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_K8S_NAMESPACE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUTHORIZED_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_CAN_USE_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_UNAUTHORIZED_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_K8S_NAMESPACE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.K8sNamespaceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
 * K8s命名空间控制器。提供K8s命名空间的增删改查REST API，包括命名空间创建、更新、分页查询、校验、
 * 删除、授权/未授权管理以及可用命名空间列表查询等操作。
 */
@Api(tags = "K8S_NAMESPACE_TAG")
@RestController
@RequestMapping("/k8s-namespace")
public class K8sNamespaceController extends BaseController {


    @Autowired
    private K8sNamespaceService k8sNamespaceService;

    /**
     * 分页查询K8s命名空间列表。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索值
     * @param pageSize 每页大小
     * @param pageNo 页码
     * @return 命名空间分页列表
     */
    @ApiOperation(value = "queryNamespaceListPaging", notes = "QUERY_NAMESPACE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryNamespaceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "searchVal", required = false) String searchVal,
                                           @RequestParam("pageSize") Integer pageSize,
                                           @RequestParam("pageNo") Integer pageNo) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = k8sNamespaceService.queryListPaging(loginUser, searchVal, pageNo, pageSize);
        return result;
    }

    /**
     * 创建K8s命名空间。若K8s上不存在则创建，若已存在则仅在数据库注册。
     *
     * @param loginUser 当前登录用户
     * @param namespace K8s命名空间名称
     * @param clusterCode 集群编码
     * @param limitsCpu CPU限制
     * @param limitsMemory 内存限制
     * @return 创建结果
     */
    @ApiOperation(value = "createK8sNamespace", notes = "CREATE_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "namespace", value = "NAMESPACE", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "clusterCode", value = "CLUSTER_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "limits_cpu", value = "LIMITS_CPU", required = false, dataTypeClass = double.class),
            @ApiImplicitParam(name = "limits_memory", value = "LIMITS_MEMORY", required = false, dataTypeClass = int.class)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "namespace") String namespace,
                                  @RequestParam(value = "clusterCode") Long clusterCode,
                                  @RequestParam(value = "limitsCpu", required = false) Double limitsCpu,
                                  @RequestParam(value = "limitsMemory", required = false) Integer limitsMemory) {
        Map<String, Object> result =
                k8sNamespaceService.createK8sNamespace(loginUser, namespace, clusterCode, limitsCpu, limitsMemory);
        return returnDataList(result);
    }

    /**
     * 更新K8s命名空间。不允许修改命名空间名称和K8s集群，仅可修改所属用户和资源限制。
     *
     * @param loginUser 当前登录用户
     * @param userName 命名空间所属用户
     * @param limitsCpu CPU限制
     * @param limitsMemory 内存限制
     * @return 更新结果
     */
    @ApiOperation(value = "updateK8sNamespace", notes = "UPDATE_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "K8S_NAMESPACE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "userName", value = "OWNER", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "limitsCpu", value = "LIMITS_CPU", required = false, dataTypeClass = double.class),
            @ApiImplicitParam(name = "limitsMemory", value = "LIMITS_MEMORY", required = false, dataTypeClass = int.class)})
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @PathVariable(value = "id") int id,
                                  @RequestParam(value = "userName", required = false) String userName,
                                  @RequestParam(value = "tag", required = false) String tag,
                                  @RequestParam(value = "limitsCpu", required = false) Double limitsCpu,
                                  @RequestParam(value = "limitsMemory", required = false) Integer limitsMemory) {
        Map<String, Object> result =
                k8sNamespaceService.updateK8sNamespace(loginUser, id, userName, limitsCpu, limitsMemory);
        return returnDataList(result);
    }

    /**
     * 校验K8s命名空间是否已存在。同一K8s集群下的命名空间名称必须唯一。
     *
     * @param loginUser 当前登录用户
     * @param namespace 命名空间名称
     * @param clusterCode 集群编码
     * @return 校验结果，命名空间不存在返回true，否则返回false
     */
    @ApiOperation(value = "verifyNamespaceK8s", notes = "VERIFY_NAMESPACE_K8S_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "namespace", value = "NAMESPACE", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "clusterCode", value = "CLUSTER_CODE", required = true, dataTypeClass = long.class),
    })
    @PostMapping(value = "/verify")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "namespace") String namespace,
                                  @RequestParam(value = "clusterCode") Long clusterCode) {

        return k8sNamespaceService.verifyNamespaceK8s(namespace, clusterCode);
    }

    /**
     * 根据ID删除K8s命名空间。
     *
     * @param loginUser 当前登录用户
     * @param id 命名空间ID
     * @return 删除结果状态码
     */
    @ApiOperation(value = "delNamespaceById", notes = "DELETE_NAMESPACE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "NAMESPACE_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_K8S_NAMESPACE_BY_ID_ERROR)
    @AccessLogAnnotation
    public Result delNamespaceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id) {
        Map<String, Object> result = k8sNamespaceService.deleteNamespaceById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * 查询未授权给指定用户的命名空间列表。
     *
     * @param loginUser 当前登录用户
     * @param userId 用户ID
     * @return 未授权的命名空间列表
     */
    @ApiOperation(value = "queryUnauthorizedNamespace", notes = "QUERY_UNAUTHORIZED_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/unauth-namespace")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UNAUTHORIZED_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUnauthorizedNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("userId") Integer userId) {
        Map<String, Object> result = k8sNamespaceService.queryUnauthorizedNamespace(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * 查询已授权给指定用户的命名空间列表。
     *
     * @param loginUser 当前登录用户
     * @param userId 用户ID
     * @return 已授权的命名空间列表
     */
    @ApiOperation(value = "queryAuthorizedNamespace", notes = "QUERY_AUTHORIZED_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/authed-namespace")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUTHORIZED_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuthorizedNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("userId") Integer userId) {
        Map<String, Object> result = k8sNamespaceService.queryAuthorizedNamespace(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * 查询当前用户可用的K8s命名空间列表。
     *
     * @param loginUser 当前登录用户
     * @return 可用命名空间列表
     */
    @ApiOperation(value = "queryAvailableNamespaceList", notes = "QUERY_AVAILABLE_NAMESPACE_LIST_NOTES")
    @GetMapping(value = "/available-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CAN_USE_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAvailableNamespaceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<K8sNamespace> result = k8sNamespaceService.queryNamespaceAvailable(loginUser);
        return success(result);
    }
}
