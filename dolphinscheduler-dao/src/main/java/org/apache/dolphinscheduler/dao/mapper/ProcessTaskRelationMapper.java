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

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 流程任务关系 Mapper 接口，封装对 t_ds_process_task_relation 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供流程任务关系的增删改查、上下游查询及批量更新能力，支持 Spring Cache 缓存。
 */
@CacheConfig(cacheNames = "processTaskRelation", keyGenerator = "cacheKeyGenerator")
public interface ProcessTaskRelationMapper extends BaseMapper<ProcessTaskRelation> {

    /**
     * 根据项目编码和流程定义编码查询流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE project_code = #{projectCode} AND process_definition_code = #{processCode}
     * 结果不为空时会被缓存。
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return ProcessTaskRelation list
     */
    @Cacheable(unless = "#result == null || #result.size() == 0")
    List<ProcessTaskRelation> queryByProcessCode(@Param("projectCode") long projectCode,
                                                 @Param("processCode") long processCode);

    /**
     * 根据主键ID更新流程任务关系，同时清除对应的缓存。
     * SQL: UPDATE t_ds_process_task_relation SET ... WHERE id = #{et.id}
     * 缓存清除键为 projectCode + '_' + processDefinitionCode。
     *
     * @param processTaskRelation processTaskRelation
     * @return 更新的记录数
     */
    @CacheEvict(key = "#p0.projectCode + '_' + #p0.processDefinitionCode")
    int updateById(@Param("et") ProcessTaskRelation processTaskRelation);

    /**
     * 根据项目编码和流程定义编码删除流程任务关系，同时清除对应的缓存。
     * SQL: DELETE FROM t_ds_process_task_relation WHERE project_code = #{projectCode} AND process_definition_code = #{processCode}
     *
     * @param projectCode projectCode
     * @param processCode processCode
     * @return 删除的记录数
     */
    @CacheEvict
    int deleteByCode(@Param("projectCode") long projectCode, @Param("processCode") long processCode);

    /**
     * 根据任务编码数组查询流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE pre_task_code IN #{taskCodes} OR post_task_code IN #{taskCodes}
     *
     * @param taskCodes taskCode list
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryByTaskCodes(@Param("taskCodes") Long[] taskCodes);

    /**
     * 根据单个任务编码查询流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE pre_task_code = #{taskCode} OR post_task_code = #{taskCode}
     *
     * @param taskCode taskCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryByTaskCode(@Param("taskCode") long taskCode);

    /**
     * 批量插入流程任务关系记录。
     * SQL: INSERT INTO t_ds_process_task_relation (...) VALUES (...), (...), ...
     *
     * @param taskRelationList taskRelationList
     * @return 插入的记录数
     */
    int batchInsert(@Param("taskRelationList") List<ProcessTaskRelationLog> taskRelationList);

    /**
     * 根据任务编码查询下游流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE pre_task_code = #{taskCode}
     *
     * @param taskCode taskCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryDownstreamByTaskCode(@Param("taskCode") long taskCode);

    /**
     * 根据项目编码和任务编码查询上游流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE project_code = #{projectCode} AND post_task_code = #{taskCode}
     *
     * @param projectCode projectCode
     * @param taskCode    taskCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryUpstreamByCode(@Param("projectCode") long projectCode, @Param("taskCode") long taskCode);

    /**
     * 根据项目编码和任务编码查询下游流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE project_code = #{projectCode} AND pre_task_code = #{taskCode}
     *
     * @param projectCode projectCode
     * @param taskCode    taskCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryDownstreamByCode(@Param("projectCode") long projectCode, @Param("taskCode") long taskCode);

    /**
     * 根据项目编码、任务编码和前置任务编码数组查询上游流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE project_code = #{projectCode}
     *      AND post_task_code = #{taskCode} AND pre_task_code IN #{preTaskCodes}
     *
     * @param projectCode  projectCode
     * @param taskCode     taskCode
     * @param preTaskCodes preTaskCode list
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryUpstreamByCodes(@Param("projectCode") long projectCode, @Param("taskCode") long taskCode, @Param("preTaskCodes") Long[] preTaskCodes);

    /**
     * 根据流程定义编码和版本号查询流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE process_definition_code = #{processDefinitionCode}
     *      AND process_definition_version = #{processDefinitionVersion}
     *
     * @param processDefinitionCode    process definition code
     * @param processDefinitionVersion process definition version
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryProcessTaskRelationsByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode,
                                                                               @Param("processDefinitionVersion") Integer processDefinitionVersion);

    /**
     * 统计各流程定义中指定任务的上游依赖数量（按流程定义编码分组）。
     * SQL: SELECT process_definition_code, COUNT(*) AS count FROM t_ds_process_task_relation
     *      WHERE project_code = #{projectCode} AND post_task_code = #{taskCode}
     *      AND process_definition_code IN #{processDefinitionCodes} GROUP BY process_definition_code
     *
     * @param projectCode              projectCode
     * @param processDefinitionCodes   processDefinitionCodes
     * @param taskCode                 taskCode
     * @return upstream count list group by process definition code
     */
    List<Map<String, Long>> countUpstreamByCodeGroupByProcessDefinitionCode(@Param("projectCode") long projectCode,
                                                                            @Param("processDefinitionCodes") Long[] processDefinitionCodes,
                                                                            @Param("taskCode") long taskCode);

