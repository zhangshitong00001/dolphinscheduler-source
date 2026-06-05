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
 * 集群服务接口。提供K8s集群配置的管理功能，包括集群的创建、查询、更新、删除和名称校验。
 * 集群配置用于管理多个K8s环境，支持按名称和编码查询。
 */
public interface ClusterService {

    /**
     * 创建集群配置。
     *
     * @param loginUser 登录用户
     * @param name      集群名称
     * @param config    集群配置
     * @param desc      集群描述
     * @return 创建结果
     */
    Map<String, Object> createCluster(User loginUser, String name, String config, String desc);

    /**
     * 根据集群名称查询集群信息。
     *
     * @param name 集群名称
     * @return 集群信息
     */
    Map<String, Object> queryClusterByName(String name);

    /**
     * 根据集群编码查询集群信息。
     *
     * @param code 集群编码
     * @return 集群信息
     */
    Map<String, Object> queryClusterByCode(Long code);

    /**
     * 根据集群编码删除集群。
     *
     * @param loginUser 登录用户
     * @param code      集群编码
     * @return 删除结果
     */
    Map<String, Object> deleteClusterByCode(User loginUser, Long code);

    /**
     * 更新集群配置。
     *
     * @param loginUser 登录用户
     * @param code      集群编码
     * @param name      新集群名称
     * @param config    新集群配置
     * @param desc      新集群描述
     * @return 更新结果
     */
    Map<String, Object> updateClusterByCode(User loginUser, Long code, String name, String config, String desc);

    /**
     * 分页查询集群列表。
     *
     * @param pageNo    页码
     * @param searchVal 搜索关键字
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result queryClusterListPaging(Integer pageNo, Integer pageSize, String searchVal);

    /**
     * 查询所有集群列表。
     *
     * @return 所有集群列表
     */
    Map<String, Object> queryAllClusterList();

    /**
     * 校验集群名称是否可用。
     *
     * @param clusterName 集群名称
     * @return 校验结果
     */
    Map<String, Object> verifyCluster(String clusterName);

}

