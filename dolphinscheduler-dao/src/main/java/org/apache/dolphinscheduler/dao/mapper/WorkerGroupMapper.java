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

import static org.apache.dolphinscheduler.common.constants.Constants.CACHE_KEY_VALUE_ALL;

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Worker 分组 Mapper 接口，封装对 t_ds_worker_group 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供 Worker 分组的全量查询、按名称精确查询及缓存管理等能力。
 * 查询结果使用 Spring Cache 缓存，缓存名称为 "workerGroup"。
 */
@CacheConfig(cacheNames = "workerGroup", keyGenerator = "cacheKeyGenerator")
public interface WorkerGroupMapper extends BaseMapper<WorkerGroup> {

    /**
     * 查询所有 Worker 分组列表，结果会被缓存以提高性能。
     * SELECT * FROM t_ds_worker_group
     *
     * @return 所有 Worker 分组列表
     */
    @Cacheable(sync = true, key = CACHE_KEY_VALUE_ALL)
    List<WorkerGroup> queryAllWorkerGroup();

    /**
     * 根据主键ID删除 Worker 分组，同时清除全部缓存。
     * DELETE FROM t_ds_worker_group WHERE id = #{id}
     *
     * @param id Worker 分组ID
     * @return 受影响的行数
     */
    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int deleteById(Integer id);

    /**
     * 新增 Worker 分组记录，同时清除全部缓存。
     * INSERT INTO t_ds_worker_group (...) VALUES (...)
     *
     * @param entity Worker 分组实体
     * @return 受影响的行数
     */
    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int insert(WorkerGroup entity);

    /**
     * 根据 Worker 分组实体更新信息，同时清除全部缓存。
     * UPDATE t_ds_worker_group SET ... WHERE id = #{et.id}
     *
     * @param entity 包含更新信息的 Worker 分组实体
     * @return 受影响的行数
     */
    @CacheEvict(key = CACHE_KEY_VALUE_ALL)
    int updateById(@Param("et") WorkerGroup entity);

    /**
     * 根据名称模糊查询 Worker 分组列表。
     * SELECT * FROM t_ds_worker_group WHERE name LIKE CONCAT('%', #{name}, '%')
     *
     * @param name Worker 分组名称
     * @return 匹配的 Worker 分组列表
     */
    List<WorkerGroup> queryWorkerGroupByName(@Param("name") String name);

}