    /**
     * 批量更新流程任务关系的前置任务信息。
     * SQL: UPDATE t_ds_process_task_relation SET pre_task_code = #{item.preTaskCode}, pre_task_version = #{item.preTaskVersion}
     *      WHERE id = #{item.id}
     *
     * @param processTaskRelationList process task relation list
     * @return 更新的记录数
     */
    int batchUpdateProcessTaskRelationPreTask(@Param("processTaskRelationList") List<ProcessTaskRelation> processTaskRelationList);

    /**
     * 根据项目编码、流程定义编码、前置任务编码和后置任务编码查询流程任务关系。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE project_code = #{projectCode}
     *      AND process_definition_code = #{processDefinitionCode} AND pre_task_code = #{preTaskCode}
     *      AND post_task_code = #{postTaskCode}
     *
     * @param projectCode          projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode          preTaskCode
     * @param postTaskCode         postTaskCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryByCode(@Param("projectCode") long projectCode,
                                          @Param("processDefinitionCode") long processDefinitionCode,
                                          @Param("preTaskCode") long preTaskCode,
                                          @Param("postTaskCode") long postTaskCode);

    /**
     * 根据流程任务关系日志实体删除对应的流程任务关系记录。
     * SQL: DELETE FROM t_ds_process_task_relation WHERE pre_task_code = #{processTaskRelationLog.preTaskCode}
     *      AND post_task_code = #{processTaskRelationLog.postTaskCode}
     *      AND process_definition_code = #{processTaskRelationLog.processDefinitionCode}
     *      AND process_definition_version = #{processTaskRelationLog.processDefinitionVersion}
     *
     * @param processTaskRelationLog processTaskRelationLog
     * @return 删除的记录数
     */
    int deleteRelation(@Param("processTaskRelationLog") ProcessTaskRelationLog processTaskRelationLog);

    /**
     * 统计满足条件的流程任务关系数量。
     * SQL: SELECT COUNT(*) FROM t_ds_process_task_relation WHERE project_code = #{projectCode}
     *      AND process_definition_code = #{processDefinitionCode} AND pre_task_code = #{preTaskCode}
     *      AND post_task_code = #{postTaskCode}
     *
     * @param projectCode          projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param preTaskCode          preTaskCode
     * @param postTaskCode         postTaskCode
     * @return 满足条件的记录数
     */
    int countByCode(@Param("projectCode") long projectCode,
                    @Param("processDefinitionCode") long processDefinitionCode,
                    @Param("preTaskCode") long preTaskCode,
                    @Param("postTaskCode") long postTaskCode);

    /**
     * 根据流程定义编码查询下游流程任务关系列表。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE process_definition_code = #{processDefinitionCode}
     *      AND post_task_code IS NOT NULL（下游关系）
     *
     * @param processDefinitionCode processDefinitionCode
     * @return ProcessTaskRelation list
     */
    List<ProcessTaskRelation> queryDownstreamByProcessDefinitionCode(@Param("processDefinitionCode") long processDefinitionCode);

    /**
     * 分页筛选查询流程任务关系列表，支持按流程任务关系属性进行过滤。
     * SQL: SELECT * FROM t_ds_process_task_relation WHERE ...（动态过滤条件）
     *
     * @param page                page
     * @param processTaskRelation process task relation object（过滤条件）
     * @return process task relation IPage
     */
    IPage<ProcessTaskRelation> filterProcessTaskRelation(IPage<ProcessTaskRelation> page,
                                                         @Param("relation") ProcessTaskRelation processTaskRelation);

    /**
     * 批量更新流程任务关系中的任务版本号。
     * SQL: UPDATE t_ds_process_task_relation SET pre_task_version = #{processTaskRelation.preTaskVersion},
     *      post_task_version = #{processTaskRelation.postTaskVersion} WHERE ...
     *
     * @param processTaskRelationList process task relation list
     * @return 更新的记录数
     */
    int updateProcessTaskRelationTaskVersion(@Param("processTaskRelation") ProcessTaskRelation processTaskRelationList);
}
