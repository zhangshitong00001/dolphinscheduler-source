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

import static org.apache.dolphinscheduler.api.enums.Status.GET_DATASOURCE_OPTIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_RULE_FORM_CREATE_JSON_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_EXECUTE_RESULT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RULE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RULE_LIST_PAGING_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DqExecuteResultService;
import org.apache.dolphinscheduler.api.service.DqRuleService;
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
 * 数据质量控制器。提供数据质量规则管理REST API，包括规则表单JSON获取、规则分页查询、规则列表查询和执行结果分页查询等操作。
 */
@Api(tags = "DATA_QUALITY_SERVICE")
@RestController
@RequestMapping("/data-quality")
public class DataQualityController extends BaseController {

    @Autowired
    private DqRuleService dqRuleService;

    @Autowired
    private DqExecuteResultService dqExecuteResultService;

    /**
     * 获取规则创建表单的JSON数据。
     *
     * @param ruleId 规则ID
     * @return 表单创建JSON
     */
    @ApiOperation(value = "getRuleFormCreateJson", notes = "GET_RULE_FORM_CREATE_JSON_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ruleId", value = "RULE_ID", dataTypeClass = int.class, example = "1")
    })
    @GetMapping(value = "/getRuleFormCreateJson")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_RULE_FORM_CREATE_JSON_ERROR)
    public Result getRuleFormCreateJsonById(@RequestParam(value = "ruleId") int ruleId) {
        Map<String, Object> result = dqRuleService.getRuleFormCreateJsonById(ruleId);
        return returnDataList(result);
    }

    /**
     * 分页查询规则列表。支持按搜索值、规则类型和时间范围进行筛选。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索值
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 规则分页列表
     */
    @ApiOperation(value = "queryRuleListPaging", notes = "QUERY_RULE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "ruleType", value = "RULE_TYPE", dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataTypeClass = int.class, example = "10")
    })
    @GetMapping(value = "/rule/page")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RULE_LIST_PAGING_ERROR)
    public Result queryRuleListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                      @RequestParam(value = "ruleType", required = false) Integer ruleType,
                                      @RequestParam(value = "startDate", required = false) String startTime,
                                      @RequestParam(value = "endDate", required = false) String endTime,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        return dqRuleService.queryRuleListPaging(loginUser, searchVal, ruleType, startTime, endTime, pageNo, pageSize);
    }

    /**
     * 查询所有规则列表。
     *
     * @return 规则列表
     */
    @ApiOperation(value = "queryRuleList", notes = "QUERY_RULE_LIST_NOTES")
    @GetMapping(value = "/ruleList")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RULE_LIST_ERROR)
    public Result queryRuleList() {
        Map<String, Object> result = dqRuleService.queryAllRuleList();
        return returnDataList(result);
    }

    /**
     * 分页查询任务执行结果列表。支持按规则类型、状态和时间范围进行筛选。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索值
     * @param ruleType 规则类型
     * @param state 执行状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 执行结果分页列表
     */
    @ApiOperation(value = "queryExecuteResultListPaging", notes = "QUERY_EXECUTE_RESULT_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "ruleType", value = "RULE_TYPE", dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "state", value = "STATE", dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataTypeClass = int.class, example = "10")
    })
    @GetMapping(value = "/result/page")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXECUTE_RESULT_LIST_PAGING_ERROR)
    public Result queryExecuteResultListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "searchVal", required = false) String searchVal,
                                               @RequestParam(value = "ruleType", required = false) Integer ruleType,
                                               @RequestParam(value = "state", required = false) Integer state,
                                               @RequestParam(value = "startDate", required = false) String startTime,
                                               @RequestParam(value = "endDate", required = false) String endTime,
                                               @RequestParam("pageNo") Integer pageNo,
                                               @RequestParam("pageSize") Integer pageSize) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        return dqExecuteResultService.queryResultListPaging(loginUser, searchVal, state, ruleType, startTime, endTime,
                pageNo, pageSize);
    }

    /**
     * 根据数据源ID获取数据源选项。
     *
     * @param datasourceId 数据源ID
     * @return 数据源选项
     */
    @ApiOperation(value = "getDatasourceOptionsById", notes = "GET_DATASOURCE_OPTIONS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "datasourceId", value = "DATA_SOURCE_ID", dataTypeClass = int.class, example = "1")
    })
    @GetMapping(value = "/getDatasourceOptionsById")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_DATASOURCE_OPTIONS_ERROR)
    public Result getDatasourceOptionsById(@RequestParam(value = "datasourceId") int datasourceId) {
        Map<String, Object> result = dqRuleService.getDatasourceOptionsById(datasourceId);
        return returnDataList(result);
    }
}
