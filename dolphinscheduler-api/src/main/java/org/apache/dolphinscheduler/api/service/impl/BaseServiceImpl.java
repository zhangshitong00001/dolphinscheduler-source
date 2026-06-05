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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础服务实现类。提供权限校验、消息封装、日期参数解析等通用功能，作为其他服务实现类的基类。
 */
public class BaseServiceImpl implements BaseService {
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @Autowired
    protected ResourcePermissionCheckService resourcePermissionCheckService;

    /**
     * 权限后置处理，在资源操作完成后关联用户与资源的权限关系。
     *
     * @param authorizationType 授权类型
     * @param userId 用户ID
     * @param ids 资源ID列表
     * @param logger 日志记录器
     */
    @Override
    public void permissionPostHandle(AuthorizationType authorizationType, Integer userId, List<Integer> ids, Logger logger) {
        try{
            resourcePermissionCheckService.postHandle(authorizationType, userId, ids, logger);
        }catch (Exception e){
            logger.error("post handle error", e);
            throw new RuntimeException("resource association user error", e);
        }
    }

    /**
     * 检查用户是否为管理员。
     *
     * @param user 待检查的用户
     * @return true表示管理员，false表示非管理员
     */
    @Override
    public boolean isAdmin(User user) {
        return user.getUserType() == UserType.ADMIN_USER;
    }

    /**
     * 检查用户是否为非管理员，如果非管理员则在结果中设置无权限错误信息。
     *
     * @param loginUser 登录用户
     * @param result 结果Map，非管理员时写入错误状态
     * @return true表示非管理员，false表示管理员
     */
    @Override
    public boolean isNotAdmin(User loginUser, Map<String, Object> result) {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return true;
        }
        return false;
    }

    /**
     * 将状态消息放入Map结果中。
     *
     * @param result 结果Map
     * @param status 状态枚举
     * @param statusParams 状态消息格式化参数
     */
    @Override
    public void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    /**
     * 将状态消息放入Result结果对象中。
     *
     * @param result 结果对象
     * @param status 状态枚举
     * @param statusParams 状态消息格式化参数
     */
    @Override
    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

    /**
     * 检查条件并设置无权限结果。如果bool为true则将无操作权限状态放入结果中。
     *
     * @param result 结果Map
     * @param bool 检查条件
     * @param userNoOperationPerm 无操作权限的状态码
     * @return true表示检查不通过，false表示检查通过
     */
    @Override
    public boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm) {
        // only admin can operate
        if (bool) {
            result.put(Constants.STATUS, userNoOperationPerm);
            result.put(Constants.MSG, userNoOperationPerm.getMsg());
            return true;
        }
        return false;
    }

    /**
     * 创建租户目录（如果不存在）。已废弃的方法，保留以供参考。
     *
     * @param tenantCode 租户编码
     * @throws IOException HDFS操作异常时抛出
     */
//    @Override
//    public void createTenantDirIfNotExists(String tenantCode) throws IOException {
//        String resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
//        String udfsPath = HadoopUtils.getHdfsUdfDir(tenantCode);
//        // init resource path and udf path
//        HadoopUtils.getInstance().mkdir(tenantCode,resourcePath);
//        HadoopUtils.getInstance().mkdir(tenantCode,udfsPath);
//    }

    /**
     * 验证操作用户是否有权限操作目标资源。操作用户为管理员或资源创建者时具有操作权限。
     *
     * @param operateUser 操作用户
     * @param createUserId 资源创建者用户ID
     * @return true表示有权限操作，false表示无权限
     */
    @Override
    public boolean canOperator(User operateUser, int createUserId) {
        return operateUser.getId() == createUserId || isAdmin(operateUser);
    }

    /**
     * 验证用户对指定资源是否有操作权限，同时检查操作权限和资源权限。
     *
     * @param user 操作用户
     * @param ids 资源ID数组
     * @param type 授权类型
     * @param permissionKey 权限键
     * @return true表示有权限，false表示无权限
     */
    @Override
    public boolean canOperatorPermissions(User user, Object[] ids,AuthorizationType type,String permissionKey) {
        boolean operationPermissionCheck = resourcePermissionCheckService.operationPermissionCheck(type, type.equals(AuthorizationType.PROJECTS) ? ids : null, user.getId(), permissionKey, logger);
        boolean resourcePermissionCheck = resourcePermissionCheckService.resourcePermissionCheck(type, ids, user.getUserType().equals(UserType.ADMIN_USER) ? 0 : user.getId(), logger);
        return operationPermissionCheck && resourcePermissionCheck;
    }

    /**
     * 检查并解析日期参数，将起止日期字符串转换为Date对象并放入结果Map中。
     *
     * @param startDateStr 开始日期字符串
     * @param endDateStr 结束日期字符串
     * @return 包含STATUS、START_TIME、END_TIME的结果Map
     */
    @Override
    public Map<String, Object> checkAndParseDateParameters(String startDateStr, String endDateStr) {
        Map<String, Object> result = new HashMap<>();
        Date start = null;
        if (!StringUtils.isEmpty(startDateStr)) {
            start = DateUtils.stringToDate(startDateStr);
            if (Objects.isNull(start)) {
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return result;
            }
        }
        result.put(Constants.START_TIME, start);

        Date end = null;
        if (!StringUtils.isEmpty(endDateStr)) {
            end = DateUtils.stringToDate(endDateStr);
            if (Objects.isNull(end)) {
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return result;
            }
        }
        result.put(Constants.END_TIME, end);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 检查描述文本长度是否超过255个字符（按Unicode码点计算）。
     *
     * @param description 描述文本
     * @return true表示超过长度限制，false表示未超过
     */
    @Override
    public boolean checkDescriptionLength(String description) {
        return description!=null && description.codePointCount(0, description.length()) > 255;
    }
}
