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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;

import java.util.Map;

/**
 * 执行器服务接口。提供流程实例和任务实例的执行、控制和管理功能。
 * 支持启动流程、暂停、停止、重跑、恢复等操作，以及补数、流式任务执行等高级功能。
 */
public interface ExecutorService {

    /**
     * 执行流程实例。
     *
     * @param loginUser                 登录用户
     * @param projectCode               项目编码
     * @param processDefinitionCode     流程定义编码
     * @param cronTime                  cron时间
     * @param commandType               命令类型
     * @param failureStrategy           失败策略
     * @param startNodeList             起始节点列表
     * @param taskDependType            节点依赖类型
     * @param warningType               告警类型
     * @param warningGroupId            告警组ID
     * @param runMode                   运行模式
     * @param processInstancePriority   流程实例优先级
     * @param workerGroup               Worker分组名称
     * @param environmentCode           环境编码
     * @param timeout                   超时时间
     * @param startParams               传递给新流程实例的全局参数
     * @param expectedParallelismNumber 并行补数时的期望并行度
     * @param dryRun                    是否为干运行
     * @param complementDependentMode   补数依赖模式
     * @return 执行结果
     */
    Map<String, Object> execProcessInstance(User loginUser, long projectCode,
                                            long processDefinitionCode, String cronTime, CommandType commandType,
                                            FailureStrategy failureStrategy, String startNodeList,
                                            TaskDependType taskDependType, WarningType warningType, int warningGroupId,
                                            RunMode runMode,
                                            Priority processInstancePriority, String workerGroup, Long environmentCode, Integer timeout,
                                            Map<String, String> startParams, Integer expectedParallelismNumber,
                                            int dryRun,
                                            ComplementDependentMode complementDependentMode);

    /**
     * 检查流程定义是否可以执行。
     *
     * @param projectCode        项目编码
     * @param processDefinition 流程定义
     * @param processDefineCode 流程定义编码
     * @param verison           流程定义版本
     * @return 校验结果
     */
    Map<String, Object> checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition, long processDefineCode, Integer verison);

    /**
     * 对流程实例执行操作：暂停、停止、重跑、恢复暂停、恢复停止。
     *
     * @param loginUser          登录用户
     * @param projectCode        项目编码
     * @param processInstanceId  流程实例ID
     * @param executeType        执行操作类型
     * @return 执行结果
     */
    Map<String, Object> execute(User loginUser, long projectCode, Integer processInstanceId, ExecuteType executeType);

    /**
     * 启动流程定义前检查子流程是否已下线。
     *
     * @param processDefinitionCode 流程定义编码
     * @return 校验结果
     */
    Map<String, Object> startCheckByProcessDefinedCode(long processDefinitionCode);

    /**
     * 检查当前流程是否包含子流程以及所有子流程是否有效。
     *
     * @param processDefinition 流程定义
     * @return 全部有效返回true，否则返回false
     */
    boolean checkSubProcessDefinitionValid(ProcessDefinition processDefinition);

    /**
     * 强制启动任务实例（从队列中取出并执行）。
     *
     * @param loginUser 登录用户
     * @param queueId   队列ID
     * @return 执行结果
     */
    Map<String, Object> forceStartTaskInstance(User loginUser, int queueId);

    /**
     * 查询Master内存中正在执行的工作流数据。
     *
     * @param processInstanceId 流程实例ID
     * @return 工作流执行数据
     */
    WorkflowExecuteDto queryExecutingWorkflowByProcessInstanceId(Integer processInstanceId);

    /**
     * 执行流式任务实例。
     *
     * @param loginUser              登录用户
     * @param projectCode            项目编码
     * @param taskDefinitionCode     任务定义编码
     * @param taskDefinitionVersion  任务定义版本
     * @param warningGroupId         告警组ID
     * @param workerGroup            Worker分组名称
     * @param environmentCode        环境编码
     * @param startParams            传递给新流程实例的全局参数
     * @param dryRun                 是否为干运行
     * @return 执行结果
     */
    Map<String, Object> execStreamTaskInstance(User loginUser, long projectCode,
                                            long taskDefinitionCode, int taskDefinitionVersion,
                                            int warningGroupId,
                                            String workerGroup, Long environmentCode,
                                            Map<String, String> startParams,
                                            int dryRun);
}
