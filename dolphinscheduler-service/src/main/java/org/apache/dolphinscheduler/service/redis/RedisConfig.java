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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching and session management.
 * Provides RedisTemplate, StringRedisTemplate, and CacheManager beans
 * with proper serialization configuration for distributed deployment.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Default time-to-live for cache entries: 30 minutes.
     */
    public static final long DEFAULT_CACHE_TTL_SECONDS = 1800L;

    /**
     * TTL for session cache: 10 minutes (matches session timeout).
     */
    private static final long SESSION_CACHE_TTL_SECONDS = 600L;

    /**
     * TTL for metadata cache: 5 minutes.
     */
    private static final long METADATA_CACHE_TTL_SECONDS = 300L;

    /**
     * TTL for project cache: 10 minutes (projects change infrequently).
     */
    private static final long PROJECT_CACHE_TTL_SECONDS = 600L;

    /**
     * TTL for alert plugin cache: 10 minutes.
     */
    private static final long ALERT_PLUGIN_CACHE_TTL_SECONDS = 600L;

    /**
     * TTL for data quality cache: 5 minutes.
     */
    private static final long DATA_QUALITY_CACHE_TTL_SECONDS = 300L;

    /**
     * Creates a RedisTemplate with Jackson2Json serialization support.
     * This template supports storing arbitrary objects as JSON in Redis.
     *
     * @param redisConnectionFactory the Redis connection factory
     * @return configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Configure Jackson2JsonRedisSerializer for value serialization
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = createObjectMapper();
        jacksonSerializer.setObjectMapper(objectMapper);

        // Use StringRedisSerializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates a StringRedisTemplate for simple string-based operations.
     *
     * @param redisConnectionFactory the Redis connection factory
     * @return configured StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * Creates a CacheManager backed by Redis with per-cache TTL configuration.
     * Supports cache names: "session", "metadata", "default".
     *
     * @param redisConnectionFactory the Redis connection factory
     * @return configured RedisCacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configure default cache settings
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
                .disableCachingNullValues();

        // Per-cache TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("session", defaultConfig.entryTtl(Duration.ofSeconds(SESSION_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("metadata", defaultConfig.entryTtl(Duration.ofSeconds(METADATA_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("tenant", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("queue", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("processDefinition", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("processTaskRelation", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("taskDefinition", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("workerGroup", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("schedule", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("project", defaultConfig.entryTtl(Duration.ofSeconds(PROJECT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("environment", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("datasource", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("cluster", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("k8sNamespace", defaultConfig.entryTtl(Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("alertPluginInstance", defaultConfig.entryTtl(Duration.ofSeconds(ALERT_PLUGIN_CACHE_TTL_SECONDS)));
        cacheConfigurations.put("dataQuality", defaultConfig.entryTtl(Duration.ofSeconds(DATA_QUALITY_CACHE_TTL_SECONDS)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Creates a configured ObjectMapper for JSON serialization/deserialization.
     *
     * @return configured ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
