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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import org.slf4j.Logger;

/**
 * 权限检查通用类。根据授权类型和资源列表，校验用户是否拥有指定资源的访问权限。
 * 支持按资源数组或资源信息列表两种方式进行检查。
 *
 * @param <T> 需要进行权限检查的资源类型
 */
public class PermissionCheck<T> {
    private Logger logger;
    /**
     * 授权类型
     */
    private AuthorizationType authorizationType;

    /**
     * 流程服务
     */
    private ProcessService processService;

    /**
     * 需要进行权限检查的资源数组
     */
    private T[] needChecks;

    /**
     * 资源信息列表
     */
    private List<ResourceInfo> resourceList;

    /**
     * 用户ID
     */
    private int userId;

    /**
     * permission check
     *
     * @param authorizationType authorization type
     * @param processService process dao
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService) {
        this.authorizationType = authorizationType;
        this.processService = processService;
    }

    /**
     * permission check
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, T[] needChecks, int userId) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.needChecks = needChecks;
        this.userId = userId;
    }

    /**
     * permission check
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, T[] needChecks, int userId, Logger logger) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.needChecks = needChecks;
        this.userId = userId;
        this.logger = logger;
    }

    /**
     * permission check
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, List<ResourceInfo> resourceList, int userId, Logger logger) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.resourceList = resourceList;
        this.userId = userId;
        this.logger = logger;
    }

    public AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(AuthorizationType authorizationType) {
        this.authorizationType = authorizationType;
    }

    public ProcessService getProcessService() {
        return processService;
    }

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

    public T[] getNeedChecks() {
        return needChecks;
    }

    public void setNeedChecks(T[] needChecks) {
        this.needChecks = needChecks;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    /**
     * 检查当前用户是否拥有所需资源的访问权限。
     *
     * @return 有权限返回true，否则返回false
     */
    public boolean hasPermission() {
        try {
            checkPermission();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 执行权限校验。如果是管理员用户则直接通过；否则校验用户是否对needChecks中所有资源均有授权。
     *
     * @throws ServiceException 用户不存在或存在未授权资源时抛出
     */
    public void checkPermission() throws ServiceException {
        if (this.needChecks.length > 0) {

            // get user type in order to judge whether the user is admin
            User user = processService.getUserById(userId);
            if (user == null) {
                logger.error("user id {} doesn't exist", userId);
                throw new ServiceException(String.format("user %s doesn't exist", userId));
            }
            if (user.getUserType() != UserType.ADMIN_USER) {
                List<T> unauthorizedList = processService.listUnauthorized(userId, needChecks, authorizationType);
                // if exist unauthorized resource
                if (CollectionUtils.isNotEmpty(unauthorizedList)) {
                    logger.error("user {} doesn't have permission of {}: {}", user.getUserName(), authorizationType.getDescp(), unauthorizedList);
                    throw new ServiceException(String.format("user %s doesn't have permission of %s %s", user.getUserName(), authorizationType.getDescp(), unauthorizedList.get(0)));
                }
            }
        }
    }

}
