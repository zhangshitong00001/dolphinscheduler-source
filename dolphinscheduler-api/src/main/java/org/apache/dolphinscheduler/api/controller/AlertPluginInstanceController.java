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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ALERT_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 告警插件实例控制器。提供告警插件实例的增删改查REST API，包括实例创建、更新、删除、详情查询和分页列表查询。
 */
@Api(tags = "ALERT_PLUGIN_INSTANCE_TAG")
@RestController
@RequestMapping("alert-plugin-instances")
public class AlertPluginInstanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceController.class);

    @Autowired
    private AlertPluginInstanceService alertPluginInstanceService;

    /**
     * 创建告警插件实例。
     *
     * @param loginUser 当前登录用户
     * @param pluginDefineId 告警插件定义ID
     * @param instanceName 实例名称
     * @param pluginInstanceParams 实例参数
     * @return 创建结果
     */
    @ApiOperation(value = "createAlertPluginInstance", notes = "CREATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pluginDefineId", value = "ALERT_PLUGIN_DEFINE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "instanceName", value = "ALERT_PLUGIN_INSTANCE_NAME", required = true, dataTypeClass = String.class, example = "DING TALK"),
            @ApiImplicitParam(name = "pluginInstanceParams", value = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, dataTypeClass = String.class, example = "ALERT_PLUGIN_INSTANCE_PARAMS")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "pluginDefineId") int pluginDefineId,
                                            @RequestParam(value = "instanceName") String instanceName,
                                            @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        Map<String, Object> result =
                alertPluginInstanceService.create(loginUser, pluginDefineId, instanceName, pluginInstanceParams);
        return returnDataList(result);
    }

    /**
     * 更新告警插件实例。
     *
     * @param loginUser 当前登录用户
     * @param id 告警插件实例ID
     * @param instanceName 实例名称
     * @param pluginInstanceParams 实例参数
     * @return 更新结果
     */
    @ApiOperation(value = "updateAlertPluginInstance", notes = "UPDATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertPluginInstanceId", value = "ALERT_PLUGIN_INSTANCE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "instanceName", value = "ALERT_PLUGIN_INSTANCE_NAME", required = true, dataTypeClass = String.class, example = "DING TALK"),
            @ApiImplicitParam(name = "pluginInstanceParams", value = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, dataTypeClass = String.class, example = "ALERT_PLUGIN_INSTANCE_PARAMS")
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable(value = "id") int id,
                                            @RequestParam(value = "instanceName") String instanceName,
                                            @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        Map<String, Object> result =
                alertPluginInstanceService.update(loginUser, id, instanceName, pluginInstanceParams);
        return returnDataList(result);
    }

    /**
     * 删除告警插件实例。
     *
     * @param loginUser 当前登录用户
     * @param id 告警插件实例ID
     * @return 删除结果
     */
    @ApiOperation(value = "deleteAlertPluginInstance", notes = "DELETE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_PLUGIN_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable(value = "id") int id) {

        Map<String, Object> result = alertPluginInstanceService.delete(loginUser, id);
        return returnDataList(result);
    }

    /**
     * 根据ID获取告警插件实例详情。
     *
     * @param loginUser 当前登录用户
     * @param id 告警插件实例ID
     * @return 实例详情
     */
    @ApiOperation(value = "getAlertPluginInstance", notes = "GET_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable(value = "id") int id) {
        Map<String, Object> result = alertPluginInstanceService.get(loginUser, id);
        return returnDataList(result);
    }

    /**
     * 查询所有告警插件实例列表。
     *
     * @param loginUser 当前登录用户
     * @return 实例列表
     */
    @ApiOperation(value = "queryAlertPluginInstanceList", notes = "QUERY_ALL_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = alertPluginInstanceService.queryAll();
        return returnDataList(result);
    }

    /**
     * 检查告警插件实例名称是否已存在。
     *
     * @param loginUser 当前登录用户
     * @param alertInstanceName 告警实例名称
     * @return 校验结果状态码
     */
    @ApiOperation(value = "verifyAlertInstanceName", notes = "VERIFY_ALERT_INSTANCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertInstanceName", value = "ALERT_INSTANCE_NAME", required = true, dataTypeClass = String.class),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyGroupName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "alertInstanceName") String alertInstanceName) {

        boolean exist = alertPluginInstanceService.checkExistPluginInstanceName(alertInstanceName);
        if (exist) {
            logger.error("alert plugin instance {} has exist, can't create again.", alertInstanceName);
            return Result.error(Status.PLUGIN_INSTANCE_ALREADY_EXISTS);
        } else {
            return Result.success();
        }
    }

    /**
     * 分页查询告警插件实例列表。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索值
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 告警插件实例分页列表
     */
    @ApiOperation(value = "queryAlertPluginInstanceListPaging", notes = "QUERY_ALERT_PLUGIN_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "searchVal", required = false) String searchVal,
                             @RequestParam("pageNo") Integer pageNo,
                             @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return alertPluginInstanceService.listPaging(loginUser, searchVal, pageNo, pageSize);
    }

}
