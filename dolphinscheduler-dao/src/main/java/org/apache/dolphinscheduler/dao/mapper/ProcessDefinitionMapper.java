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

import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.DependentSimplifyDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 流程定义 Mapper 接口，封装对 t_ds_process_definition 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供流程定义的查询、分页、统计、缓存管理等功能。
 * 启用 Spring Cache 缓存（cacheNames = "processDefinition"），通过自定义 CacheKeyGenerator 生成缓存键。
 */
@CacheConfig(cacheNames = "processDefinition", keyGenerator = "cacheKeyGenerator")
public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinition> {

    /**
     * 根据流程定义编码（唯一标识）查询流程定义。
     * SELECT * FROM t_ds_process_definition WHERE code = #{code}
     * 结果会被缓存（sync = true 保证缓存穿透时的同步加载）。
     *
     * @param code 流程定义编码
     * @return 流程定义实体，若不存在则返回 null
     */
    @Cacheable(sync = true)
    ProcessDefinition queryByCode(@Param("code") long code);

    /**
     * 根据主键ID更新流程定义（MyBatis-Plus 内置方法覆写）。
     * 更新成功后清除对应 code 的缓存（key = "#p0.code"）。
     *
     * @param processDefinition 流程定义实体
     * @return 更新的记录数
     */
    @CacheEvict(key = "#p0.code")
    int updateById(@Param("et") ProcessDefinition processDefinition);

    /**
     * 根据流程定义编码删除流程定义记录。
     * DELETE FROM t_ds_process_definition WHERE code = #{code}
     * 删除后清除对应缓存。
     *
     * @param code 流程定义编码
     * @return 删除的记录数
     */
    @CacheEvict
    int deleteByCode(@Param("code") long code);

    /**
     * 根据流程定义编码列表批量查询流程定义。
     * SELECT * FROM t_ds_process_definition WHERE code IN (#{codes})
     *
     * @param codes 流程定义编码集合
     * @return 流程定义列表
     */
    List<ProcessDefinition> queryByCodes(@Param("codes") Collection<Long> codes);

    /**
     * 校验指定项目下流程定义名称是否已存在（与 queryByDefineName 不同，此方法会排除已删除的记录）。
     * SELECT * FROM t_ds_process_definition WHERE project_code = #{projectCode} AND name = #{processDefinitionName} AND flag != DELETE
     *
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @return 流程定义实体，若不存在则返回 null
     */
    ProcessDefinition verifyByDefineName(@Param("projectCode") long projectCode,
                                         @Param("processDefinitionName") String name);

    /**
     * 根据项目编码和流程定义名称查询流程定义。
     * SELECT * FROM t_ds_process_definition WHERE project_code = #{projectCode} AND name = #{processDefinitionName}
     *
     * @param projectCode 项目编码
     * @param name 流程定义名称
     * @return 流程定义实体，若不存在则返回 null
     */
    ProcessDefinition queryByDefineName(@Param("projectCode") long projectCode,
                                        @Param("processDefinitionName") String name);

    /**
     * 根据主键ID查询流程定义。
     * SELECT * FROM t_ds_process_definition WHERE id = #{processDefineId}
     *
     * @param processDefineId 流程定义主键ID
     * @return 流程定义实体，若不存在则返回 null
     */
    ProcessDefinition queryByDefineId(@Param("processDefineId") int processDefineId);

    /**
     * 分页查询流程定义列表，支持按搜索值、用户ID和项目编码过滤。
     * 搜索值用于 LIKE 模糊匹配流程名称，用户ID用于权限过滤。
     *
     * @param page 分页对象
     * @param searchVal 搜索关键字，用于模糊匹配流程名称
     * @param userId 用户ID，用于权限过滤
     * @param projectCode 项目编码
     * @return 流程定义分页结果
     */
    IPage<ProcessDefinition> queryDefineListPaging(IPage<ProcessDefinition> page,
                                                   @Param("searchVal") String searchVal,
                                                   @Param("userId") int userId,
                                                   @Param("projectCode") long projectCode);

    /**
     * 查询指定项目下的所有流程定义列表。
     * SELECT * FROM t_ds_process_definition WHERE project_code = #{projectCode}
     *
     * @param projectCode 项目编码
     * @return 流程定义列表
     */
    List<ProcessDefinition> queryAllDefinitionList(@Param("projectCode") long projectCode);

    /**
     * 根据项目编码和流程定义编码列表查询简化版流程定义（用于依赖检测等场景）。
     * 返回 DependentSimplifyDefinition，仅包含依赖检测所需的关键字段。
     *
     * @param projectCode 项目编码
     * @param codes 流程定义编码集合
     * @return 简化版流程定义列表
     */
    List<DependentSimplifyDefinition> queryDefinitionListByProjectCodeAndProcessDefinitionCodes(@Param("projectCode") long projectCode,
                                                                                                @Param("codes") Collection<Long> codes);

    /**
     * 根据主键ID数组批量查询流程定义。
     * SELECT * FROM t_ds_process_definition WHERE id IN (#{ids})
     *
     * @param ids 主键ID数组
     * @return 流程定义列表
     */
    List<ProcessDefinition> queryDefinitionListByIdList(@Param("ids") Integer[] ids);

    /**
     * 查询指定租户下的所有流程定义列表。
     * SELECT * FROM t_ds_process_definition WHERE tenant_id = #{tenantId}
     *
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    List<ProcessDefinition> queryDefinitionListByTenant(@Param("tenantId") int tenantId);

    /**
     * 按项目编码数组统计每个项目下的流程定义数量，并附上创建者用户ID分组。
     * SELECT user_id, COUNT(*) FROM t_ds_process_definition WHERE project_code IN (#{projectCodes}) GROUP BY project_code, user_id
     *
     * @param projectCodes 项目编码数组
     * @return 按用户分组的流程定义统计结果列表
     */
    List<DefinitionGroupByUser> countDefinitionByProjectCodes(@Param("projectCodes") Long[] projectCodes);

    /**
     * 列出所有流程定义中的资源ID（用于资源引用关系建立）。
     * 查询 flow_relation 字段中引用的资源ID。
     *
     * @return 以 id 为 key 的资源Map列表
     */
    @MapKey("id")
    List<Map<String, Object>> listResources();

    /**
     * 列出指定用户创建的流程定义中的资源ID。
     *
     * @param userId 用户ID
     * @return 以 id 为 key 的资源Map列表
     */
    @MapKey("id")
    List<Map<String, Object>> listResourcesByUser(@Param("userId") Integer userId);

    /**
     * 列出所有不重复的项目ID。
     * SELECT DISTINCT project_code FROM t_ds_process_definition
     *
     * @return 项目ID列表
     */
    List<Integer> listProjectIds();
}
