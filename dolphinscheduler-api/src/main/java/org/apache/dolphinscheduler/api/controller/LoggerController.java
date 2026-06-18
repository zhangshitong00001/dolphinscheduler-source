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

import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_INSTANCE_LOG_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 日志控制器，提供任务日志的查询与下载 REST API。
 */
@Api(tags = "LOGGER_TAG")
@RestController
@RequestMapping("/log")
public class LoggerController extends BaseController {

    @Autowired
    private LoggerService loggerService;

    /**
     * 查询任务实例日志内容。
     *
     * @param loginUser 登录用户
     * @param taskInstanceId 任务实例 ID
     * @param skipNum 跳过的行数
     * @param limit 返回行数上限
     * @return 任务日志内容
     */
    @ApiOperation(value = "queryLog", notes = "QUERY_TASK_INSTANCE_LOG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_INSTANCE_LOG_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<ResponseTaskLog> queryLog(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "taskInstanceId") int taskInstanceId,
                                            @RequestParam(value = "skipLineNum") int skipNum,
                                            @RequestParam(value = "limit") int limit) {
        return loggerService.queryLog(taskInstanceId, skipNum, limit);
    }

    /**
     * 下载任务实例日志文件。
     *
     * @param loginUser 登录用户
     * @param taskInstanceId 任务实例 ID
     * @return 日志文件内容
     */
    @ApiOperation(value = "downloadTaskLog", notes = "DOWNLOAD_TASK_INSTANCE_LOG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadTaskLog(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "taskInstanceId") int taskInstanceId) {
        byte[] logBytes = loggerService.getLogBytes(taskInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                .body(logBytes);
    }

    /**
     * 在指定项目中查询任务实例日志内容。
     *
     * @param loginUser      登录用户
     * @param projectCode 项目编码
     * @param taskInstanceId 任务实例 ID
     * @param skipNum        跳过的行数
     * @param limit          返回行数上限
     * @return 任务日志内容
     */
    @ApiOperation(value = "queryLogInSpecifiedProject", notes = "QUERY_TASK_INSTANCE_LOG_IN_SPECIFIED_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{projectCode}/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_INSTANCE_LOG_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<String> queryLog(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                   @RequestParam(value = "taskInstanceId") int taskInstanceId,
                                   @RequestParam(value = "skipLineNum") int skipNum,
                                   @RequestParam(value = "limit") int limit) {
        return returnDataList(loggerService.queryLog(loginUser, projectCode, taskInstanceId, skipNum, limit));
    }

    /**
     * 在指定项目中下载任务实例日志文件。
     *
     * @param loginUser      登录用户
     * @param projectCode    项目编码
     * @param taskInstanceId 任务实例 ID
     * @return 日志文件内容
     */
    @ApiOperation(value = "downloadTaskLogInSpecifiedProject", notes = "DOWNLOAD_TASK_INSTANCE_LOG_IN_SPECIFIED_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{projectCode}/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadTaskLog(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "taskInstanceId") int taskInstanceId) {
        byte[] logBytes = loggerService.getLogBytes(loginUser, projectCode, taskInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                .body(logBytes);
    }
}
