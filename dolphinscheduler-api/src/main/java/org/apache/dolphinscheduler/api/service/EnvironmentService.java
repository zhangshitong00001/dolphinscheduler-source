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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * 环境服务接口。提供环境配置的完整生命周期管理，包括环境的创建、查询、更新、删除和名称校验。
 * 环境用于管理不同的执行环境配置，可关联特定的Worker分组。
 */
public interface EnvironmentService {

    /**
     * 创建环境配置。
     *
     * @param loginUser    登录用户
     * @param name         环境名称
     * @param config       环境配置
     * @param desc         环境描述
     * @param workerGroups Worker分组
     * @return 创建结果
     */
    Map<String, Object> createEnvironment(User loginUser, String name, String config, String desc, String workerGroups);

    /**
     * 根据环境名称查询环境信息。
     *
     * @param name 环境名称
     * @return 环境信息
     */
    Map<String, Object> queryEnvironmentByName(String name);

    /**
     * 根据环境编码查询环境信息。
     *
     * @param code 环境编码
     * @return 环境信息
     */
    Map<String, Object> queryEnvironmentByCode(Long code);


    /**
     * 根据环境编码删除环境。
     *
     * @param loginUser 登录用户
     * @param code      环境编码
     * @return 删除结果
     */
    Map<String, Object> deleteEnvironmentByCode(User loginUser, Long code);

    /**
     * 更新环境配置。
     *
     * @param loginUser    登录用户
     * @param code         环境编码
     * @param name         新环境名称
     * @param config       新环境配置
     * @param desc         新环境描述
     * @param workerGroups Worker分组
     * @return 更新结果
     */
    Map<String, Object> updateEnvironmentByCode(User loginUser, Long code, String name, String config, String desc, String workerGroups);

    /**
     * 分页查询环境列表。
     *
     * @param loginUser 登录用户
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @param searchVal 搜索关键字
     * @return 分页查询结果
     */
    Result queryEnvironmentListPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal);

    /**
     * 查询所有环境列表。
     *
     * @param loginUser 登录用户
     * @return 所有环境列表
     */
    Map<String, Object> queryAllEnvironmentList(User loginUser);

    /**
     * 校验环境名称是否可用。
     *
     * @param environmentName 环境名称
     * @return 校验结果
     */
    Map<String, Object> verifyEnvironment(String environmentName);

}

