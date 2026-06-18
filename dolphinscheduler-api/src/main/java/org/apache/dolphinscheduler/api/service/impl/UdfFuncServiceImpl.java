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

package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * UDF函数服务实现类。负责UDF函数的增删改查和权限管理，关联资源文件并校验资源上传状态。
 */
@Service
public class UdfFuncServiceImpl extends BaseServiceImpl implements UdfFuncService {

    private static final Logger logger = LoggerFactory.getLogger(UdfFuncServiceImpl.class);

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    /**
     * 创建UDF函数。校验权限、资源上传状态和函数名唯一性，关联资源文件后持久化。
     *
     * @param loginUser 当前登录用户
     * @param funcName 函数名称
     * @param className 类名
     * @param argTypes 参数类型
     * @param database 数据库名
     * @param desc 描述信息
     * @param type UDF类型
     * @param resourceId 关联资源ID
     * @return 包含创建结果的结果对象
     */
    @Override
    @Transactional
    public Result<Object> createUdfFunction(User loginUser,
                                            String funcName,
                                            String className,
                                            String argTypes,
                                            String database,
                                            String desc,
                                            UdfType type,
                                            int resourceId) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_CREATE);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        if(checkDescriptionLength(desc)){
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()) {
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udf func name exist
        if (checkUdfFuncNameExists(funcName)) {
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
            return result;
        }

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("resourceId {} is not exist", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        //save data
        UdfFunc udf = new UdfFunc();
        Date now = new Date();
        udf.setUserId(loginUser.getId());
        udf.setFuncName(funcName);
        udf.setClassName(className);
        if (!StringUtils.isEmpty(argTypes)) {
            udf.setArgTypes(argTypes);
        }
        if (!StringUtils.isEmpty(database)) {
            udf.setDatabase(database);
        }
        udf.setDescription(desc);
        udf.setResourceId(resourceId);
        udf.setResourceName(resource.getFullName());
        udf.setType(type);

        udf.setCreateTime(now);
        udf.setUpdateTime(now);

        udfFuncMapper.insert(udf);
        putMsg(result, Status.SUCCESS);
        permissionPostHandle(AuthorizationType.UDF, loginUser.getId(), Collections.singletonList(udf.getId()), logger);
        return result;
    }

    /**
     *
     * @param name name
     * @return check result code
     */
    /**
     * 检查UDF函数名称是否已存在。
     *
     * @param name 函数名称
     * @return true表示已存在，false表示不存在
     */
    private boolean checkUdfFuncNameExists(String name) {
        List<UdfFunc> resource = udfFuncMapper.queryUdfByIdStr(null, name);
        return resource != null && !resource.isEmpty();
    }

    /**
     * 根据ID查询UDF函数详情，需要UDF查看权限。
     *
     * @param loginUser 当前登录用户
     * @param id UDF函数ID
     * @return 包含UDF函数详情的结果对象
     */
    @Override
    public Result<Object> queryUdfFuncDetail(User loginUser, int id) {
        Result<Object> result = new Result<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        UdfFunc udfFunc = udfFuncMapper.selectById(id);
        if (udfFunc == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        result.setData(udfFunc);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 更新UDF函数信息。校验权限和名称唯一性，更新关联资源文件。
     *
     * @param loginUser 当前登录用户
     * @param udfFuncId UDF函数ID
     * @param funcName 新的函数名称
     * @param className 新的类名
     * @param argTypes 新的参数类型
     * @param database 新的数据库名
     * @param desc 新的描述信息
     * @param type 新的UDF类型
     * @param resourceId 新的资源ID
     * @return 包含更新结果的结果对象
     */
    @Override
    public Result<Object> updateUdfFunc(User loginUser,
                                        int udfFuncId,
                                        String funcName,
                                        String className,
                                        String argTypes,
                                        String database,
                                        String desc,
                                        UdfType type,
                                        int resourceId) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, new Object[]{udfFuncId}, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        if(checkDescriptionLength(desc)){
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        // verify udfFunc is exist
        UdfFunc udf = udfFuncMapper.selectUdfById(udfFuncId);

        if (udf == null) {
            result.setCode(Status.UDF_FUNCTION_NOT_EXIST.getCode());
            result.setMsg(Status.UDF_FUNCTION_NOT_EXIST.getMsg());
            return result;
        }

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()) {
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udfFuncName is exist
        if (!funcName.equals(udf.getFuncName())) {
            if (checkUdfFuncNameExists(funcName)) {
                logger.error("UdfFuncRequest {} has exist, can't create again.", funcName);
                result.setCode(Status.UDF_FUNCTION_EXISTS.getCode());
                result.setMsg(Status.UDF_FUNCTION_EXISTS.getMsg());
                return result;
            }
        }

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("resourceId {} is not exist", resourceId);
            result.setCode(Status.RESOURCE_NOT_EXIST.getCode());
            result.setMsg(Status.RESOURCE_NOT_EXIST.getMsg());
            return result;
        }
        Date now = new Date();
        udf.setFuncName(funcName);
        udf.setClassName(className);
        udf.setArgTypes(argTypes);
        if (!StringUtils.isEmpty(database)) {
            udf.setDatabase(database);
        }
        udf.setDescription(desc);
        udf.setResourceId(resourceId);
        udf.setResourceName(resource.getFullName());
        udf.setType(type);

        udf.setUpdateTime(now);

        udfFuncMapper.updateById(udf);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 分页查询UDF函数列表，根据用户权限过滤。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索关键字
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页UDF函数列表的结果对象
     */
    @Override
    public Result<Object> queryUdfFuncListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result<Object> result = new Result();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        PageInfo<UdfFunc> pageInfo = new PageInfo<>(pageNo, pageSize);
        IPage<UdfFunc> udfFuncList = getUdfFuncsPage(loginUser, searchVal, pageSize, pageNo);
        pageInfo.setTotal((int)udfFuncList.getTotal());
        pageInfo.setTotalList(udfFuncList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 根据用户权限获取分页的UDF函数列表。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索关键字
     * @param pageSize 每页大小
     * @param pageNo 页码
     * @return UDF函数分页结果
     */
    private IPage<UdfFunc> getUdfFuncsPage(User loginUser, String searchVal, Integer pageSize, int pageNo) {
        Set<Integer> udfFuncIds = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF, loginUser.getId(), logger);
        Page<UdfFunc> page = new Page<>(pageNo, pageSize);
        if (udfFuncIds.isEmpty()) {
            return page;
        }
        return udfFuncMapper.queryUdfFuncPaging(page, new ArrayList<>(udfFuncIds), searchVal);
    }

    /**
     * 按类型查询用户有权限的UDF函数列表。
     *
     * @param loginUser 当前登录用户
     * @param type UDF类型（可为null表示全部）
     * @return 包含UDF函数列表的结果对象
     */
    @Override
    public Result<Object> queryUdfFuncList(User loginUser, Integer type) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        Set<Integer> udfFuncIds = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF, loginUser.getId(), logger);
        if (udfFuncIds.isEmpty()){
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<UdfFunc> udfFuncList = udfFuncMapper.getUdfFuncByType(new ArrayList<>(udfFuncIds), type);

        result.setData(udfFuncList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 删除UDF函数，同步删除UDF用户关联记录。
     *
     * @param loginUser 当前登录用户
     * @param id UDF函数ID
     * @return 包含删除结果的结果对象
     */
    @Override
    @Transactional
    public Result<Object> delete(User loginUser, int id) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_DELETE);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        udfFuncMapper.deleteById(id);
        udfUserMapper.deleteByUdfFuncId(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 验证UDF函数名称是否可用（未重复）。
     *
     * @param loginUser 当前登录用户
     * @param name 函数名称
     * @return 包含验证结果的结果对象
     */
    @Override
    public Result<Object> verifyUdfFuncByName(User loginUser, String name) {
        Result<Object> result = new Result<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions){
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        if (checkUdfFuncNameExists(name)) {
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }
}
