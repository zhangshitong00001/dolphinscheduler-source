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
 * 访问令牌服务接口。提供访问令牌的CRUD操作，用于API认证。
 * 支持分页查询、按用户查询、创建、生成、更新和删除访问令牌。
 */
public interface AccessTokenService {

    /**
     * 分页查询访问令牌列表。
     *
     * @param loginUser 登录用户
     * @param searchVal 搜索关键字
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result queryAccessTokenList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * 查询指定用户的访问令牌。
     *
     * @param loginUser 登录用户
     * @param userId    用户ID
     * @return 指定用户的令牌查询结果
     */
    Map<String, Object> queryAccessTokenByUser(User loginUser, Integer userId);

    /**
     * 创建访问令牌。
     *
     * @param loginUser  登录用户
     * @param userId     关联的用户ID
     * @param expireTime 过期时间
     * @param token      令牌字符串，为空时自动生成
     * @return 创建结果
     */
    Result createToken(User loginUser, int userId, String expireTime, String token);


    /**
     * 生成访问令牌字符串。
     *
     * @param loginUser  登录用户
     * @param userId     关联的用户ID
     * @param expireTime 过期时间
     * @return 生成的令牌结果
     */
    Map<String, Object> generateToken(User loginUser, int userId, String expireTime);

    /**
     * 删除指定ID的访问令牌。
     *
     * @param loginUser 登录用户
     * @param id        令牌ID
     * @return 删除结果
     */
    Map<String, Object> delAccessTokenById(User loginUser, int id);

    /**
     * 更新指定ID的访问令牌。
     *
     * @param loginUser  登录用户
     * @param id         令牌ID
     * @param userId     关联的用户ID
     * @param expireTime 过期时间
     * @param token      令牌字符串，为空时自动生成
     * @return 更新结果
     */
    Map<String, Object> updateToken(User loginUser, int id, int userId, String expireTime, String token);
}
