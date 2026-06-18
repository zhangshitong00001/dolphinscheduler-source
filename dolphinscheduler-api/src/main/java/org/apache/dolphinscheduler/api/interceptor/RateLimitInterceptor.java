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

import org.apache.dolphinscheduler.api.configuration.TrafficConfiguration;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;

/**
 * 流量控制拦截器。同时支持全局级别的流量控制和租户级别的流量控制。
 * 当某个租户达到其请求配额时，该租户的请求将被快速拒绝；
 * 当系统全局请求数达到配额时，所有请求将被快速拒绝。
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private TrafficConfiguration trafficConfiguration;

    private RateLimiter globalRateLimiter;

    private LoadingCache<String, RateLimiter> tenantRateLimiterCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String token) {
                    // use tenant customize rate limit
                    Map<String, Integer> customizeTenantQpsRate = trafficConfiguration.getCustomizeTenantQpsRate();
                    int tenantQuota = trafficConfiguration.getDefaultTenantQpsRate();
                    if (MapUtils.isNotEmpty(customizeTenantQpsRate)) {
                        tenantQuota = customizeTenantQpsRate.getOrDefault(token, trafficConfiguration.getDefaultTenantQpsRate());
                    }
                    // use tenant default rate limit
                    return RateLimiter.create(tenantQuota, 1, TimeUnit.SECONDS);
                }
            });

    /**
     * 在请求处理前进行流量控制，依次检查租户级别和全局级别的速率限制。
     * 如果触发限流，直接设置HTTP 429状态码并拒绝请求。
     *
     * @param request  当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler  处理器对象
     * @return 是否放行，true表示未触发限流可以继续处理
     * @throws ExecutionException 获取租户级别RateLimiter时异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ExecutionException {
        // tenant-level rate limit
        if (trafficConfiguration.isTenantSwitch()) {
            String token = request.getHeader("token");
            if (!StringUtils.isEmpty(token)) {
                RateLimiter tenantRateLimiter = tenantRateLimiterCache.get(token);
                if (!tenantRateLimiter.tryAcquire()) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    logger.warn("Too many request, reach tenant rate limit, current tenant:{} qps is {}", token, tenantRateLimiter.getRate());
                    return false;
                }
            }
        }
        // global rate limit
        if (trafficConfiguration.isGlobalSwitch()) {
            if (!globalRateLimiter.tryAcquire()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                logger.warn("Too many request, reach global rate limit, current qps is {}", globalRateLimiter.getRate());
                return false;
            }
        }
        return true;
    }

    public RateLimitInterceptor(TrafficConfiguration trafficConfiguration) {
        this.trafficConfiguration = trafficConfiguration;
        if (trafficConfiguration.isGlobalSwitch()) {
            this.globalRateLimiter = RateLimiter.create(trafficConfiguration.getMaxGlobalQpsRate(), 1, TimeUnit.SECONDS);
        }
    }

}
