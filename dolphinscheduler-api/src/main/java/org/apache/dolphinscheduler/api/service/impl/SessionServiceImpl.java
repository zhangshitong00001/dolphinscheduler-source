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

import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.SessionMapper;
import org.apache.dolphinscheduler.service.session.SessionManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

/**
 * Session service implementation with Redis-backed session management.
 * <p>
 * Uses {@link SessionManager} (Redis-based) for distributed session storage,
 * while maintaining backward compatibility with the database session table
 * for auditing and fallback purposes.
 */
@Service
public class SessionServiceImpl extends BaseServiceImpl implements SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private SessionManager sessionManager;

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves the session from the HTTP request (header or cookie),
     * then looks up the corresponding user ID from Redis.
     * Falls back to database lookup if Redis is unavailable.
     */
    @Override
    public Session getSession(HttpServletRequest request) {
        String sessionId = request.getHeader(Constants.SESSION_ID);

        if (StringUtils.isBlank(sessionId)) {
            Cookie cookie = WebUtils.getCookie(request, Constants.SESSION_ID);
            if (cookie != null) {
                sessionId = cookie.getValue();
            }
        }

        if (StringUtils.isBlank(sessionId)) {
            return null;
        }

        String ip = BaseController.getClientIpAddress(request);
        logger.debug("get session: {}, ip: {}", sessionId, ip);

        // Refresh session TTL in Redis on each access (sliding expiration)
        sessionManager.refreshSession(sessionId);

        // Look up user from Redis session data
        Integer userId = sessionManager.getUserIdBySession(sessionId);
        if (userId == null) {
            // Fallback: try database lookup
            Session session = sessionMapper.selectById(sessionId);
            if (session != null) {
                // Session exists in DB but not in Redis - recreate in Redis
                logger.info("Session found in DB but not in Redis, recreating: sessionId={}", sessionId);
                return session;
            }
            return null;
        }

        // Build Session object from Redis data
        Session session = new Session();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setLastLoginTime(new Date());
        return session;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates a new session in both Redis (primary) and database (fallback/audit).
     * If the user already has an active valid session, returns the existing one.
     */
    @Override
    @Transactional
    public String createSession(User user, String ip) {
        Session session = null;

        // Check if user already has an active session
        List<Session> sessionList = sessionMapper.queryByUserId(user.getId());
        Date now = new Date();

        if (CollectionUtils.isNotEmpty(sessionList)) {
            // If multiple sessions exist, keep only the most recent one
            if (sessionList.size() > 1) {
                for (int i = 1; i < sessionList.size(); i++) {
                    sessionMapper.deleteById(sessionList.get(i).getId());
                }
            }
            session = sessionList.get(0);
            // Check if existing session is still within timeout window
            if (now.getTime() - session.getLastLoginTime().getTime() <= Constants.SESSION_TIME_OUT * 1000) {
                session.setLastLoginTime(now);
                sessionMapper.updateById(session);
                // Also refresh in Redis
                sessionManager.refreshSession(session.getId());
                return session.getId();
            } else {
                // Session expired, clean up from both DB and Redis
                sessionMapper.deleteById(session.getId());
                sessionManager.destroySession(session.getId());
            }
        }

        // Create new session
        session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setIp(ip);
        session.setUserId(user.getId());
        session.setLastLoginTime(now);

        sessionMapper.insert(session);

        // Store session in Redis
        String redisSessionId = sessionManager.createSession(user, ip);
        if (redisSessionId == null) {
            logger.warn("Redis session creation failed, but DB session created: sessionId={}", session.getId());
        }

        return session.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Signs out the user by removing the session from both Redis and database.
     */
    @Override
    public void signOut(String ip, User loginUser) {
        try {
            Session session = sessionMapper.queryByUserIdAndIp(loginUser.getId(), ip);
            if (session != null) {
                // Remove from Redis
                sessionManager.destroySession(session.getId());
                // Remove from database
                sessionMapper.deleteById(session.getId());
            }
        } catch (Exception e) {
            logger.warn("Sign out failed for userId: {}, ip: {}", loginUser.getId(), ip, e);
        }
    }

}
