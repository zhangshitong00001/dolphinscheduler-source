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

package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.entity.DataSource;

import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 数据源 Mapper 接口，封装对 t_ds_datasource 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供数据源的分页查询、按类型/名称/用户查询、授权管理等功能。
 */
public interface DataSourceMapper extends BaseMapper<DataSource> {

    /**
     * 根据用户ID和数据源类型查询数据源列表。
     * SELECT * FROM t_ds_datasource WHERE user_id = #{userId} AND type = #{type}
     *
     * @param userId 用户ID
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSource> queryDataSourceByType(@Param("userId") int userId, @Param("type") Integer type);

    /**
     * 分页查询数据源列表，支持按用户ID过滤和名称 LIKE 模糊搜索。
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @param name 数据源名称关键字，用于 LIKE 模糊匹配
     * @return 数据源分页结果
     */
    IPage<DataSource> selectPaging(IPage<DataSource> page,
                                   @Param("userId") int userId,
                                   @Param("name") String name);

    /**
     * 根据数据源名称精确查询数据源列表。
     * SELECT * FROM t_ds_datasource WHERE name = #{name}
     *
     * @param name 数据源名称
     * @return 数据源列表
     */
    List<DataSource> queryDataSourceByName(@Param("name") String name);


    /**
     * 查询指定用户已授权访问的数据源列表。
     * 通过 LEFT JOIN t_ds_datasource_user 表查找用户有权限的数据源。
     *
     * @param userId 用户ID
     * @return 用户授权数据源列表
     */
    List<DataSource> queryAuthedDatasource(@Param("userId") int userId);

    /**
     * 查询除指定用户之外的所有数据源列表。
     * SELECT * FROM t_ds_datasource WHERE user_id != #{userId}
     *
     * @param userId 要排除的用户ID
     * @return 数据源列表
     */
    List<DataSource> queryDatasourceExceptUserId(@Param("userId") int userId);

    /**
     * 根据数据源类型查询所有数据源列表（不限制用户）。
     * SELECT * FROM t_ds_datasource WHERE type = #{type}
     *
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSource> listAllDataSourceByType(@Param("type") Integer type);


    /**
     * 查询用户在指定数据源ID列表中有权访问的数据源。
     * SELECT * FROM t_ds_datasource WHERE id IN (#{dataSourceIds})
     * 并根据用户权限进行进一步过滤。
     *
     * @param userId 用户ID
     * @param dataSourceIds 数据源ID数组
     * @param <T> ID类型参数
     * @return 用户有权访问的数据源列表
     */
    <T> List<DataSource> listAuthorizedDataSource(@Param("userId") int userId,@Param("dataSourceIds")T[] dataSourceIds);

    /**
     * 根据数据源名称和用户ID精确查询数据源。
     * SELECT * FROM t_ds_datasource WHERE name = #{name} AND user_id = #{userId}
     *
     * @param userId 用户ID
     * @param name 数据源名称
     * @return 数据源实体，若名称不存在或用户无权限则返回 null
     */
    DataSource queryDataSourceByNameAndUserId(@Param("userId") int userId, @Param("name") String name);

    /**
     * 根据指定的数据源ID列表分页查询数据源，支持按名称关键字过滤。
     * SELECT * FROM t_ds_datasource WHERE id IN (#{dataSourceIds}) AND name LIKE CONCAT('%', #{name}, '%')
     *
     * @param dataSourcePage 分页对象
     * @param dataSourceIds 数据源ID列表
     * @param name 数据源名称搜索关键字
     * @return 数据源分页结果
     */
    IPage<DataSource> selectPagingByIds(Page<DataSource> dataSourcePage, @Param("dataSourceIds")List<Integer> dataSourceIds, @Param("name")String name);
}
