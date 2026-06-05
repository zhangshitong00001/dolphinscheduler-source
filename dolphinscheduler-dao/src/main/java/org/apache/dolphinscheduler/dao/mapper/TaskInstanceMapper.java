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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 任务实例 Mapper 接口，封装对 t_ds_task_instance 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供任务实例的条件查询、状态统计、故障转移及分页等能力。
 */
public interface TaskInstanceMapper extends BaseMapper<TaskInstance> {

    /**
     * 根据流程实例ID和任务状态查询任务ID列表。
     * SELECT id FROM t_ds_task_instance WHERE process_instance_id = #{processInstanceId} AND state = #{state}
     *
     * @param processInstanceId 流程实例ID
     * @param state 任务状态
     * @return 任务ID列表
     */
    List<Integer> queryTaskByProcessIdAndState(@Param("processInstanceId") Integer processInstanceId,
                                               @Param("state") Integer state);

    /**
     * 根据流程实例ID和标记查询有效的任务实例列表。
     * SELECT * FROM t_ds_task_instance WHERE process_instance_id = #{processInstanceId} AND flag = #{flag}
     *
     * @param processInstanceId 流程实例ID
     * @param flag 标记（是否有效）
     * @return 有效的任务实例列表
     */
    List<TaskInstance> findValidTaskListByProcessId(@Param("processInstanceId") Integer processInstanceId,
                                                    @Param("flag") Flag flag);

    /**
     * 根据主机地址和任务状态数组查询任务实例列表。
     * SELECT * FROM t_ds_task_instance WHERE host = #{host} AND state IN (...)
     *
     * @param host 主机地址
     * @param stateArray 任务状态数组
     * @return 匹配的任务实例列表
     */
    List<TaskInstance> queryByHostAndStatus(@Param("host") String host,
                                            @Param("states") int[] stateArray);

    /**
     * 将指定主机上指定状态的故障任务实例切换为目标状态，实现故障转移。
     * UPDATE t_ds_task_instance SET state = #{destStatus} WHERE host = #{host} AND state IN (...)
     *
     * @param host 主机地址
     * @param stateArray 需要转移的任务状态数组
     * @param destStatus 目标状态
     * @return 受影响的行数
     */
    int setFailoverByHostAndStateArray(@Param("host") String host,
                                       @Param("states") int[] stateArray,
                                       @Param("destStatus") TaskExecutionStatus destStatus);

    /**
     * 根据流程实例ID和任务名称精确查询任务实例。
     * SELECT * FROM t_ds_task_instance WHERE process_instance_id = #{processInstanceId} AND name = #{name}
     *
     * @param processInstanceId 流程实例ID
     * @param name 任务名称
     * @return 任务实例实体，若不存在则返回 null
     */
    TaskInstance queryByInstanceIdAndName(@Param("processInstanceId") int processInstanceId,
                                          @Param("name") String name);

    /**
     * 根据流程实例ID和任务编码精确查询任务实例。
     * SELECT * FROM t_ds_task_instance WHERE process_instance_id = #{processInstanceId} AND task_code = #{taskCode}
     *
     * @param processInstanceId 流程实例ID
     * @param taskCode 任务编码
     * @return 任务实例实体，若不存在则返回 null
     */
    TaskInstance queryByInstanceIdAndCode(@Param("processInstanceId") int processInstanceId,
                                          @Param("taskCode") Long taskCode);

    /**
     * 根据流程实例ID列表和任务编码列表批量查询任务实例。
     * SELECT * FROM t_ds_task_instance WHERE process_instance_id IN (...) AND task_code IN (...)
     *
     * @param processInstanceIds 流程实例ID列表
     * @param taskCodes 任务编码列表
     * @return 匹配的任务实例列表
     */
    List<TaskInstance> queryByProcessInstanceIdsAndTaskCodes(@Param("processInstanceIds") List<Integer> processInstanceIds,
                                                  @Param("taskCodes") List<Long> taskCodes);

    /**
     * 统计指定项目编码和任务ID条件下的任务实例数量。
     * SELECT COUNT(*) FROM t_ds_task_instance WHERE project_code IN (...) AND task_code IN (...)
     *
     * @param projectCodes 项目编码数组
     * @param taskIds 任务ID数组
     * @return 任务实例数量
     */
    Integer countTask(@Param("projectCodes") Long[] projectCodes,
                      @Param("taskIds") int[] taskIds);

