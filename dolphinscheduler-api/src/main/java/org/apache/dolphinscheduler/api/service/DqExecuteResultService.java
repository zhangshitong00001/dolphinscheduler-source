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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

/**
 * 数据质量执行结果服务接口。提供数据质量规则执行结果的分页查询功能。
 * 用于追踪和展示数据质量检查任务的执行情况，可按状态、规则类型和时间范围过滤。
 */
public interface DqExecuteResultService {

    /**
     * 分页查询数据质量执行结果列表。
     *
     * @param loginUser 登录用户
     * @param searchVal 搜索关键字
     * @param state     执行状态
     * @param ruleType  规则类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result queryResultListPaging(User loginUser,
                                 String searchVal,
                                 Integer state,
                                 Integer ruleType,
                                 String startTime,
                                 String endTime,
                                 Integer pageNo, Integer pageSize);
}
