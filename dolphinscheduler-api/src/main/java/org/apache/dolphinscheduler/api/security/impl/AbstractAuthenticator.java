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

package org.apache.dolphinscheduler.api.security.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.security.SecurityConfig;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象认证器。实现认证流程的核心逻辑：调用子类的login方法进行凭证验证，检查用户状态，
 * 创建会话并返回认证结果。子类只需实现具体的登录逻辑（如密码验证、LDAP验证）。
 *
 * 认证流程：
 * 1. 调用子类login()方法验证用户名密码
 * 2. 检查用户状态是否启用
 * 3. 创建Session会话
 * 4. 返回包含sessionId和安全配置类型的认证结果
 */
public abstract class AbstractAuthenticator implements Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    @Autowired
    protected UsersService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SecurityConfig securityConfig;

    /**
     * 子类实现具体的用户登录逻辑，通过用户名和密码在数据库或LDAP中验证用户。
     *
     * @param userId   用户身份标识
     * @param password 用户登录密码
     * @param extra    额外登录信息
     * @return 数据库中的用户对象，验证失败返回null
     */
    public abstract User login(String userId, String password, String extra);

    @Override
    public Result<Map<String, String>> authenticate(String userId, String password, String extra) {
        Result<Map<String, String>> result = new Result<>();
        User user = login(userId, password, extra);
        if (user == null) {
            result.setCode(Status.USER_NAME_PASSWD_ERROR.getCode());
            result.setMsg(Status.USER_NAME_PASSWD_ERROR.getMsg());
            return result;
        }

        // check user state
        if (user.getState() == Flag.NO.ordinal()) {
            result.setCode(Status.USER_DISABLED.getCode());
            result.setMsg(Status.USER_DISABLED.getMsg());
            return result;
        }

        // create session
        String sessionId = sessionService.createSession(user, extra);
        if (sessionId == null) {
            result.setCode(Status.LOGIN_SESSION_FAILED.getCode());
            result.setMsg(Status.LOGIN_SESSION_FAILED.getMsg());
            return result;
        }

        logger.info("sessionId : {}", sessionId);

        Map<String, String> data = new HashMap<>();
        data.put(Constants.SESSION_ID, sessionId);
        data.put(Constants.SECURITY_CONFIG_TYPE, securityConfig.getType());

        result.setData(data);
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.LOGIN_SUCCESS.getMsg());
        return result;
    }

    @Override
    public User getAuthUser(HttpServletRequest request) {
        Session session = sessionService.getSession(request);
        if (session == null) {
            logger.info("session info is null ");
            return null;
        }
        //get user object from session
        return userService.queryUser(session.getUserId());
    }

}
