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

import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 流程实例映射 Mapper 接口，封装对 t_ds_process_instance_map 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，管理父子流程实例之间的关联映射关系（如子流程任务触发的子流程实例）。
 */
public interface ProcessInstanceMapMapper extends BaseMapper<ProcessInstanceMap> {

    /**
     * 根据父流程实例ID和父任务ID查询关联的子流程映射记录。
     * SELECT * FROM t_ds_process_instance_map WHERE parent_process_instance_id = #{parentProcessId} AND parent_task_instance_id = #{parentTaskId}
     *
     * @param parentProcessId 父流程实例ID
     * @param parentTaskId 父任务实例ID
     * @return 流程实例映射实体
     */
    ProcessInstanceMap queryByParentId(@Param("parentProcessId") int parentProcessId,
                                       @Param("parentTaskId") int parentTaskId);


    /**
     * 根据子流程实例ID反向查询其父流程映射记录。
     * SELECT * FROM t_ds_process_instance_map WHERE process_instance_id = #{subProcessId}
     *
     * @param subProcessId 子流程实例ID
     * @return 流程实例映射实体
     */
    ProcessInstanceMap queryBySubProcessId(@Param("subProcessId") Integer subProcessId);

    /**
     * 根据父流程实例ID删除所有关联的子流程映射记录。
     * DELETE FROM t_ds_process_instance_map WHERE parent_process_instance_id = #{parentProcessId}
     *
     * @param parentProcessId 父流程实例ID
     * @return 删除的记录数
     */
    int deleteByParentProcessId(@Param("parentProcessId") int parentProcessId);

    /**
     * 根据父流程实例ID查询所有子流程实例ID列表。
     * SELECT process_instance_id FROM t_ds_process_instance_map WHERE parent_process_instance_id = #{parentInstanceId}
     *
     * @param parentInstanceId 父流程实例ID
     * @return 子流程实例ID列表
     */
    List<Integer> querySubIdListByParentId(@Param("parentInstanceId") int parentInstanceId);

}
