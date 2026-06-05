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

import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户 Mapper 接口，封装对 t_ds_user 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供用户的查询、分页、认证、授权及缓存管理等能力。
 * 查询结果使用 Spring Cache 缓存，缓存名称为 "user"。
 */
@CacheConfig(cacheNames = "user", keyGenerator = "cacheKeyGenerator")
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据主键ID查询用户信息，结果会被缓存以提高性能。
     * SELECT * FROM t_ds_user WHERE id = #{id}
     *
     * @param id 用户ID
     * @return 用户实体，若不存在则返回 null
     */
    @Cacheable(sync = true)
    User selectById(int id);

    /**
     * 根据主键ID删除用户，同时清除对应的缓存。
     * DELETE FROM t_ds_user WHERE id = #{id}
     *
     * @param id 用户ID
     * @return 受影响的行数
     */
    @CacheEvict
    int deleteById(int id);

    /**
     * 根据用户实体更新用户信息，同时按实体主键清除对应缓存。
     * UPDATE t_ds_user SET ... WHERE id = #{et.id}
     *
     * @param user 包含更新信息的用户实体
     * @return 受影响的行数
     */
    @CacheEvict(key = "#p0.id")
    int updateById(@Param("et") User user);

    /**
     * 查询所有普通用户列表（排除管理员账号）。
     * SELECT * FROM t_ds_user WHERE user_type = 1
     *
     * @return 普通用户列表
     */
    List<User> queryAllGeneralUser();

    /**
     * 根据用户名精确查询用户信息。
     * SELECT * FROM t_ds_user WHERE user_name = #{userName}
     *
     * @param userName 用户名
     * @return 用户实体，若不存在则返回 null
     */
    User queryByUserNameAccurately(@Param("userName") String userName);

    /**
     * 根据用户名和密码查询用户，用于登录认证。
     * SELECT * FROM t_ds_user WHERE user_name = #{userName} AND user_password = #{password}
     *
     * @param userName 用户名
     * @param password 密码
     * @return 用户实体，若不存在则返回 null
     */
    User queryUserByNamePassword(@Param("userName") String userName, @Param("password") String password);


    /**
     * 分页查询用户列表，支持按用户名搜索。
     * SELECT * FROM t_ds_user WHERE user_name LIKE CONCAT('%', #{userName}, '%')
     *
     * @param page 分页对象
     * @param userName 用户名搜索关键字
     * @return 用户分页结果
     */
    IPage<User> queryUserPaging(Page page,
                                @Param("userName") String userName);

    /**
     * 根据用户ID查询用户详细信息（含关联的租户、队列等信息）。
     * SELECT * FROM t_ds_user WHERE id = #{userId}
     *
     * @param userId 用户ID
     * @return 用户详细信息实体，若不存在则返回 null
     */
    User queryDetailsById(@Param("userId") int userId);

    /**
     * 根据告警组ID查询其关联的所有用户列表。
     * SELECT * FROM t_ds_user WHERE alert_group_id = #{alertgroupId}
     *
     * @param alertgroupId 告警组ID
     * @return 用户列表
     */
    List<User> queryUserListByAlertGroupId(@Param("alertgroupId") int alertgroupId);

    /**
     * 根据租户ID查询关联的所有用户列表。
     * SELECT * FROM t_ds_user WHERE tenant_id = #{tenantId}
     *
     * @param tenantId 租户ID
     * @return 用户列表
     */
    List<User> queryUserListByTenant(@Param("tenantId") int tenantId);

    /**
     * 根据用户ID查询其关联的租户编码信息。
     * SELECT tenant_code FROM t_ds_user WHERE id = #{userId}
     *
     * @param userId 用户ID
     * @return 包含租户编码信息的用户实体，若不存在则返回 null
     */
    User queryTenantCodeByUserId(@Param("userId") int userId);

    /**
     * 根据访问令牌和当前时间查询用户，用于令牌认证。
     * SELECT * FROM t_ds_user WHERE token = #{token} AND token_expire_time > #{now}
     *
     * @param token 访问令牌
     * @param now 当前时间，用于校验令牌是否过期
     * @return 用户实体，若令牌无效或已过期则返回 null
     */
    User queryUserByToken(@Param("token") String token, @Param("now") Date now);

    /**
     * 根据队列名称查询关联的所有用户列表。
     * SELECT * FROM t_ds_user WHERE queue = #{queue}
     *
     * @param queueName 队列名称
     * @return 用户列表
     */
    List<User> queryUserListByQueue(@Param("queue") String queueName);

    /**
     * 检查指定队列是否有关联的用户。
     * SELECT COUNT(*) > 0 FROM t_ds_user WHERE queue = #{queue}
     *
     * @param queue 队列名称
     * @return 存在返回 true，否则返回 null/false
     */
    Boolean existUser(@Param("queue") String queue);

    /**
     * 将用户的旧队列名称更新为新队列名称。
     * UPDATE t_ds_user SET queue = #{newQueue} WHERE queue = #{oldQueue}
     *
     * @param oldQueue 旧队列名称
     * @param newQueue 新队列名称
     * @return 更新的行数
     */
    Integer updateUserQueue(@Param("oldQueue") String oldQueue, @Param("newQueue") String newQueue);

    /**
     * 根据用户ID列表批量查询用户。
     * SELECT * FROM t_ds_user WHERE id IN (...)
     *
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<User> selectByIds(@Param("ids") List<Integer> ids);

    /**
     * 查询指定项目中已授权的用户列表。
     * SELECT u.* FROM t_ds_user u JOIN t_ds_relation_project_user r ON u.id = r.user_id WHERE r.project_id = #{projectId}
     *
     * @param projectId 项目ID
     * @return 已授权的用户列表
     */
    List<User> queryAuthedUserListByProjectId(@Param("projectId") int projectId);

    /**
     * 查询所有已启用的用户列表。
     * SELECT * FROM t_ds_user WHERE state = 1
     *
     * @return 已启用的用户列表
     */
    List<User> queryEnabledUsers();
}
