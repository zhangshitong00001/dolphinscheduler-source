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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUDIT_LOG_LIST_PAGING;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.AuditResourceType;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

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
 * 审计日志控制器。提供审计日志的分页查询REST API，支持按资源类型、操作类型、时间范围和用户名等条件筛选。
 */
@Api(tags = "AUDIT_LOG_TAG")
@RestController
@RequestMapping("projects/audit")
public class AuditLogController extends BaseController {

    @Autowired
    AuditService auditService;

    /**
     * 分页查询审计日志列表。支持按资源类型、操作类型、时间范围和用户名等条件进行筛选。
     *
     * @param loginUser 当前登录用户
     * @param pageNo 页码
     * @param resourceType 资源类型
     * @param operationType 操作类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param userName 用户名
     * @param pageSize 每页大小
     * @return 审计日志分页内容
     */
    @ApiOperation(value = "queryAuditLogListPaging", notes = "QUERY_AUDIT_LOG")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataTypeClass = String.class),
            @ApiImplicitParam(name = "resourceType", value = "RESOURCE_TYPE", dataTypeClass = AuditResourceType.class),
            @ApiImplicitParam(name = "operationType", value = "OPERATION_TYPE", dataTypeClass = AuditOperationType.class),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping(value = "/audit-log-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUDIT_LOG_LIST_PAGING)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuditLogListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam("pageNo") Integer pageNo,
                                          @RequestParam("pageSize") Integer pageSize,
                                          @RequestParam(value = "resourceType", required = false) AuditResourceType resourceType,
                                          @RequestParam(value = "operationType", required = false) AuditOperationType operationType,
                                          @RequestParam(value = "startDate", required = false) String startDate,
                                          @RequestParam(value = "endDate", required = false) String endDate,
                                          @RequestParam(value = "userName", required = false) String userName) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        result = auditService.queryLogListPaging(loginUser, resourceType, operationType, startDate, endDate, userName,
                pageNo, pageSize);
        return result;
    }
}
