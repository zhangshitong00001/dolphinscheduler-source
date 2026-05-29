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

import java.util.concurrent.TimeUnit;

/**
 * SessionManager interface for managing user sessions with Redis.
 * Provides distributed session management supporting:
 * - Session creation with configurable TTL (10 minutes)
 * - Automatic session expiry and cleanup
 * - Session refresh on user activity
 * - Distributed deployment support via Redis
 */
public interface SessionManager {

    /**
     * Redis key prefix for session entries.
     */
    String SESSION_KEY_PREFIX = "dolphinscheduler:session:";

    /**
     * Redis key prefix for user-to-session mapping.
     */
    String USER_SESSION_KEY_PREFIX = "dolphinscheduler:user_session:";

    /**
     * Default session timeout in seconds (10 minutes).
     */
    long DEFAULT_SESSION_TIMEOUT_SECONDS = 600L;

    /**
     * Creates a new session for the given user and IP address.
     * Stores session info in Redis with TTL of 10 minutes.
     *
     * @param user the authenticated user
     * @param ip   the client IP address
     * @return the generated session ID
     */
    String createSession(User user, String ip);

    /**
     * Retrieves the user ID associated with the given session ID.
     * Returns null if the session does not exist or has expired.
     *
     * @param sessionId the session ID to look up
     * @return the user ID, or null if session is invalid/expired
     */
    Integer getUserIdBySession(String sessionId);

    /**
     * Refreshes the session TTL (time-to-live) on user activity.
     * This ensures the session remains valid for another 10 minutes
     * from the last operation timestamp.
     *
     * @param sessionId the session ID to refresh
     * @return true if the session was refreshed successfully, false otherwise
     */
    boolean refreshSession(String sessionId);

    /**
     * Destroys (removes) a session, typically on user logout.
     * Clears both the session entry and the user-to-session mapping.
     *
     * @param sessionId the session ID to destroy
     */
    void destroySession(String sessionId);

    /**
     * Checks if a session is still valid (exists and not expired).
     *
     * @param sessionId the session ID to check
     * @return true if the session is valid, false otherwise
     */
    boolean isValid(String sessionId);
}
