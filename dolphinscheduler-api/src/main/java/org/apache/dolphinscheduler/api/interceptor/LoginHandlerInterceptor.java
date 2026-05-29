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

package org.apache.dolphinscheduler.api.interceptor;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.thread.ThreadLocalContext;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.session.SessionManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Login interceptor - validates user authentication on every request.
 * <p>
 * Features:
 * <ul>
 *   <li>Validates session token from request header or cookie</li>
 *   <li>Refreshes Redis session TTL on each request (sliding expiration)</li>
 *   <li>If session expired (10 min inactivity), returns 401 and frontend redirects to login</li>
 *   <li>Checks user enabled/disabled state</li>
 * </ul>
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlerInterceptor.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private SessionManager sessionManager;

    /**
     * Intercepts every HTTP request to verify user authentication.
     * <p>
     * Steps:
     * 1. Extract session ID from header or cookie
     * 2. Refresh session TTL in Redis (sliding expiration)
     * 3. If session invalid/expired, return 401 Unauthorized
     * 4. Look up user and verify they are enabled
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Get session token from request
        String token = request.getHeader("token");
        User user;

        if (StringUtils.isEmpty(token)) {
            // Authenticate via session cookie/header
            user = authenticator.getAuthUser(request);
            if (user == null) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                logger.info("User authentication failed - session invalid or expired");
                return false;
            }
            // Refresh Redis session TTL (sliding expiration)
            String sessionId = request.getHeader(Constants.SESSION_ID);
            if (StringUtils.isNotEmpty(sessionId)) {
                sessionManager.refreshSession(sessionId);
            }
        } else {
            // Authenticate via token
            user = userMapper.queryUserByToken(token, new Date());
            if (user == null) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                logger.info("User token has expired");
                return false;
            }
        }

        // Check if user account is enabled
        if (user.getState() == Flag.NO.ordinal()) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            logger.info(Status.USER_DISABLED.getMsg());
            return false;
        }

        request.setAttribute(Constants.SESSION_USER, user);
        ThreadLocalContext.getTimezoneThreadLocal().set(user.getTimeZone());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        ThreadLocalContext.getTimezoneThreadLocal().remove();
    }
}
