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

import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 数据质量执行结果 Mapper 接口，封装对 t_ds_dq_execute_result 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供数据质量任务执行结果的分页查询和详情查询能力。
 */
public interface DqExecuteResultMapper extends BaseMapper<DqExecuteResult> {

    /**
     * 多条件分页查询数据质量执行结果列表。
     * 支持按搜索值（LIKE 模糊匹配）、用户权限、状态数组（IN 条件）、规则类型及时间范围进行过滤。
     *
     * @param page 分页对象
     * @param searchVal 搜索关键字，用于模糊匹配规则名称
     * @param user 当前用户，用于权限过滤
     * @param statusArray 状态数组，用于 IN 条件过滤
     * @param ruleType 规则类型
     * @param startTime 执行时间范围起始
     * @param endTime 执行时间范围结束
     * @return 数据质量执行结果分页
     */
    IPage<DqExecuteResult> queryResultListPaging(IPage<DqExecuteResult> page,
                                                 @Param("searchVal") String searchVal,
                                                 @Param("user") User user,
                                                 @Param("states") int[] statusArray,
                                                 @Param("ruleType") int ruleType,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime);

    /**
     * 根据任务实例ID查询数据质量执行结果。
     * SELECT * FROM t_ds_dq_execute_result WHERE task_instance_id = #{taskInstanceId}
     *
     * @param taskInstanceId 任务实例ID
     * @return 数据质量执行结果实体
     */
    DqExecuteResult getExecuteResultById(@Param("taskInstanceId") int taskInstanceId);
}
