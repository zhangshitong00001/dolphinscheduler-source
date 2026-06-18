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

import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 任务定义 Mapper 接口，封装对 t_ds_task_definition 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供任务定义的查询、分页、批量插入及删除等能力。
 */
public interface TaskDefinitionMapper extends BaseMapper<TaskDefinition> {

    /**
     * 根据项目编码、流程编码和任务名称精确查询任务定义。
     * SELECT * FROM t_ds_task_definition WHERE project_code = #{projectCode} AND process_code = #{processCode} AND name = #{name}
     *
     * @param projectCode 项目编码
     * @param processCode 流程编码
     * @param name 任务名称
     * @return 任务定义实体，若不存在则返回 null
     */
    TaskDefinition queryByName(@Param("projectCode") long projectCode,
                               @Param("processCode") long processCode,
                               @Param("name") String name);

    /**
     * 根据任务定义编码精确查询任务定义。
     * SELECT * FROM t_ds_task_definition WHERE code = #{code}
     *
     * @param code 任务定义编码
     * @return 任务定义实体，若不存在则返回 null
     */
    TaskDefinition queryByCode(@Param("code") long code);

    /**
     * 查询指定项目下所有任务定义列表。
     * SELECT * FROM t_ds_task_definition WHERE project_code = #{projectCode}
     *
     * @param projectCode 项目编码
     * @return 任务定义列表
     */
    List<TaskDefinition> queryAllDefinitionList(@Param("projectCode") long projectCode);

    /**
     * 按用户分组统计任务定义数量。
     * SELECT user_id, COUNT(*) FROM t_ds_task_definition WHERE project_code IN (...) GROUP BY user_id
     *
     * @param projectCodes 项目编码数组
     * @return 按用户分组的任务定义统计列表
     */
    List<DefinitionGroupByUser> countDefinitionGroupByUser(@Param("projectCodes") Long[] projectCodes);

    /**
     * 查询所有任务定义的资源ID列表，以Map形式返回，key为id。
     *
     * @return 资源ID Map列表
     */
    @MapKey("id")
    List<Map<String, Object>> listResources();

    /**
     * 根据用户ID查询该用户所拥有的任务定义资源ID列表。
     * SELECT id FROM t_ds_task_definition WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 资源ID Map列表
     */
    @MapKey("id")
    List<Map<String, Object>> listResourcesByUser(@Param("userId") Integer userId);

    /**
     * 根据任务定义编码删除任务定义。
     * DELETE FROM t_ds_task_definition WHERE code = #{code}
     *
     * @param code 任务定义编码
     * @return 受影响的行数
     */
    int deleteByCode(@Param("code") long code);

    /**
     * 批量插入任务定义日志记录。
     * INSERT INTO t_ds_task_definition (...) VALUES (...)
     *
     * @param taskDefinitions 任务定义日志列表
     * @return 插入的行数
     */
    int batchInsert(@Param("taskDefinitions") List<TaskDefinitionLog> taskDefinitions);

    /**
     * 分页查询任务定义主信息，支持按项目编码、流程名称、任务名称、任务类型及执行类型筛选。
     * SELECT * FROM t_ds_task_definition WHERE project_code = #{projectCode} AND ...
     *
     * @param page 分页对象
     * @param projectCode 项目编码
     * @param searchWorkflowName 流程名称搜索关键字
     * @param searchTaskName 任务名称搜索关键字
     * @param taskType 任务类型
     * @param taskExecuteType 任务执行类型
     * @return 任务主信息分页结果
     */
    IPage<TaskMainInfo> queryDefineListPaging(IPage<TaskMainInfo> page,
                                              @Param("projectCode") long projectCode,
                                              @Param("searchWorkflowName") String searchWorkflowName,
                                              @Param("searchTaskName") String searchTaskName,
                                              @Param("taskType") String taskType,
                                              @Param("taskExecuteType")TaskExecuteType taskExecuteType);

    /**
     * 根据任务定义编码列表批量查询任务定义。
     * SELECT * FROM t_ds_task_definition WHERE code IN (...)
     *
     * @param codes 任务定义编码集合
     * @return 任务定义列表
     */
    List<TaskDefinition> queryByCodeList(@Param("codes") Collection<Long> codes);

    /**
     * 根据任务编码列表批量删除任务定义。
     * DELETE FROM t_ds_task_definition WHERE code IN (...)
     *
     * @param taskCodeList 任务编码列表
     * @return 删除的行数
     */
    int deleteByBatchCodes(@Param("taskCodeList") List<Long> taskCodeList);
}