    /**
     * 按项目编码和开始时间统计各状态任务实例数量。
     * SELECT state, COUNT(*) FROM t_ds_task_instance WHERE start_time BETWEEN #{startTime} AND #{endTime} AND project_code IN (...) GROUP BY state
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param projectCodes 项目编码数组
     * @return 执行状态统计列表
     */
    List<ExecuteStatusCount> countTaskInstanceStateByProjectCodes(@Param("startTime") Date startTime,
                                                                  @Param("endTime") Date endTime,
                                                                  @Param("projectCodes") Long[] projectCodes);

    /**
     * 按项目编码、提交时间以及指定状态统计任务实例数量。
     * SELECT state, COUNT(*) FROM t_ds_task_instance WHERE submit_time BETWEEN #{startTime} AND #{endTime} AND project_code IN (...) AND state IN (...) GROUP BY state
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param projectCodes 项目编码数组
     * @param states 需要统计的任务状态列表
     * @return 执行状态统计列表
     */
    List<ExecuteStatusCount> countTaskInstanceStateByProjectCodesAndStatesBySubmitTime(@Param("startTime") Date startTime,
                                                                                       @Param("endTime") Date endTime,
                                                                                       @Param("projectCodes") Long[] projectCodes,
                                                                                       @Param("states") List<TaskExecutionStatus> states);

    /**
     * 分页查询任务实例列表，支持按项目编码、流程实例、搜索关键字、任务名称、执行者、状态数组、主机、执行类型和时间范围进行多条件筛选。
     * SELECT * FROM t_ds_task_instance WHERE project_code = #{projectCode} AND process_instance_id = #{processInstanceId} ...
     *
     * @param page 分页对象
     * @param projectCode 项目编码
     * @param processInstanceId 流程实例ID
     * @param processInstanceName 流程实例名称
     * @param searchVal 搜索关键字
     * @param taskName 任务名称
     * @param executorId 执行者ID
     * @param states 任务状态数组
     * @param host 主机地址
     * @param taskExecuteType 任务执行类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务实例分页结果
     */
    IPage<TaskInstance> queryTaskInstanceListPaging(IPage<TaskInstance> page,
                                                    @Param("projectCode") Long projectCode,
                                                    @Param("processInstanceId") Integer processInstanceId,
                                                    @Param("processInstanceName") String processInstanceName,
                                                    @Param("searchVal") String searchVal,
                                                    @Param("taskName") String taskName,
                                                    @Param("executorId") int executorId,
                                                    @Param("states") int[] statusArray,
                                                    @Param("host") String host,
                                                    @Param("taskExecuteType") TaskExecuteType taskExecuteType,
                                                    @Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime
    );

    /**
     * 分页查询流式任务实例列表，支持按项目编码、流程定义名称、搜索关键字、任务名称、执行者、状态数组、主机、执行类型和时间范围进行多条件筛选。
     * SELECT * FROM t_ds_task_instance WHERE project_code = #{projectCode} AND process_definition_name = #{processDefinitionName} ...
     *
     * @param page 分页对象
     * @param projectCode 项目编码
     * @param processDefinitionName 流程定义名称
     * @param searchVal 搜索关键字
     * @param taskName 任务名称
     * @param executorId 执行者ID
     * @param states 任务状态数组
     * @param host 主机地址
     * @param taskExecuteType 任务执行类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 流式任务实例分页结果
     */
    IPage<TaskInstance> queryStreamTaskInstanceListPaging(IPage<TaskInstance> page,
                                                    @Param("projectCode") Long projectCode,
                                                    @Param("processDefinitionName") String processDefinitionName,
                                                    @Param("searchVal") String searchVal,
                                                    @Param("taskName") String taskName,
                                                    @Param("executorId") int executorId,
                                                    @Param("states") int[] statusArray,
                                                    @Param("host") String host,
                                                    @Param("taskExecuteType") TaskExecuteType taskExecuteType,
                                                    @Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime);

    /**
     * 查询指定流程实例下所有指定状态且未释放的任务实例（逻辑未删除）。
     * SELECT * FROM t_ds_task_instance WHERE process_instance_id = #{processInstanceId} AND state = #{status} AND flag = 1
     *
     * @param processInstanceId 流程实例ID
     * @param status 任务状态
     * @return 未释放的任务实例列表
     */
    List<TaskInstance> loadAllInfosNoRelease(@Param("processInstanceId") int processInstanceId,
                                             @Param("status") int status);
}
