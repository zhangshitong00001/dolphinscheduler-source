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

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 流程实例 Mapper 接口，封装对 t_ds_process_instance 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供流程实例的分页查询、状态统计、故障转移及全局参数更新等能力。
 */
public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {

    /**
     * 根据流程实例ID查询详细信息。
     * SQL: SELECT * FROM t_ds_process_instance WHERE id = #{processId}
     *
     * @param processId processId
     * @return process instance
     */
    ProcessInstance queryDetailById(@Param("processId") int processId);

    /**
     * 根据主机名和状态数组查询流程实例列表，用于指定主机上的状态过滤查询。
     * SQL: SELECT * FROM t_ds_process_instance WHERE host = #{host} AND state IN #{states}
     *
     * @param host       host
     * @param stateArray stateArray
     * @return process instance list
     */
    List<ProcessInstance> queryByHostAndStatus(@Param("host") String host,
                                               @Param("states") int[] stateArray);

    /**
     * 查询需要故障转移的流程实例所在的主机列表。
     * SQL: SELECT DISTINCT host FROM t_ds_process_instance WHERE state IN #{states}
     *
     * @param stateArray stateArray
     * @return 需要故障转移的主机名列表
     */
    List<String> queryNeedFailoverProcessInstanceHost(@Param("states") int[] stateArray);

    /**
     * 根据租户ID和状态数组查询流程实例列表。
     * SQL: SELECT * FROM t_ds_process_instance WHERE tenant_id = #{tenantId} AND state IN #{states}
     *
     * @param tenantId tenantId
     * @param states   states array
     * @return process instance list
     */
    List<ProcessInstance> queryByTenantIdAndStatus(@Param("tenantId") int tenantId,
                                                   @Param("states") int[] states);

    /**
     * 根据Worker分组名和状态数组查询流程实例列表。
     * SQL: SELECT * FROM t_ds_process_instance WHERE worker_group = #{workerGroupName} AND state IN #{states}
     *
     * @param workerGroupName workerGroupName
     * @param states          states array
     * @return process instance list
     */
    List<ProcessInstance> queryByWorkerGroupNameAndStatus(@Param("workerGroupName") String workerGroupName,
                                                          @Param("states") int[] states);

    /**
     * 分页查询流程实例列表，支持按项目、流程定义、执行人、状态、主机和时间范围等多条件过滤。
     * SQL: SELECT * FROM t_ds_process_instance WHERE project_code = #{projectCode}
     *      AND process_definition_code = #{processDefinitionCode} AND name LIKE #{searchVal}
     *      AND executor_id = #{executorId} AND state IN #{statusArray} AND host = #{host}
     *      AND start_time BETWEEN #{startTime} AND #{endTime} ORDER BY start_time DESC
     *
     * @param page                  page
     * @param projectCode           projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param searchVal             searchVal
     * @param executorId            executorId
     * @param statusArray           statusArray
     * @param host                  host
     * @param startTime             startTime
     * @param endTime               endTime
     * @return process instance page
     */
    IPage<ProcessInstance> queryProcessInstanceListPaging(Page<ProcessInstance> page,
                                                          @Param("projectCode") Long projectCode,
                                                          @Param("processDefinitionCode") Long processDefinitionCode,
                                                          @Param("searchVal") String searchVal,
                                                          @Param("executorId") Integer executorId,
                                                          @Param("states") int[] statusArray,
                                                          @Param("host") String host,
                                                          @Param("startTime") Date startTime,
                                                          @Param("endTime") Date endTime);

    /**
     * 根据主机名和状态数组设置流程实例为故障转移状态。
     * SQL: UPDATE t_ds_process_instance SET state = #{failoverState} WHERE host = #{host} AND state IN #{stateArray}
     *
     * @param host       host
     * @param stateArray stateArray
     * @return set result
     */
    int setFailoverByHostAndStateArray(@Param("host") String host,
                                       @Param("states") int[] stateArray);

    /**
     * 将流程实例从原始状态批量更新为目标状态。
     * SQL: UPDATE t_ds_process_instance SET state = #{destState} WHERE state = #{originState}
     *
     * @param originState originState
     * @param destState   destState
     * @return update result
     */
    int updateProcessInstanceByState(@Param("originState") WorkflowExecutionStatus originState,
                                     @Param("destState") WorkflowExecutionStatus destState);

    /**
     * 将流程实例从原始租户批量迁移到目标租户。
     * SQL: UPDATE t_ds_process_instance SET tenant_id = #{destTenantId} WHERE tenant_id = #{originTenantId}
     *
     * @param originTenantId originTenantId
     * @param destTenantId   destTenantId
     * @return update result
     */
    int updateProcessInstanceByTenantId(@Param("originTenantId") int originTenantId,
                                        @Param("destTenantId") int destTenantId);

    /**
     * 将流程实例从原始Worker分组批量迁移到目标Worker分组。
     * SQL: UPDATE t_ds_process_instance SET worker_group = #{destWorkerGroupName} WHERE worker_group = #{originWorkerGroupName}
     *
     * @param originWorkerGroupName originWorkerGroupName
     * @param destWorkerGroupName   destWorkerGroupName
     * @return update result
     */
    int updateProcessInstanceByWorkerGroupName(@Param("originWorkerGroupName") String originWorkerGroupName,
                                               @Param("destWorkerGroupName") String destWorkerGroupName);

    /**
     * 统计指定项目列表和时间范围内的流程实例各状态数量。
     * SQL: SELECT state, COUNT(*) AS count FROM t_ds_process_instance
     *      WHERE start_time BETWEEN #{startTime} AND #{endTime} AND project_code IN #{projectCodes}
     *      GROUP BY state
     * 仅通过项目编码来判断流程实例是否属于用户。
     *
     * @param startTime    startTime
     * @param endTime      endTime
     * @param projectCodes projectCodes
     * @return ExecuteStatusCount list
     */
    List<ExecuteStatusCount> countInstanceStateByProjectCodes(
                                                              @Param("startTime") Date startTime,
                                                              @Param("endTime") Date endTime,
                                                              @Param("projectCodes") Long[] projectCodes);

    /**
     * 根据流程定义编码查询指定数量的流程实例列表。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode} LIMIT #{size}
     *
     * @param processDefinitionCode processDefinitionCode
     * @param size                  size
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineCode(@Param("processDefinitionCode") Long processDefinitionCode,
                                                   @Param("size") int size);

    /**
     * 查询指定时间范围内最后一次调度的流程实例。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode}
     *      AND start_time BETWEEN #{startTime} AND #{endTime} AND command_type = 'SCHEDULER'
     *      ORDER BY start_time DESC LIMIT 1
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @return process instance
     */
    ProcessInstance queryLastSchedulerProcess(@Param("processDefinitionCode") Long definitionCode,
                                              @Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime);

    /**
     * 查询指定时间范围内最后一次运行的流程实例（支持状态过滤）。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode}
     *      AND start_time BETWEEN #{startTime} AND #{endTime} AND state IN #{states}
     *      ORDER BY start_time DESC LIMIT 1
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @param stateArray     stateArray
     * @return process instance
     */
    ProcessInstance queryLastRunningProcess(@Param("processDefinitionCode") Long definitionCode,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime,
                                            @Param("states") int[] stateArray);

    /**
     * 查询指定时间范围内最后一次手动触发的流程实例。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode}
     *      AND start_time BETWEEN #{startTime} AND #{endTime} AND command_type = 'MANUAL'
     *      ORDER BY start_time DESC LIMIT 1
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @return process instance
     */
    ProcessInstance queryLastManualProcess(@Param("processDefinitionCode") Long definitionCode,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);

    /**
     * 按运行时长降序查询Top N流程实例，用于统计耗时最长的流程。
     * SQL: SELECT * FROM t_ds_process_instance WHERE start_time BETWEEN #{startTime} AND #{endTime}
     *      AND state = #{status} AND project_code = #{projectCode}
     *      ORDER BY (end_time - start_time) DESC LIMIT #{size}
     *
     * @param size        size
     * @param startTime   start time
     * @param endTime     end time
     * @param status      process instance status
     * @param projectCode project code
     * @return ProcessInstance list
     */
    List<ProcessInstance> queryTopNProcessInstance(@Param("size") int size,
                                                   @Param("startTime") Date startTime,
                                                   @Param("endTime") Date endTime,
                                                   @Param("status") WorkflowExecutionStatus status,
                                                   @Param("projectCode") long projectCode);

    /**
     * 根据流程定义编码和状态数组查询流程实例列表。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode} AND state IN #{states}
     *
     * @param processDefinitionCode processDefinitionCode
     * @param states                states array
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineCodeAndStatus(@Param("processDefinitionCode") Long processDefinitionCode,
                                                            @Param("states") int[] states);

    /**
     * 根据流程定义编码、版本号、状态数组以及起始ID查询流程实例列表，用于串行流程的后续实例获取。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode}
     *      AND process_definition_version = #{processDefinitionVersion} AND state IN #{states} AND id > #{id}
     *      ORDER BY id ASC
     *
     * @param processDefinitionCode    processDefinitionCode
     * @param processDefinitionVersion processDefinitionVersion
     * @param states                   states array
     * @param id                       id
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(@Param("processDefinitionCode") Long processDefinitionCode,
                                                                                                @Param("processDefinitionVersion") int processDefinitionVersion,
                                                                                                @Param("states") int[] states,
                                                                                                @Param("id") Integer id);

    /**
     * 根据流程实例ID更新全局参数。
     * SQL: UPDATE t_ds_process_instance SET global_params = #{globalParams} WHERE id = #{id}
     *
     * @param globalParams globalParams
     * @param id           id
     * @return update count
     */
    int updateGlobalParamsById(@Param("globalParams") String globalParams,
                               @Param("id") int id);

    /**
     * 更新流程实例的下一个流程实例ID，用于串行流程的链式连接。
     * SQL: UPDATE t_ds_process_instance SET next_process_instance_id = #{runningInstanceId} WHERE id = #{thisInstanceId}
     *
     * @param thisInstanceId    thisInstanceId
     * @param runningInstanceId runningInstanceId
     * @return true if update success
     */
    boolean updateNextProcessIdById(@Param("thisInstanceId") int thisInstanceId,
                                    @Param("runningInstanceId") int runningInstanceId);

    /**
     * 加载串行模式下流程定义的下一个待执行流程实例。
     * SQL: SELECT * FROM t_ds_process_instance WHERE process_definition_code = #{processDefinitionCode}
     *      AND state = #{state} AND id > #{id} ORDER BY id ASC LIMIT 1
     *
     * @param processDefinitionCode processDefinitionCode
     * @param state                 state
     * @param id                    id
     * @return process instance
     */
    ProcessInstance loadNextProcess4Serial(@Param("processDefinitionCode") Long processDefinitionCode,
                                           @Param("state") int state, @Param("id") int id);
}
