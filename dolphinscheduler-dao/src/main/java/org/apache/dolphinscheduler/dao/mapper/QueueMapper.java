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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.Queue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * Queue mapper interface with Redis caching support.
 * <p>
 * Cache configuration:
 * - Cache name: "queue"
 * - TTL: 30 minutes (configured in RedisConfig)
 * - Key generation: custom CacheKeyGenerator (params joined by underscore)
 * - Cache is evicted on update/delete operations
 */
@CacheConfig(cacheNames = "queue", keyGenerator = "cacheKeyGenerator")
public interface QueueMapper extends BaseMapper<Queue> {

    /**
     * Query queue by ID with caching.
     * Result is cached in Redis for 30 minutes.
     */
    @Cacheable(sync = true)
    Queue selectById(int id);

    /**
     * Delete queue by ID, evicts cache.
     */
    @CacheEvict
    int deleteById(int id);

    /**
     * Update queue, evicts cache for this entry.
     */
    @CacheEvict(key = "#p0.id")
    int updateById(Queue queue);

    /**
     * Queue pagination query.
     */
    IPage<Queue> queryQueuePaging(IPage<Queue> page, @Param("ids")List<Integer> ids,
                                  @Param("searchVal") String searchVal);

    /**
     * Query all queue list.
     */
    List<Queue> queryAllQueueList(@Param("queue") String queue,
                             @Param("queueName") String queueName);

    /**
     * Check if queue exists.
     */
    Boolean existQueue(@Param("queue") String queue, @Param("queueName") String queueName);

    /**
     * Query queue by name.
     */
    Queue queryQueueName(@Param("queue") String queue, @Param("queueName") String queueName);
}
