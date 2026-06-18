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

package org.apache.dolphinscheduler.service.redis;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

/**
 * Distributed lock using Redis SET NX with Lua-based safe unlock.
 * <p>
 * Key features:
 * <ul>
 *   <li>Unique lock ID per instance prevents accidental release of other's locks</li>
 *   <li>Lua script ensures atomic check-ownership-then-release</li>
 *   <li>TTL-based auto-expiry prevents deadlocks on process crash</li>
 * </ul>
 */
@Slf4j
@Service
public class RedisDistributedLock {

    private static final String LOCK_KEY_PREFIX = "dolphinscheduler:lock:";

    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end",
            Long.class
    );

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final String lockId = UUID.randomUUID().toString();

    /**
     * Try to acquire a distributed lock.
     *
     * @param key     lock key
     * @param timeout lock TTL
     * @param unit    time unit
     * @return true if acquired
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        String lockKey = buildLockKey(key);
        try {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockId, timeout, unit);
            boolean result = Boolean.TRUE.equals(acquired);
            if (result) {
                log.debug("Lock acquired: key={}", lockKey);
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to acquire lock: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * Try to acquire with 30s default timeout.
     */
    public boolean tryLock(String key) {
        return tryLock(key, 30, TimeUnit.SECONDS);
    }

    /**
     * Release lock if owned by this instance.
     *
     * @param key lock key
     * @return true if released
     */
    public boolean unlock(String key) {
        String lockKey = buildLockKey(key);
        try {
            Long result = stringRedisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    lockId
            );
            boolean released = result != null && result > 0;
            if (released) {
                log.debug("Lock released: key={}", lockKey);
            }
            return released;
        } catch (Exception e) {
            log.warn("Failed to release lock: key={}", lockKey, e);
            return false;
        }
    }

    private static String buildLockKey(String key) {
        return LOCK_KEY_PREFIX + key;
    }
}
