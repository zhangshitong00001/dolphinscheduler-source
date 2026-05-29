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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Set;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * Environment mapper interface with Redis caching.
 */
@CacheConfig(cacheNames = "environment", keyGenerator = "cacheKeyGenerator")
public interface EnvironmentMapper extends BaseMapper<Environment> {

    @Cacheable(sync = true)
    Environment selectById(int id);

    @CacheEvict
    int deleteById(int id);

    @CacheEvict(key = "#p0.id")
    int updateById(Environment environment);

    Environment queryByEnvironmentName(@Param("environmentName") String name);
    Environment queryByEnvironmentCode(@Param("environmentCode") Long environmentCode);
    List<Environment> queryAllEnvironmentList();
    IPage<Environment> queryEnvironmentListPaging(IPage<Environment> page, @Param("searchName") String searchName);
    int deleteByCode(@Param("code") Long code);
    IPage<Environment> queryEnvironmentListPagingByIds(Page<Environment> page, @Param("ids")List<Integer> ids, @Param("searchName")String searchVal);
}
