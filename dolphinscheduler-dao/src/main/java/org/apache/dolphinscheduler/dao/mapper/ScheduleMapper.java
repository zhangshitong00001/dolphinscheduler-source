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

import org.apache.dolphinscheduler.dao.entity.Schedule;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 调度 Mapper 接口，封装对 t_ds_schedules 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供调度配置的增删改查、按流程定义查询及分页能力，支持 Spring Cache 缓存。
 */
@CacheConfig(cacheNames = "schedule", keyGenerator = "cacheKeyGenerator")
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 插入调度配置，同时清除对应流程定义的缓存。
     * SQL: INSERT INTO t_ds_schedules (...) VALUES (...)
     * 缓存清除键为 processDefinitionCode。
     *
     * @param entity schedule entity
     * @return 插入的记录数
     */
    @CacheEvict(key = "#p0.processDefinitionCode")
    int insert(Schedule entity);

    /**
     * 根据主键ID更新调度配置，同时清除对应流程定义的缓存。
     * SQL: UPDATE t_ds_schedules SET ... WHERE id = #{et.id}
     * 缓存清除键为 processDefinitionCode。
     *
     * @param entity schedule entity
     * @return 更新的记录数
     */
    @CacheEvict(key = "#p0.processDefinitionCode")
    int updateById(@Param("et") Schedule entity);

    /**
     * 根据流程定义编码查询已发布的调度配置列表。
     * SQL: SELECT * FROM t_ds_schedules WHERE process_definition_code = #{processDefinitionCode} AND release_state = 1
     * 查询结果会被缓存，支持同步缓存以避免缓存击穿。
     *
     * @param processDefinitionCode processDefinitionCode
     * @return schedule list
     */
    @Cacheable(sync = true)
    List<Schedule> queryReleaseSchedulerListByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    /**
     * 根据流程定义编码分页查询调度配置列表，支持按名称模糊搜索。
     * SQL: SELECT * FROM t_ds_schedules WHERE process_definition_code = #{processDefinitionCode}
     *      AND (name LIKE #{searchVal} OR ...) ORDER BY update_time DESC
     *
     * @param page                  page
     * @param processDefinitionCode processDefinitionCode
     * @param searchVal             searchVal
     * @return scheduler IPage 分页结果
     */
    IPage<Schedule> queryByProcessDefineCodePaging(IPage<Schedule> page,
                                                   @Param("processDefinitionCode") long processDefinitionCode,
                                                   @Param("searchVal") String searchVal);

    /**
     * 根据项目名称查询调度配置列表。
     * SQL: SELECT s.* FROM t_ds_schedules s INNER JOIN t_ds_project p ON s.project_code = p.code WHERE p.name = #{projectName}
     *
     * @param projectName projectName
     * @return schedule list
     */
    List<Schedule> querySchedulerListByProjectName(@Param("projectName") String projectName);

    /**
     * 根据流程定义编码数组批量查询调度配置列表。
     * SQL: SELECT * FROM t_ds_schedules WHERE process_definition_code IN #{processDefineCodes}
     *
     * @param processDefineCodes processDefineCodes
     * @return schedule list
     */
    List<Schedule> selectAllByProcessDefineArray(@Param("processDefineCodes") long[] processDefineCodes);

    /**
     * 根据流程定义编码查询单个调度配置。
     * SQL: SELECT * FROM t_ds_schedules WHERE process_definition_code = #{processDefinitionCode} LIMIT 1
     *
     * @param processDefinitionCode processDefinitionCode
     * @return schedule 调度实体，未找到时返回 null
     */
    Schedule queryByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    /**
     * 根据流程定义编码列表批量查询调度配置。
     * SQL: SELECT * FROM t_ds_schedules WHERE process_definition_code IN #{processDefinitionCodeList}
     *
     * @param processDefinitionCodeList processDefinitionCodeList
     * @return schedule list
     */
    List<Schedule> querySchedulesByProcessDefinitionCodes(@Param("processDefinitionCodeList") List<Long> processDefinitionCodeList);
}
