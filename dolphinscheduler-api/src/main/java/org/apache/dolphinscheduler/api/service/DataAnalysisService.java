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

import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务接口。提供任务实例、流程实例、命令状态等多维度的统计分析功能。
 * 用于首页仪表盘的数据展示，包括按项目统计任务状态分布、流程实例状态分布、命令状态和队列状态等。
 */
public interface DataAnalysisService {

    /**
     * 按项目统计任务实例的状态分布。
     *
     * @param loginUser   登录用户
     * @param projectCode 项目编码
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 任务状态统计结果
     */
    Map<String, Object> countTaskStateByProject(User loginUser, long projectCode, String startDate, String endDate);

    /**
     * 按项目统计流程实例的状态分布。
     *
     * @param loginUser   登录用户
     * @param projectCode 项目编码
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 流程状态统计结果
     */
    Map<String, Object> countProcessInstanceStateByProject(User loginUser, long projectCode, String startDate, String endDate);

    /**
     * 统计用户的工作流定义数量。
     * 仅在用户有权限查看的项目范围内判断工作流定义归属。
     *
     * @param loginUser   登录用户
     * @param projectCode 项目编码
     * @return 定义数量统计结果
     */
    Map<String, Object> countDefinitionByUser(User loginUser, long projectCode);

    /**
     * 统计各命令状态的数量分布。
     *
     * @param loginUser 登录用户
     * @return 命令状态统计结果
     */
    Map<String, Object> countCommandState(User loginUser);

    /**
     * 统计各队列状态的数量分布。
     *
     * @param loginUser 登录用户
     * @return 队列状态统计结果
     */
    Map<String, Object> countQueueState(User loginUser);

    /**
     * 按项目编码列表统计所有状态的任务实例数量。
     * 通过项目编码列表来判断任务实例是否属于用户可访问的范围。
     *
     * @param startTime    统计开始时间
     * @param endTime      统计结束时间
     * @param projectCodes 项目编码列表，用于过滤
     * @return 各状态的任务实例统计列表
     */
    List<ExecuteStatusCount> countTaskInstanceAllStatesByProjectCodes(@Param("startTime") Date startTime,
                                                                      @Param("endTime") Date endTime,
                                                                      @Param("projectCodes") Long[] projectCodes);
}
