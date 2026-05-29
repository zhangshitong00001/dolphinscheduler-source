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

package org.apache.dolphinscheduler.service.session;

import org.apache.dolphinscheduler.dao.entity.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis-based implementation of SessionManager.
 * <p>
 * This implementation stores session data in Redis, enabling distributed session sharing
 * across multiple API server nodes. Key design decisions:
 * <p>
 * 1. <b>Session Data</b>: Stored as JSON in Redis key {@code dolphinscheduler:session:{sessionId}}.
 * Contains userId and IP address.
 * <p>
 * 2. <b>User-to-Session Mapping</b>: Stored as {@code dolphinscheduler:user_session:{userId}}
 * for quick lookup of active sessions per user.
 * <p>
 * 3. <b>Sliding Expiration</b>: Each user operation refreshes the session TTL,
 * keeping the session alive as long as the user is active.
 * <p>
 * 4. <b>Automatic Cleanup</b>: Redis TTL handles expired session cleanup automatically,
 * eliminating the need for a separate cleanup job.
 */
@Service
public class RedisSessionManager implements SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisSessionManager.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String createSession(User user, String ip) {
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_KEY_PREFIX + user.getId();

        try {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", user.getId());
            sessionData.put("ip", ip);
            sessionData.put("userName", user.getUserName());
            String sessionJson = objectMapper.writeValueAsString(sessionData);

            stringRedisTemplate.opsForValue().set(sessionKey, sessionJson,
                    DEFAULT_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(userSessionKey, sessionId,
                    DEFAULT_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            logger.info("Session created: sessionId={}, userId={}, ip={}", sessionId, user.getId(), ip);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize session data for user: {}", user.getId(), e);
            return null;
        }

        return sessionId;
    }

    @Override
    public Integer getUserIdBySession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String sessionJson = stringRedisTemplate.opsForValue().get(sessionKey);

        if (sessionJson == null) {
            logger.debug("Session not found or expired: sessionId={}", sessionId);
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionData = objectMapper.readValue(sessionJson, Map.class);
            Object userIdObj = sessionData.get("userId");
            if (userIdObj instanceof Integer) {
                return (Integer) userIdObj;
            } else if (userIdObj instanceof Number) {
                return ((Number) userIdObj).intValue();
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize session data for sessionId: {}", sessionId, e);
        }

        return null;
    }

    @Override
    public boolean refreshSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        Boolean result = stringRedisTemplate.expire(sessionKey,
                DEFAULT_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(result)) {
            logger.debug("Session refreshed: sessionId={}", sessionId);
            return true;
        }

        logger.debug("Session refresh failed (may have expired): sessionId={}", sessionId);
        return false;
    }

    @Override
    public void destroySession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        Integer userId = getUserIdBySession(sessionId);
        if (userId != null) {
            String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
            stringRedisTemplate.delete(userSessionKey);
            logger.info("User-session mapping removed: userId={}", userId);
        }

        stringRedisTemplate.delete(sessionKey);
        logger.info("Session destroyed: sessionId={}", sessionId);
    }

    @Override
    public boolean isValid(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        Boolean exists = stringRedisTemplate.hasKey(sessionKey);
        return Boolean.TRUE.equals(exists);
    }
}
