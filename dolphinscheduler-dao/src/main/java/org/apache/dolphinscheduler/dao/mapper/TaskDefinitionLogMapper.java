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

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 任务定义日志 Mapper 接口，封装对 t_ds_task_definition_log 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供任务定义版本日志的增删改查、版本管理及分页查询能力，支持 Spring Cache 缓存。
 */
@CacheConfig(cacheNames = "taskDefinition", keyGenerator = "cacheKeyGenerator")
public interface TaskDefinitionLogMapper extends BaseMapper<TaskDefinitionLog> {

    /**
     * 根据任务定义编码和版本号查询任务定义日志记录。
     * SQL: SELECT * FROM t_ds_task_definition_log WHERE code = #{code} AND version = #{version}
     * 查询结果会被缓存，支持同步缓存以避免缓存击穿。
     *
     * @param code    taskDefinitionCode
     * @param version version
     * @return task definition log 任务定义日志实体，未找到时返回 null
     */
    @Cacheable(sync = true)
    TaskDefinitionLog queryByDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * 根据主键ID更新任务定义日志，同时清除对应的缓存。
     * SQL: UPDATE t_ds_task_definition_log SET ... WHERE id = #{et.id}
     * 缓存清除键为 code + '_' + version。
     *
     * @param taskDefinitionLog taskDefinitionLog
     * @return 更新的记录数
     */
    @CacheEvict(key = "#p0.code + '_' + #p0.version")
    int updateById(@Param("et") TaskDefinitionLog taskDefinitionLog);

    /**
     * 根据任务定义编码和版本号删除指定的任务定义日志版本记录，同时清除缓存。
     * SQL: DELETE FROM t_ds_task_definition_log WHERE code = #{code} AND version = #{version}
     *
     * @param code    task definition code
     * @param version task definition version
     * @return 删除的记录数
     */
    @CacheEvict
    int deleteByCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * 查询指定任务定义的最大版本号，用于版本管理（新建版本时自增）。
     * SQL: SELECT MAX(version) FROM t_ds_task_definition_log WHERE code = #{code}
     *
     * @param code taskDefinitionCode
     * @return 最大版本号，无记录时返回 null
     */
    Integer queryMaxVersionForDefinition(@Param("code") long code);

    /**
     * 根据任务定义实体集合查询对应的任务定义日志记录。
     * SQL: SELECT * FROM t_ds_task_definition_log WHERE (code, version) IN ((...), (...), ...)
     *
     * @param taskDefinitions taskDefinition list
     * @return task definition log list
     */
    List<TaskDefinitionLog> queryByTaskDefinitions(@Param("taskDefinitions") Collection<TaskDefinition> taskDefinitions);

    /**
     * 批量插入任务定义日志记录。
     * SQL: INSERT INTO t_ds_task_definition_log (...) VALUES (...), (...), ...
     *
     * @param taskDefinitionLogs taskDefinitionLogs
     * @return 插入的记录数
     */
    int batchInsert(@Param("taskDefinitionLogs") List<TaskDefinitionLog> taskDefinitionLogs);

    /**
     * 分页查询指定任务定义的版本历史列表。
     * SQL: SELECT * FROM t_ds_task_definition_log WHERE code = #{code} AND project_code = #{projectCode}
     *      ORDER BY version DESC
     *
     * @param page        pagination info
     * @param code        process definition code
     * @param projectCode project code
     * @return the paging task definition version list
     */
    IPage<TaskDefinitionLog> queryTaskDefinitionVersionsPaging(Page<TaskDefinitionLog> page, @Param("code") long code, @Param("projectCode") long projectCode);
}
