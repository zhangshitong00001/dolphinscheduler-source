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

import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流血缘 Mapper 接口，封装对 t_ds_work_flow_lineage 及相关表的数据库操作。
 * 提供工作流血缘关系的查询，包括上下游依赖、流程谱系以及任务依赖关系等能力。
 */
public interface WorkFlowLineageMapper {

    /**
     * 根据项目编码和流程名称查询工作流血缘关系列表。
     * SELECT * FROM t_ds_work_flow_lineage WHERE project_code = #{projectCode} AND work_flow_name LIKE CONCAT('%', #{workFlowName}, '%')
     *
     * @param projectCode 项目编码
     * @param workFlowName 流程名称搜索关键字
     * @return 工作流血缘关系列表
     */
    List<WorkFlowLineage> queryWorkFlowLineageByName(@Param("projectCode") long projectCode, @Param("workFlowName") String workFlowName);

    /**
     * 根据项目编码和流程编码精确查询工作流血缘关系。
     * SELECT * FROM t_ds_work_flow_lineage WHERE project_code = #{projectCode} AND work_flow_code = #{workFlowCode}
     *
     * @param projectCode 项目编码
     * @param workFlowCode 流程编码
     * @return 工作流血缘关系实体，若不存在则返回 null
     */
    WorkFlowLineage queryWorkFlowLineageByCode(@Param("projectCode") long projectCode, @Param("workFlowCode") long workFlowCode);

    /**
     * 根据流程定义编码列表批量查询工作流血缘关系。
     * SELECT * FROM t_ds_work_flow_lineage WHERE work_flow_code IN (#{workFlowCodes})
     *
     * @param workFlowCodes 流程编码列表
     * @return 工作流血缘关系列表
     */
    List<WorkFlowLineage> queryWorkFlowLineageByProcessDefinitionCodes(@Param("workFlowCodes") List<Long> workFlowCodes);

    /**
     * 根据流程血缘关系列表查询对应的工作流血缘详细信息。
     * SELECT * FROM t_ds_work_flow_lineage WHERE (src_work_flow_code, dest_work_flow_code) IN (...)
     *
     * @param processLineages 流程血缘关系列表
     * @return 工作流血缘关系列表
     */
    List<WorkFlowLineage> queryWorkFlowLineageByLineage(@Param("processLineages") List<ProcessLineage> processLineages);

    /**
     * 查询指定项目下所有流程的血缘关系。
     * SELECT DISTINCT src_work_flow_code, dest_work_flow_code FROM t_ds_process_lineage WHERE project_code = #{projectCode}
     *
     * @param projectCode 项目编码
     * @return 流程血缘关系列表
     */
    List<ProcessLineage> queryProcessLineage(@Param("projectCode") long projectCode);

    /**
     * 根据项目编码和流程定义编码查询该流程的上下游血缘关系。
     * SELECT * FROM t_ds_process_lineage WHERE project_code = #{projectCode} AND (src_work_flow_code = #{processDefinitionCode} OR dest_work_flow_code = #{processDefinitionCode})
     *
     * @param projectCode 项目编码
     * @param processDefinitionCode 流程定义编码
     * @return 流程血缘关系列表
     */
    List<ProcessLineage> queryProcessLineageByCode(@Param("projectCode") long projectCode,
                                                   @Param("processDefinitionCode") long processDefinitionCode);

    /**
     * 根据流程定义编码查询其依赖的下游流程定义。
     * SELECT * FROM t_ds_process_definition WHERE code IN (SELECT dest_work_flow_code FROM t_ds_process_lineage WHERE src_work_flow_code = #{code})
     *
     * @param code 流程定义编码
     * @return 依赖的下游流程定义列表
     */
    List<DependentProcessDefinition> queryDependentProcessDefinitionByProcessDefinitionCode(@Param("code") long code);

    /**
     * 根据流程定义编码和任务类型查询该流程的下游工作流血缘关系。
     * SELECT * FROM t_ds_work_flow_lineage WHERE src_work_flow_code = #{code} AND task_type = #{taskType}
     *
     * @param code 流程定义编码
     * @param taskType 任务类型
     * @return 下游工作流血缘关系列表
     */
    List<WorkFlowLineage> queryDownstreamLineageByProcessDefinitionCode(@Param("code") long code,
                                                                        @Param("taskType") String taskType);


    /**
     * 根据流程定义编码和任务类型查询该流程的上游依赖任务参数。
     * SELECT task_params FROM t_ds_process_task_relation WHERE process_definition_code = #{code} AND task_type = #{taskType}
     *
     * @param code 流程定义编码
     * @param taskType 任务类型
     * @return 上游依赖的流程定义列表
     */
    List<DependentProcessDefinition> queryUpstreamDependentParamsByProcessDefinitionCode(@Param("code") long code,
                                                                                         @Param("taskType") String taskType);

    /**
     * 查询指定流程定义中所有类型为 SUB_PROCESS 的子流程依赖任务主信息。
     * 查询所有上游子流程类型的任务。
     * SELECT * FROM t_ds_task_definition WHERE process_definition_code = #{processDefinitionCode} AND task_type = 'SUB_PROCESS'
     *
     * @param projectCode 项目编码
     * @param processDefinitionCode 流程定义编码
     * @return 子流程依赖的任务主信息列表
     */
    List<TaskMainInfo> queryTaskSubProcessDepOnProcess(@Param("projectCode") long projectCode,
                                                       @Param("processDefinitionCode") long processDefinitionCode);

    /**
     * 查询指定流程定义中所有类型为 DEPENDENT 的依赖任务主信息。
     * 查询所有下游 DEPENDENT 类型任务。方法 queryTaskDepOnTask 是当前方法 queryTaskDependentDepOnProcess 的子集，
     * 即对于相同的 processDefinitionCode 参数，queryTaskDepOnTask 的所有结果都包含在 queryTaskDependentDepOnProcess 的结果中。
     * SELECT * FROM t_ds_task_definition WHERE process_definition_code = #{processDefinitionCode} AND task_type = 'DEPENDENT'
     *
     * @param projectCode 项目编码
     * @param processDefinitionCode 流程定义编码
     * @return 依赖任务的主信息列表
     */
    List<TaskMainInfo> queryTaskDependentDepOnProcess(@Param("projectCode") long projectCode,
                                                      @Param("processDefinitionCode") long processDefinitionCode);

    /**
     * 查询依赖于指定任务的所有下游任务主信息（目前仅支持 DEPENDENT 任务类型）。
     * 方法 queryTaskDepOnTask 是 queryTaskDependentDepOnProcess 的子集，
     * 即对于相同的 processDefinitionCode，queryTaskDepOnTask 的所有结果都包含在 queryTaskDependentDepOnProcess 的结果中。
     * SELECT * FROM t_ds_task_definition WHERE process_definition_code = #{processDefinitionCode} AND task_code = #{taskCode} AND task_type = 'DEPENDENT'
     *
     * @param projectCode 项目编码
     * @param processDefinitionCode 流程定义编码
     * @param taskCode 任务编码
     * @return 依赖该任务的下游任务主信息列表
     */
    List<TaskMainInfo> queryTaskDepOnTask(@Param("projectCode") long projectCode,
                                          @Param("processDefinitionCode") long processDefinitionCode,
                                          @Param("taskCode") long taskCode);
}
