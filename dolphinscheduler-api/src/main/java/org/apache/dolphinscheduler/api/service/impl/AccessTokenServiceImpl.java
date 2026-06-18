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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_UPDATE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 访问令牌服务实现类。负责访问令牌的增删改查和权限校验，支持令牌的自动生成和过期管理。
 */
@Service
public class AccessTokenServiceImpl extends BaseServiceImpl implements AccessTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    /**
     * 分页查询访问令牌列表。管理员可查看所有令牌，普通用户仅查看自己的令牌。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索关键字
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页令牌列表的结果对象
     */
    @Override
    public Result queryAccessTokenList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        PageInfo<AccessToken> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<AccessToken> page = new Page<>(pageNo, pageSize);
        int userId = loginUser.getId();
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            userId = 0;
        }
        IPage<AccessToken> accessTokenList = accessTokenMapper.selectAccessTokenPage(page, searchVal, userId);
        pageInfo.setTotal((int) accessTokenList.getTotal());
        pageInfo.setTotalList(accessTokenList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 查询指定用户的访问令牌列表。普通用户只能查询自己的令牌。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @return 包含令牌列表的结果Map
     */
    @Override
    public Map<String, Object> queryAccessTokenByUser(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        // no permission
        if (loginUser.getUserType().equals(UserType.GENERAL_USER) && loginUser.getId() != userId) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        userId = loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : userId;
        // query access token for specified user
        List<AccessToken> accessTokenList = this.accessTokenMapper.queryAccessTokenByUser(userId);
        result.put(Constants.DATA_LIST, accessTokenList);
        this.putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 创建访问令牌。如果未提供token则自动生成，同时校验权限和用户有效性。
     *
     * @param loginUser 当前登录用户
     * @param userId 令牌所属用户ID
     * @param expireTime 令牌过期时间
     * @param token 令牌字符串（为空时自动生成）
     * @return 包含创建结果的结果对象
     */
    @SuppressWarnings("checkstyle:WhitespaceAround")
    @Override
    public Result createToken(User loginUser, int userId, String expireTime, String token) {
        Result result = new Result();

        // 1. check permission
        if (!(canOperatorPermissions(loginUser,null, AuthorizationType.ACCESS_TOKEN,ACCESS_TOKEN_CREATE) || loginUser.getId() == userId)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 2. check if user is existed
        if (userId <= 0) {
            String errorMsg = "User id should not less than or equals to 0.";
            logger.error(errorMsg);
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, errorMsg);
            return result;
        }

        // 3. generate access token if absent
        if (StringUtils.isBlank(token)) {
            token = EncryptionUtils.getMd5(userId + expireTime + System.currentTimeMillis());
        }

        // 4. persist to the database
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());

        int insert = accessTokenMapper.insert(accessToken);

        if (insert > 0) {
            result.setData(accessToken);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ACCESS_TOKEN_ERROR);
        }

        return result;
    }

    /**
     * 生成令牌字符串。基于用户ID、过期时间和当前时间戳生成MD5令牌。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @param expireTime 令牌过期时间
     * @return 包含生成的令牌字符串的结果Map
     */
    @Override
    public Map<String, Object> generateToken(User loginUser, int userId, String expireTime) {
        Map<String, Object> result = new HashMap<>();
        String token = EncryptionUtils.getMd5(userId + expireTime + System.currentTimeMillis());
        result.put(Constants.DATA_LIST, token);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 根据ID删除访问令牌。管理员可删除任意令牌，普通用户只能删除自己的令牌。
     *
     * @param loginUser 当前登录用户
     * @param id 令牌ID
     * @return 包含删除结果的结果Map
     */
    @Override
    public Map<String, Object> delAccessTokenById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ACCESS_TOKEN,ACCESS_TOKEN_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        AccessToken accessToken = accessTokenMapper.selectById(id);
        if (accessToken == null) {
            logger.error("access token not exist,  access token id {}", id);
            putMsg(result, Status.ACCESS_TOKEN_NOT_EXIST);
            return result;
        }

        // admin can operate all, non-admin can operate their own
        if (accessToken.getUserId() != loginUser.getId() && !loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        accessTokenMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 根据ID更新访问令牌。校验权限后更新令牌的用户、过期时间和令牌字符串。
     *
     * @param loginUser 当前登录用户
     * @param id 令牌ID
     * @param userId 新的用户ID
     * @param expireTime 新的过期时间
     * @param token 新的令牌字符串（为空时自动生成）
     * @return 包含更新后的令牌实体的结果Map
     */
    @Override
    public Map<String, Object> updateToken(User loginUser, int id, int userId, String expireTime, String token) {
        Map<String, Object> result = new HashMap<>();

        // 1. check permission
        if (!canOperatorPermissions(loginUser, null,AuthorizationType.ACCESS_TOKEN,ACCESS_TOKEN_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 2. check if token is existed
        AccessToken accessToken = accessTokenMapper.selectById(id);
        if (accessToken == null) {
            logger.error("access token not exist,  access token id {}", id);
            putMsg(result, Status.ACCESS_TOKEN_NOT_EXIST);
            return result;
        }
        // admin can operate all, non-admin can operate their own
        if (accessToken.getUserId() != loginUser.getId() && !loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 3. generate access token if absent
        if (StringUtils.isBlank(token)) {
            token = EncryptionUtils.getMd5(userId + expireTime + System.currentTimeMillis());
        }

        // 4. persist to the database
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setUpdateTime(new Date());

        accessTokenMapper.updateById(accessToken);

        result.put(Constants.DATA_LIST, accessToken);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
