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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_FILE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZE_RESOURCE_TREE;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_RESOURCE_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATASOURCE_BY_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_PAGING;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_UDF_FUNCTION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_FILE_IS_EMPTY;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_NOT_EXIST;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_UDF_FUNCTION_NAME_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_UDF_FUNCTION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import springfox.documentation.annotations.ApiIgnore;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/**
 * 资源控制器，提供文件资源的创建、更新、删除、查询、下载、在线编辑以及 UDF 函数管理 REST API。
 */
@Api(tags = "RESOURCES_TAG")
@RestController
@RequestMapping("resources")
public class ResourcesController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesController.class);

    @Autowired
    private ResourcesService resourceService;
    @Autowired
    private UdfFuncService udfFuncService;

    /**
     * 创建资源目录。
     *
     * @param loginUser 登录用户
     * @param type 资源类型
     * @param alias 别名
     * @param description 描述
     * @param pid 父目录 ID
     * @param currentDir 当前目录
     * @return 创建结果
     */
    @ApiOperation(value = "createDirectory", notes = "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENT_DIR", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/directory")
    @ApiException(CREATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> createDirectory(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "type") ResourceType type,
                                          @RequestParam(value = "name") String alias,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "pid") int pid,
                                          @RequestParam(value = "currentDir") String currentDir) {
        // todo verify the directory name
        return resourceService.createDirectory(loginUser, alias, description, type, pid, currentDir);
    }

    /**
     * 创建文件资源。
     *
     * @param loginUser 登录用户
     * @param type 资源类型
     * @param alias 别名
     * @param description 描述
     * @param file 上传文件
     * @param pid 父目录 ID
     * @param currentDir 当前目录
     * @return 创建结果
     */
    @ApiOperation(value = "createResource", notes = "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENT_DIR", required = true, dataTypeClass = String.class)
    })
    @PostMapping()
    @ApiException(CREATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> createResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "type") ResourceType type,
                                         @RequestParam(value = "name") String alias,
                                         @RequestParam(value = "description", required = false) String description,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "pid") int pid,
                                         @RequestParam(value = "currentDir") String currentDir) {
        // todo verify the file name
        return resourceService.createResource(loginUser, alias, description, type, file, pid, currentDir);
    }

    /**
     * 更新文件资源。
     *
     * @param loginUser 登录用户
     * @param resourceId 资源 ID
     * @param type 资源类型
     * @param alias 别名
     * @param description 描述
     * @param file 更新文件
     * @return 更新结果
     */
    @ApiOperation(value = "updateResource", notes = "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataTypeClass = MultipartFile.class)
    })
    @PutMapping(value = "/{id}")
    @ApiException(UPDATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> updateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable(value = "id") int resourceId,
                                         @RequestParam(value = "type") ResourceType type,
                                         @RequestParam(value = "name") String alias,
                                         @RequestParam(value = "description", required = false) String description,
                                         @RequestParam(value = "file", required = false) MultipartFile file) {
        // todo verify the resource name
        return resourceService.updateResource(loginUser, resourceId, alias, description, type, file);
    }

    /**
     * 查询资源列表。
     *
     * @param loginUser 登录用户
     * @param type 资源类型
     * @return 资源列表
     */
    @ApiOperation(value = "queryResourceList", notes = "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class)
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "type") ResourceType type) {
        Map<String, Object> result = resourceService.queryResourceList(loginUser, type);
        return returnDataList(result);
    }

    /**
     * 查询资源分页列表。
     *
     * @param loginUser 登录用户
     * @param type 资源类型
     * @param id 父资源 ID
     * @param pageNo 页码
     * @param searchVal 搜索关键词
     * @param pageSize 每页大小
     * @return 资源分页列表
     */
    @ApiOperation(value = "queryResourceListPaging", notes = "QUERY_RESOURCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_PAGING)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResourceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestParam(value = "type") ResourceType type,
                                                  @RequestParam(value = "id") int id,
                                                  @RequestParam("pageNo") Integer pageNo,
                                                  @RequestParam(value = "searchVal", required = false) String searchVal,
                                                  @RequestParam("pageSize") Integer pageSize) {
        Result<Object> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = resourceService.queryResourceListPaging(loginUser, id, type, searchVal, pageNo, pageSize);
        return result;
    }

    /**
     * 删除资源。
     *
     * @param loginUser 登录用户
     * @param resourceId 资源 ID
     * @return 删除结果
     */
    @ApiOperation(value = "deleteResource", notes = "DELETE_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> deleteResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable(value = "id") int resourceId) throws Exception {
        return resourceService.delete(loginUser, resourceId);
    }

    /**
     * 验证资源名称是否可用。
     *
     * @param loginUser 登录用户
     * @param fullName 资源全名
     * @param type 资源类型
     * @return 资源名称不存在则返回 true，否则返回 false
     */
    @ApiOperation(value = "verifyResourceName", notes = "VERIFY_RESOURCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataTypeClass = String.class)
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> verifyResourceName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam(value = "fullName") String fullName,
                                             @RequestParam(value = "type") ResourceType type) {
        return resourceService.verifyResourceName(fullName, type, loginUser);
    }

    /**
     * 根据程序类型查询资源列表。
     *
     * @param loginUser 登录用户
     * @param type 资源类型
     * @param programType 程序类型
     * @return 资源列表
     */
    @ApiOperation(value = "queryResourceByProgramType", notes = "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class)
    })
    @GetMapping(value = "/query-by-type")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResourceJarList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "type") ResourceType type,
                                               @RequestParam(value = "programType", required = false) ProgramType programType) {
        return resourceService.queryResourceByProgramType(loginUser, type, programType);
    }

    /**
     * 根据资源全名和类型查询资源。
     *
     * @param loginUser 登录用户
     * @param fullName 资源全名
     * @param id 资源 ID
     * @param type 资源类型
     * @return 资源详情
     */
    @ApiOperation(value = "queryResource", notes = "QUERY_BY_RESOURCE_NAME")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = false, dataTypeClass = int.class, example = "10")
    })
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "fullName", required = false) String fullName,
                                        @PathVariable(value = "id", required = false) Integer id,
                                        @RequestParam(value = "type") ResourceType type) {

        return resourceService.queryResource(loginUser, fullName, id, type);
    }

    /**
     * 在线查看资源文件内容。
     *
     * @param loginUser 登录用户
     * @param resourceId 资源 ID
     * @param skipLineNum 跳过的行数
     * @param limit 返回行数上限
     * @return 资源文件内容
     */
    @ApiOperation(value = "viewResource", notes = "VIEW_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{id}/view")
    @ApiException(VIEW_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @PathVariable(value = "id") int resourceId,
                               @RequestParam(value = "skipLineNum") int skipLineNum,
                               @RequestParam(value = "limit") int limit) {
        return resourceService.readResource(loginUser, resourceId, skipLineNum, limit);
    }

    /**
     * 在线创建资源文件。
     *
     * @param loginUser 登录用户
     * @param type 资源类型
     * @param fileName 文件名
     * @param fileSuffix 文件后缀
     * @param description 描述
     * @param content 文件内容
     * @param pid 父目录 ID
     * @param currentDir 当前目录
     * @return 创建结果
     */
    @ApiOperation(value = "onlineCreateResource", notes = "ONLINE_CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataTypeClass = ResourceType.class),
            @ApiImplicitParam(name = "fileName", value = "RESOURCE_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "suffix", value = "SUFFIX", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "content", value = "CONTENT", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataTypeClass = int.class, example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENTDIR", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/online-create")
    @ApiException(CREATE_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result onlineCreateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "type") ResourceType type,
                                       @RequestParam(value = "fileName") String fileName,
                                       @RequestParam(value = "suffix") String fileSuffix,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam(value = "content") String content,
                                       @RequestParam(value = "pid") int pid,
                                       @RequestParam(value = "currentDir") String currentDir) {
        if (StringUtils.isEmpty(content)) {
            logger.error("resource file contents are not allowed to be empty");
            return error(RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.onlineCreateResource(loginUser, type, fileName, fileSuffix, description, content, pid,
                currentDir);
    }

    /**
     * 在线编辑资源文件内容。
     *
     * @param loginUser 登录用户
     * @param resourceId 资源 ID
     * @param content 内容
     * @return 更新结果
     */
    @ApiOperation(value = "updateResourceContent", notes = "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100"),
            @ApiImplicitParam(name = "content", value = "CONTENT", required = true, dataTypeClass = String.class)
    })
    @PutMapping(value = "/{id}/update-content")
    @ApiException(EDIT_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateResourceContent(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @PathVariable(value = "id") int resourceId,
                                        @RequestParam(value = "content") String content) {
        if (StringUtils.isEmpty(content)) {
            logger.error("The resource file contents are not allowed to be empty");
            return error(RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.updateResourceContent(loginUser, resourceId, content);
    }

    /**
     * 下载资源文件。
     *
     * @param loginUser 登录用户
     * @param resourceId 资源 ID
     * @return 资源文件
     */
    @ApiOperation(value = "downloadResource", notes = "DOWNLOAD_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/{id}/download")
    @ResponseBody
    @ApiException(DOWNLOAD_RESOURCE_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable(value = "id") int resourceId) throws Exception {
        Resource file = resourceService.downloadResource(loginUser, resourceId);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RESOURCE_NOT_EXIST.getMsg());
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * 创建 UDF 函数。
     *
     * @param loginUser 登录用户
     * @param type UDF 类型
     * @param funcName 函数名
     * @param className 类名
     * @param argTypes 参数类型
     * @param database 数据库
     * @param description 描述
     * @param resourceId 资源 ID
     * @return 创建结果
     */
    @ApiOperation(value = "createUdfFunc", notes = "CREATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataTypeClass = UdfType.class),
            @ApiImplicitParam(name = "funcName", value = "FUNC_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "className", value = "CLASS_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "argTypes", value = "ARG_TYPES", dataTypeClass = String.class),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "UDF_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100")

    })
    @PostMapping(value = "/{resourceId}/udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value = "funcName") String funcName,
                                @RequestParam(value = "className") String className,
                                @RequestParam(value = "argTypes", required = false) String argTypes,
                                @RequestParam(value = "database", required = false) String database,
                                @RequestParam(value = "description", required = false) String description,
                                @PathVariable(value = "resourceId") int resourceId) {
        // todo verify the sourceName
        return udfFuncService.createUdfFunction(loginUser, funcName, className, argTypes, database, description, type,
                resourceId);
    }

    /**
     * 查看 UDF 函数详情。
     *
     * @param loginUser 登录用户
     * @param id 资源 ID
     * @return UDF 函数详情
     */
    @ApiOperation(value = "viewUIUdfFunction", notes = "VIEW_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100")

    })
    @GetMapping(value = "/{id}/udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VIEW_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewUIUdfFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @PathVariable("id") int id) {
        return udfFuncService.queryUdfFuncDetail(loginUser, id);
    }

    /**
     * 更新 UDF 函数。
     *
     * @param loginUser 登录用户
     * @param udfFuncId UDF 函数 ID
     * @param type UDF 类型
     * @param funcName 函数名
     * @param className 类名
     * @param argTypes 参数类型
     * @param database 数据库
     * @param description 描述
     * @param resourceId 资源 ID
     * @return 更新结果
     */
    @ApiOperation(value = "updateUdfFunc", notes = "UPDATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "UDF_ID", required = true, dataTypeClass = int.class),
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataTypeClass = UdfType.class),
            @ApiImplicitParam(name = "funcName", value = "FUNC_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "className", value = "CLASS_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "argTypes", value = "ARG_TYPES", dataTypeClass = String.class),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "UDF_DESC", dataTypeClass = String.class),
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "100")

    })
    @PutMapping(value = "/{resourceId}/udf-func/{id}")
    @ApiException(UPDATE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable(value = "id") int udfFuncId,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value = "funcName") String funcName,
                                @RequestParam(value = "className") String className,
                                @RequestParam(value = "argTypes", required = false) String argTypes,
                                @RequestParam(value = "database", required = false) String database,
                                @RequestParam(value = "description", required = false) String description,
                                @PathVariable(value = "resourceId") int resourceId) {
        return udfFuncService.updateUdfFunc(loginUser, udfFuncId, funcName, className, argTypes, database, description,
                type, resourceId);
    }

    /**
     * 查询 UDF 函数分页列表。
     *
     * @param loginUser 登录用户
     * @param pageNo 页码
     * @param searchVal 搜索关键词
     * @param pageSize 每页大小
     * @return UDF 函数分页列表
     */
    @ApiOperation(value = "queryUdfFuncListPaging", notes = "QUERY_UDF_FUNCTION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20")
    })
    @GetMapping(value = "/udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UDF_FUNCTION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryUdfFuncListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @RequestParam("pageNo") Integer pageNo,
                                                 @RequestParam(value = "searchVal", required = false) String searchVal,
                                                 @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        return udfFuncService.queryUdfFuncListPaging(loginUser, searchVal, pageNo, pageSize);
    }

    /**
     * 根据类型查询 UDF 函数列表。
     *
     * @param loginUser 登录用户
     * @param type UDF 类型
     * @return UDF 函数列表
     */
    @ApiOperation(value = "queryUdfFuncList", notes = "QUERY_UDF_FUNC_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataTypeClass = UdfType.class)
    })
    @GetMapping(value = "/udf-func/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_BY_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryUdfFuncList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("type") UdfType type) {
        return udfFuncService.queryUdfFuncList(loginUser, type.ordinal());
    }

    /**
     * 验证 UDF 函数名称是否可用。
     *
     * @param loginUser 登录用户
     * @param name 函数名
     * @return 名称可用则返回 true，否则返回 false
     */
    @ApiOperation(value = "verifyUdfFuncName", notes = "VERIFY_UDF_FUNCTION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "FUNC_NAME", required = true, dataTypeClass = String.class)

    })
    @GetMapping(value = "/udf-func/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_UDF_FUNCTION_NAME_ERROR)
    @AccessLogAnnotation
    public Result verifyUdfFuncName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "name") String name) {
        return udfFuncService.verifyUdfFuncByName(loginUser, name);
    }

    /**
     * 删除 UDF 函数。
     *
     * @param loginUser 登录用户
     * @param udfFuncId UDF 函数 ID
     * @return 删除结果
     */
    @ApiOperation(value = "deleteUdfFunc", notes = "DELETE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "UDF_FUNC_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @DeleteMapping(value = "/udf-func/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result deleteUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable(value = "id") int udfFuncId) {
        return udfFuncService.delete(loginUser, udfFuncId);
    }

    /**
     * 查询已授权给用户的文件资源列表。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @return 已授权文件资源列表
     */
    @ApiOperation(value = "authorizedFile", notes = "AUTHORIZED_FILE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/authed-file")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_FILE_RESOURCE_ERROR)
    @AccessLogAnnotation
    public Result authorizedFile(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizedFile(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * 查询已授权的资源树。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @return 已授权资源树
     */
    @ApiOperation(value = "authorizeResourceTree", notes = "AUTHORIZE_RESOURCE_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/authed-resource-tree")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZE_RESOURCE_TREE)
    @AccessLogAnnotation
    public Result authorizeResourceTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizeResourceTree(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * 查询未授权的 UDF 函数列表。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @return 未授权的 UDF 函数列表
     */
    @ApiOperation(value = "unauthUDFFunc", notes = "UNAUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/unauth-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UNAUTHORIZED_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result unauthUDFFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("userId") Integer userId) {

        Map<String, Object> result = resourceService.unauthorizedUDFFunction(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * 查询已授权的 UDF 函数列表。
     *
     * @param loginUser 登录用户
     * @param userId 用户 ID
     * @return 已授权的 UDF 函数列表
     */
    @ApiOperation(value = "authUDFFunc", notes = "AUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataTypeClass = int.class, example = "100")
    })
    @GetMapping(value = "/authed-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result authorizedUDFFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizedUDFFunction(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * 根据资源 ID 查询资源详情。
     *
     * @param loginUser 登录用户
     * @param id 资源 ID
     * @return 资源详情
     */
    @ApiOperation(value = "queryResourceById", notes = "QUERY_BY_RESOURCE_NAME")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataTypeClass = int.class, example = "10")
    })
    @GetMapping(value = "/{id}/query")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryResourceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @PathVariable(value = "id", required = true) Integer id) {

        return resourceService.queryResourceById(loginUser, id);
    }
}
