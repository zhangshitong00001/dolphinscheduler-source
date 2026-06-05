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
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * 资源权限检查服务接口。定义权限校验的统一入口，包括资源权限检查、操作权限检查和用户授权资源获取等功能。
 * 用于在不同授权场景下标准化权限验证流程。
 *
 * @param <T> 资源ID的类型
 */
public interface ResourcePermissionCheckService<T>{
    /**
     * 检查用户是否拥有指定资源的访问权限。
     *
     * @param authorizationType 授权类型
     * @param needChecks        需要进行权限检查的资源数组
     * @param userId            用户ID
     * @param logger            日志记录器
     * @return 有权限返回true，否则返回false
     */
    boolean resourcePermissionCheck(Object authorizationType, Object[] needChecks, Integer userId, Logger logger);

    /**
     * 获取用户拥有的指定类型授权资源ID集合。
     *
     * @param authorizationType 授权类型
     * @param userId            用户ID
     * @param logger            日志记录器
     * @param <T>               资源ID类型
     * @return 用户拥有的资源ID集合
     */
    Set<T> userOwnedResourceIdsAcquisition(Object authorizationType, Integer userId, Logger logger);

    /**
     * 检查用户是否拥有指定权限键的操作权限。
     *
     * @param authorizationType 授权类型
     * @param projectIds        项目ID数组
     * @param userId            用户ID
     * @param permissionKey     权限键
     * @param logger            日志记录器
     * @return 有操作权限返回true，否则返回false
     */
    boolean operationPermissionCheck(Object authorizationType, Object[] projectIds, Integer userId, String permissionKey, Logger logger);

    /**
     * 判断权限检查功能是否已禁用。
     *
     * @return 已禁用返回true，否则返回false
     */
    boolean functionDisabled();

    /**
     * 资源创建后的后置处理，将新建资源与当前用户关联。
     *
     * @param authorizationType 授权类型
     * @param userId            用户ID
     * @param ids               资源ID列表
     * @param logger            日志记录器
     */
    void postHandle(Object authorizationType, Integer userId, List<Integer> ids, Logger logger);
}
