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

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis-based cache service for manual caching operations.
 * <p>
 * Provides fine-grained control over caching beyond Spring's {@code @Cacheable} annotation,
 * supporting batch operations, pattern-based eviction, and cache-through patterns.
 * <p>
 * Key design decisions:
 * <ul>
 *   <li><b>Cache-Through</b>: {@link #getOrFetch} encapsulates cache miss -> DB query -> cache put</li>
 *   <li><b>Pattern Eviction</b>: {@link #evictByPattern} enables bulk invalidation by key prefix</li>
 *   <li><b>Batch Operations</b>: {@link #multiGet} and {@link #multiPut} reduce round-trips</li>
 *   <li><b>Null Value Protection</b>: Cache null placeholders to prevent cache penetration</li>
 * </ul>
 */
@Slf4j
@Service
public class RedisCacheService {

    /**
     * Prefix for all cache keys managed by this service.
     */
    public static final String CACHE_KEY_PREFIX = "dolphinscheduler:cache:";

    /**
     * Value stored when a cacheable entity does not exist in DB,
     * to prevent cache penetration attacks.
     */
    private static final String NULL_PLACEHOLDER = "__NULL__";

    /**
     * TTL for null placeholders (shorter than normal TTL to allow eventual recovery).
     */
    private static final long NULL_PLACEHOLDER_TTL_SECONDS = 60L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ---- Basic Cache Operations ----

    /**
     * Put a value into the cache with the given TTL.
     *
     * @param cacheName the logical cache name (used as key prefix)
     * @param key       the cache key
     * @param value     the value to cache (will be serialized as JSON)
     * @param ttl       time-to-live in seconds
     */
    public void put(String cacheName, String key, Object value, long ttl) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            String jsonValue = JSONUtils.toJsonString(value);
            stringRedisTemplate.opsForValue().set(redisKey, jsonValue, ttl, TimeUnit.SECONDS);
            log.debug("Cache put: cacheName={}, key={}, ttl={}s", cacheName, key, ttl);
        } catch (Exception e) {
            log.warn("Failed to put cache: cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * Put a value with the default TTL (30 minutes).
     */
    public void put(String cacheName, String key, Object value) {
        put(cacheName, key, value, 1800L);
    }

    /**
     * Put a null placeholder to prevent cache penetration when the entity does not exist.
     */
    private void putNullPlaceholder(String cacheName, String key) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            stringRedisTemplate.opsForValue().set(redisKey, NULL_PLACEHOLDER,
                    NULL_PLACEHOLDER_TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("Cache null placeholder: cacheName={}, key={}", cacheName, key);
        } catch (Exception e) {
            log.warn("Failed to put null placeholder: cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * Get a cached value and deserialize it to the given type.
     *
     * @param cacheName the logical cache name
     * @param key       the cache key
     * @param type      the target Java type
     * @param <T>       the type parameter
     * @return the cached value, or null if not found or if the null placeholder is stored
     */
    public <T> T get(String cacheName, String key, Class<T> type) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(redisKey);
            if (jsonValue == null) {
                return null;
            }
            if (NULL_PLACEHOLDER.equals(jsonValue)) {
                return null;
            }
            return JSONUtils.parseObject(jsonValue, type);
        } catch (Exception e) {
            log.warn("Failed to get cache: cacheName={}, key={}", cacheName, key, e);
            return null;
        }
    }

    /**
     * Get a cached value as a raw JSON string.
     */
    public String getRaw(String cacheName, String key) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(redisKey);
            if (jsonValue == null || NULL_PLACEHOLDER.equals(jsonValue)) {
                return null;
            }
            return jsonValue;
        } catch (Exception e) {
            log.warn("Failed to get raw cache: cacheName={}, key={}", cacheName, key, e);
            return null;
        }
    }

    /**
     * Evict a single cache entry.
     *
     * @param cacheName the logical cache name
     * @param key       the cache key
     */
    public void evict(String cacheName, String key) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            stringRedisTemplate.delete(redisKey);
            log.debug("Cache evicted: cacheName={}, key={}", cacheName, key);
        } catch (Exception e) {
            log.warn("Failed to evict cache: cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * Evict all cache entries matching the given key pattern.
     *
     * @param cacheName the logical cache name
     * @param pattern   the Redis key pattern (e.g., "*" for all keys in this cache)
     */
    public void evictByPattern(String cacheName, String pattern) {
        String keyPattern = CACHE_KEY_PREFIX + cacheName + ":" + pattern;
        try {
            Set<String> keys = stringRedisTemplate.keys(keyPattern);
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("Cache evicted by pattern: pattern={}, count={}", keyPattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache by pattern: pattern={}", keyPattern, e);
        }
    }

    /**
     * Evict all keys belonging to a cache namespace.
     */
    public void evictAll(String cacheName) {
        evictByPattern(cacheName, "*");
    }

    // ---- Batch Operations ----

    /**
     * Batch get multiple keys from the same cache namespace.
     *
     * @param cacheName the logical cache name
     * @param keys      the list of cache keys
     * @param type      the target Java type
     * @param <T>       the type parameter
     * @return map of key -> value; keys not found or null placeholders are excluded
     */
    public <T> Map<String, T> multiGet(String cacheName, List<String> keys, Class<T> type) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> redisKeys = keys.stream()
                .map(key -> buildRedisKey(cacheName, key))
                .collect(Collectors.toList());
        try {
            List<String> jsonValues = stringRedisTemplate.opsForValue().multiGet(redisKeys);
            if (jsonValues == null) {
                return Collections.emptyMap();
            }
            Map<String, T> result = new java.util.HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                String jsonValue = jsonValues.get(i);
                if (jsonValue != null && !NULL_PLACEHOLDER.equals(jsonValue)) {
                    T obj = JSONUtils.parseObject(jsonValue, type);
                    if (obj != null) {
                        result.put(keys.get(i), obj);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to multiGet cache: cacheName={}, keyCount={}", cacheName, keys.size(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Batch put multiple entries into the cache.
     */
    public void multiPut(String cacheName, Map<String, Object> entries, long ttl) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        try {
            Map<String, String> redisMap = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : entries.entrySet()) {
                String redisKey = buildRedisKey(cacheName, entry.getKey());
                redisMap.put(redisKey, JSONUtils.toJsonString(entry.getValue()));
            }
            stringRedisTemplate.opsForValue().multiSet(redisMap);
            for (String redisKey : redisMap.keySet()) {
                stringRedisTemplate.expire(redisKey, ttl, TimeUnit.SECONDS);
            }
            log.debug("Cache multiPut: cacheName={}, count={}", cacheName, entries.size());
        } catch (Exception e) {
            log.warn("Failed to multiPut cache: cacheName={}, count={}", cacheName, entries.size(), e);
        }
    }

    // ---- Cache-Through Pattern ----

    /**
     * Cache-through: get from cache, or fetch from data source on miss.
     * <p>
     * If the value is not in cache, calls {@code fetcher} to get it,
     * caches the result, and returns it. If null, caches a short-lived null placeholder.
     *
     * @param cacheName custom cache name
     * @param key       cache key
     * @param type      target type
     * @param ttl       TTL in seconds
     * @param fetcher   supplier on cache miss
     * @param <T>       type
     * @return cached or fetched value
     */
    public <T> T getOrFetch(String cacheName, String key, Class<T> type, long ttl, Supplier<T> fetcher) {
        T cached = get(cacheName, key, type);
        if (cached != null) {
            return cached;
        }

        String redisKey = buildRedisKey(cacheName, key);
        String val = stringRedisTemplate.opsForValue().get(redisKey);
        if (NULL_PLACEHOLDER.equals(val)) {
            return null;
        }

        try {
            T fetched = fetcher.get();
            if (fetched != null) {
                put(cacheName, key, fetched, ttl);
            } else {
                putNullPlaceholder(cacheName, key);
            }
            return fetched;
        } catch (Exception e) {
            log.warn("Failed to fetch data for cache: cacheName={}, key={}", cacheName, key, e);
            return null;
        }
    }

    /**
     * Check if a key exists in the cache.
     */
    public boolean exists(String cacheName, String key) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.warn("Failed to check cache exists: cacheName={}, key={}", cacheName, key, e);
            return false;
        }
    }

    /**
     * Get the remaining TTL of a key in seconds.
     */
    public long getTtl(String cacheName, String key) {
        String redisKey = buildRedisKey(cacheName, key);
        try {
            Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            log.warn("Failed to get TTL: cacheName={}, key={}", cacheName, key, e);
            return -2;
        }
    }

    /**
     * Build the full Redis key.
     */
    public static String buildRedisKey(String cacheName, String key) {
        return CACHE_KEY_PREFIX + cacheName + ":" + key;
    }
}
