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

import org.apache.dolphinscheduler.dao.entity.AccessToken;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 访问令牌 Mapper 接口，封装对 t_ds_access_token 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供访问令牌的分页查询、按用户查询及授权管理等能力。
 */
public interface AccessTokenMapper extends BaseMapper<AccessToken> {


    /**
     * 分页查询访问令牌列表，支持按用户ID和用户名过滤。
     * 通过 userName 进行 LIKE 模糊匹配，userId 进行精确匹配。
     *
     * @param page 分页对象
     * @param userId 用户ID，用于精确过滤
     * @param userName 用户名，用于模糊查询
     * @return 访问令牌分页结果
     */
    IPage<AccessToken> selectAccessTokenPage(Page page,
                                             @Param("userName") String userName,
                                             @Param("userId") int userId
    );

    /**
     * 查询指定用户的所有访问令牌。
     * SELECT * FROM t_ds_access_token WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 该用户的访问令牌列表
     */
    List<AccessToken> queryAccessTokenByUser(@Param("userId") int userId);

    /**
     * 根据用户ID删除该用户的所有访问令牌。
     * DELETE FROM t_ds_access_token WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteAccessTokenByUserId(@Param("userId") int userId);

    /**
     * 查询用户在指定令牌ID列表中有权限的访问令牌列表。
     * SELECT * FROM t_ds_access_token WHERE user_id = #{userId} AND id IN (#{accessTokensIds})
     *
     * @param userId 用户ID
     * @param accessTokensIds 令牌ID列表
     * @return 授权访问令牌列表
     */
    List<AccessToken> listAuthorizedAccessToken(@Param("userId") int userId, @Param("accessTokensIds")List<Integer> accessTokensIds);
}
