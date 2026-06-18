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

import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 流程定义版本日志 Mapper 接口，封装对 t_ds_process_definition_log 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，存储流程定义的每个版本的快照记录，用于版本追溯和回滚。
 * 启用 Spring Cache 缓存，缓存名为 "processDefinition"，通过自定义 CacheKeyGenerator 生成缓存键。
 */
@CacheConfig(cacheNames = "processDefinition", keyGenerator = "cacheKeyGenerator")
public interface ProcessDefinitionLogMapper extends BaseMapper<ProcessDefinitionLog> {

    /**
     * 根据流程定义编码和版本号查询指定版本的流程定义快照。
     * SELECT * FROM t_ds_process_definition_log WHERE code = #{code} AND version = #{version}
     * 结果会被缓存（sync = true 保证缓存穿透时的同步加载）。
     *
     * @param code 流程定义编码
     * @param version 版本号
     * @return 该版本的流程定义日志实体，若不存在则返回 null
     */
    @Cacheable(sync = true)
    ProcessDefinitionLog queryByDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);

    /**
     * 根据项目编码和流程定义名称查询关联的所有版本日志。
     * SELECT * FROM t_ds_process_definition_log WHERE project_code = #{projectCode} AND name = #{name}
     *
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @return 流程定义版本日志列表
     */
    List<ProcessDefinitionLog> queryByDefinitionName(@Param("projectCode") long projectCode, @Param("name") String name);

    /**
     * 根据流程定义编码查询该流程的所有版本日志记录。
     * SELECT * FROM t_ds_process_definition_log WHERE code = #{code} ORDER BY version DESC
     *
     * @param code 流程定义编码
     * @return 流程定义版本日志列表（按版本降序排列）
     */
    List<ProcessDefinitionLog> queryByDefinitionCode(@Param("code") long code);

    /**
     * 查询指定流程定义的最大版本号。
     * SELECT MAX(version) FROM t_ds_process_definition_log WHERE code = #{code}
     *
     * @param code 流程定义编码
     * @return 最大版本号，若无记录则返回 null
     */
    Integer queryMaxVersionForDefinition(@Param("code") long code);

    /**
     * 查询指定流程定义的最大版本日志记录（最新版本快照）。
     * SELECT * FROM t_ds_process_definition_log WHERE code = #{code} AND version = (SELECT MAX(version) FROM ...)
     *
     * @param code 流程定义编码
     * @return 最新版本的流程定义日志实体
     */
    ProcessDefinitionLog queryMaxVersionDefinitionLog(@Param("code") long code);

    /**
     * 分页查询指定流程定义的所有版本列表。
     * SELECT * FROM t_ds_process_definition_log WHERE code = #{code} AND project_code = #{projectCode} ORDER BY version DESC
     *
     * @param page 分页对象
     * @param code 流程定义编码
     * @param projectCode 项目编码
     * @return 流程定义版本分页结果
     */
    IPage<ProcessDefinitionLog> queryProcessDefinitionVersionsPaging(Page<ProcessDefinitionLog> page, @Param("code") long code, @Param("projectCode") long projectCode);

    /**
     * 删除指定流程定义指定版本号的版本日志记录。
     * DELETE FROM t_ds_process_definition_log WHERE code = #{code} AND version = #{version}
     *
     * @param code 流程定义编码
     * @param version 版本号
     * @return 删除的记录数
     */
    int deleteByProcessDefinitionCodeAndVersion(@Param("code") long code, @Param("version") int version);
}
