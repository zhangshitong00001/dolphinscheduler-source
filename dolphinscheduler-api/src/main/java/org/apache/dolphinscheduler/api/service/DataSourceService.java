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
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.Map;

/**
 * 数据源服务接口。提供数据源的完整生命周期管理，包括创建、更新、查询、删除、连接测试以及表结构查询。
 * 支持多种数据库类型的数据源配置，并提供授权与未授权数据源的查询功能。
 */
public interface DataSourceService {

    /**
     * 创建数据源。
     *
     * @param loginUser       登录用户
     * @param datasourceParam 数据源参数
     * @return 创建结果
     */
    Result<Object> createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam);

    /**
     * 更新数据源配置。
     *
     * @param loginUser       登录用户
     * @param id              数据源ID
     * @param dataSourceParam 数据源参数
     * @return 更新结果
     */
    Result<Object> updateDataSource(int id, User loginUser, BaseDataSourceParamDTO dataSourceParam);

    /**
     * 根据ID查询数据源详情。
     *
     * @param id 数据源ID
     * @return 数据源详细信息
     */
    Map<String, Object> queryDataSource(int id);

    /**
     * 分页查询数据源列表。
     *
     * @param loginUser 登录用户
     * @param searchVal 搜索关键字
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * 按类型查询数据源列表。
     *
     * @param loginUser 登录用户
     * @param type      数据源类型
     * @return 数据源列表
     */
    Map<String, Object> queryDataSourceList(User loginUser, Integer type);

    /**
     * 校验数据源名称是否可用。
     *
     * @param name 数据源名称
     * @return 校验结果
     */
    Result<Object> verifyDataSourceName(String name);

    /**
     * 检查数据源连接是否可用。
     *
     * @param type      数据源类型
     * @param parameter 数据源连接参数
     * @return 连接测试结果
     */
    Result<Object> checkConnection(DbType type, ConnectionParam parameter);

    /**
     * 测试指定数据源的连接。
     *
     * @param id 数据源ID
     * @return 连接测试结果
     */
    Result<Object> connectionTest(int id);

    /**
     * 删除数据源。
     *
     * @param loginUser    登录用户
     * @param datasourceId 数据源ID
     * @return 删除结果
     */
    Result<Object> delete(User loginUser, int datasourceId);

    /**
     * 查询指定用户未授权的数据源列表。
     *
     * @param loginUser 登录用户
     * @param userId    用户ID
     * @return 未授权数据源列表
     */
    Map<String, Object> unauthDatasource(User loginUser, Integer userId);

    /**
     * 查询指定用户已授权的数据源列表。
     *
     * @param loginUser 登录用户
     * @param userId    用户ID
     * @return 已授权数据源列表
     */
    Map<String, Object> authedDatasource(User loginUser, Integer userId);

    /**
     * 获取数据源中的表列表。
     *
     * @param datasourceId 数据源ID
     * @return 表列表
     */
    Map<String, Object> getTables(Integer datasourceId);

    /**
     * 获取指定表的列信息。
     *
     * @param datasourceId 数据源ID
     * @param tableName    表名
     * @return 列信息列表
     */
    Map<String, Object> getTableColumns(Integer datasourceId,String tableName);
}
