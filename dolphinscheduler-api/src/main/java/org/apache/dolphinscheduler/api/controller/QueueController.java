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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_QUEUE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_QUEUE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import springfox.documentation.annotations.ApiIgnore;

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
 * 队列控制器，提供 YARN 队列的创建、更新、查询和验证 REST API。
 */
@Api(tags = "QUEUE_TAG")
@RestController
@RequestMapping("/queues")
public class QueueController extends BaseController {

    @Autowired
    private QueueService queueService;

    /**
     * 查询队列列表。
     *
     * @param loginUser 登录用户
     * @return 队列列表
     */
    @ApiOperation(value = "queryList", notes = "QUERY_QUEUE_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_QUEUE_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return queueService.queryList(loginUser);
    }

    /**
     * 查询队列分页列表。
     *
     * @param loginUser 登录用户
     * @param pageNo    页码
     * @param searchVal 搜索关键词
     * @param pageSize  每页大小
     * @return 队列分页列表
     */
    @ApiOperation(value = "queryQueueListPaging", notes = "QUERY_QUEUE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_QUEUE_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryQueueListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam(value = "searchVal", required = false) String searchVal,
                                       @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = queueService.queryList(loginUser, searchVal, pageNo, pageSize);
        return result;
    }

    /**
     * 创建队列。
     *
     * @param loginUser 登录用户
     * @param queue     YARN 队列
     * @param queueName 队列名称
     * @return 创建结果
     */
    @ApiOperation(value = "createQueue", notes = "CREATE_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME", required = true, dataTypeClass = String.class)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_QUEUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createQueue(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName) {
        return queueService.createQueue(loginUser, queue, queueName);
    }

    /**
     * 更新队列。
     *
     * @param loginUser 登录用户
     * @param queue     YARN 队列
     * @param id        队列 ID
     * @param queueName 队列名称
     * @return 更新结果
     */
    @ApiOperation(value = "updateQueue", notes = "UPDATE_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "QUEUE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME", required = true, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_QUEUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateQueue(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @PathVariable(value = "id") int id,
                              @RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName) {
        return queueService.updateQueue(loginUser, id, queue, queueName);
    }

    /**
     * 验证队列与队列名称是否可用。
     *
     * @param loginUser 登录用户
     * @param queue     YARN 队列
     * @param queueName 队列名称
     * @return 队列名称不存在则返回 true，否则返回 false
     */
    @ApiOperation(value = "verifyQueue", notes = "VERIFY_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/verify")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_QUEUE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyQueue(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName) {
        return queueService.verifyQueue(queue, queueName);
    }
}
