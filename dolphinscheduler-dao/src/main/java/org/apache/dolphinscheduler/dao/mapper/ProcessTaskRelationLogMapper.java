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

import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 流程任务关系日志 Mapper 接口，封装对 t_ds_process_task_relation_log 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供流程任务关系版本日志的查询、批量插入和删除能力。
 */
public interface ProcessTaskRelationLogMapper extends BaseMapper<ProcessTaskRelationLog> {

    /**
     * 根据流程定义编码和版本号查询流程任务关系日志列表。
     * SQL: SELECT * FROM t_ds_process_task_relation_log WHERE process_definition_code = #{processCode} AND process_definition_version = #{processVersion}
     *
     * @param processCode    process definition code
     * @param processVersion process version
     * @return process task relation log list
     */
    List<ProcessTaskRelationLog> queryByProcessCodeAndVersion(@Param("processCode") long processCode,
                                                              @Param("processVersion") int processVersion);

    /**
     * 批量插入流程任务关系日志记录。
     * SQL: INSERT INTO t_ds_process_task_relation_log (...) VALUES (...), (...), ...
     *
     * @param taskRelationList taskRelationList
     * @return 插入的记录数
     */
    int batchInsert(@Param("taskRelationList") List<ProcessTaskRelationLog> taskRelationList);

    /**
     * 根据流程定义编码和版本号删除流程任务关系日志记录。
     * SQL: DELETE FROM t_ds_process_task_relation_log WHERE process_definition_code = #{processCode} AND process_definition_version = #{processVersion}
     *
     * @param processCode    process definition code
     * @param processVersion process version
     * @return 删除的记录数
     */
    int deleteByCode(@Param("processCode") long processCode,
                     @Param("processVersion") int processVersion);

    /**
     * 根据流程任务关系日志实体删除对应的记录。
     * SQL: DELETE FROM t_ds_process_task_relation_log WHERE pre_task_code = #{processTaskRelationLog.preTaskCode}
     *      AND post_task_code = #{processTaskRelationLog.postTaskCode}
     *      AND process_definition_code = #{processTaskRelationLog.processDefinitionCode}
     *      AND process_definition_version = #{processTaskRelationLog.processDefinitionVersion}
     *
     * @param processTaskRelationLog processTaskRelationLog
     * @return 删除的记录数
     */
    int deleteRelation(@Param("processTaskRelationLog") ProcessTaskRelationLog processTaskRelationLog);

    /**
     * 根据流程任务关系实体查询对应的日志记录，用于比较关系是否发生变化。
     * SQL: SELECT * FROM t_ds_process_task_relation_log WHERE pre_task_code = #{processTaskRelation.preTaskCode}
     *      AND post_task_code = #{processTaskRelation.postTaskCode}
     *      AND process_definition_code = #{processTaskRelation.processDefinitionCode}
     *      AND process_definition_version = #{processTaskRelation.processDefinitionVersion}
     *
     * @param processTaskRelation processTaskRelation
     * @return process task relation log
     */
    ProcessTaskRelationLog queryRelationLogByRelation(@Param("processTaskRelation") ProcessTaskRelation processTaskRelation);
}
