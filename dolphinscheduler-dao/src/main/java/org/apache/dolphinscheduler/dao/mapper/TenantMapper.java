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
import org.apache.dolphinscheduler.dao.entity.Tenant;

import org.apache.ibatis.annotations.Param;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户 Mapper 接口，封装对 t_ds_tenant 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供租户的查询、分页、存在性校验及缓存管理等能力。
 * 查询结果使用 Spring Cache 缓存，缓存名称为 "tenant"。
 */
@CacheConfig(cacheNames = "tenant", keyGenerator = "cacheKeyGenerator")
public interface TenantMapper extends BaseMapper<Tenant> {

    /**
     * 根据租户ID查询租户信息，结果会被缓存以提高性能。
     * SELECT * FROM t_ds_tenant WHERE id = #{tenantId}
     *
     * @param tenantId 租户ID
     * @return 租户实体，若不存在则返回 null
     */
    @Cacheable(sync = true)
    Tenant queryById(@Param("tenantId") int tenantId);

    /**
     * 根据主键ID删除租户，同时清除对应的缓存。
     * DELETE FROM t_ds_tenant WHERE id = #{id}
     *
     * @param id 租户ID
     * @return 受影响的行数
     */
    @CacheEvict
    int deleteById(int id);

    /**
     * 根据租户实体更新租户信息，同时按实体主键清除对应缓存。
     * UPDATE t_ds_tenant SET ... WHERE id = #{et.id}
     *
     * @param tenant 包含更新信息的租户实体
     * @return 受影响的行数
     */
    @CacheEvict(key = "#p0.id")
    int updateById(@Param("et") Tenant tenant);

    /**
     * 根据租户编码精确查询租户信息。
     * SELECT * FROM t_ds_tenant WHERE tenant_code = #{tenantCode}
     *
     * @param tenantCode 租户编码
     * @return 租户实体，若不存在则返回 null
     */
    Tenant queryByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * 分页查询租户列表，支持按用户ID列表和搜索关键字筛选。
     * SELECT * FROM t_ds_tenant WHERE id IN (...) AND tenant_name LIKE CONCAT('%', #{searchVal}, '%')
     *
     * @param page 分页对象
     * @param ids 用户ID列表（用于权限过滤）
     * @param searchVal 搜索关键字
     * @return 租户分页结果
     */
    IPage<Tenant> queryTenantPaging(IPage<Tenant> page,@Param("ids") List<Integer> ids,
                                    @Param("searchVal") String searchVal);

    /**
     * 检查指定租户编码的租户是否已存在。
     * SELECT COUNT(*) > 0 FROM t_ds_tenant WHERE tenant_code = #{tenantCode}
     *
     * @param tenantCode 租户编码
     * @return 存在返回 true，否则返回 false
     */
    Boolean existTenant(@Param("tenantCode") String tenantCode);

    /**
     * 按用户ID列表分页查询租户，支持搜索关键字筛选。
     * SELECT * FROM t_ds_tenant WHERE id IN (...) AND tenant_name LIKE CONCAT('%', #{searchVal}, '%')
     *
     * @param page 分页对象
     * @param ids 用户ID列表
     * @param searchVal 搜索关键字
     * @return 租户分页结果
     */
    IPage<Tenant> queryTenantPagingByIds(Page<Tenant> page, @Param("ids")List<Integer> ids, @Param("searchVal")String searchVal);

    /**
     * 查询所有租户列表。
     * SELECT * FROM t_ds_tenant
     *
     * @return 所有租户列表
     */
    List<Tenant> queryAll();
}
