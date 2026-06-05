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

import java.util.Map;

/**
 * 数据质量规则服务接口。提供数据质量规则的查询、表单生成和数据源选项获取等功能。
 * 用于管理数据质量检查规则的配置与选择。
 */
public interface DqRuleService {

    /**
     * 根据规则ID获取规则创建表单的JSON结构。
     *
     * @param id 规则ID
     * @return 规则表单JSON
     */
    Map<String, Object> getRuleFormCreateJsonById(int id);

    /**
     * 查询所有数据质量规则列表。
     *
     * @return 所有规则列表
     */
    Map<String, Object> queryAllRuleList();

    /**
     * 分页查询数据质量规则列表。
     *
     * @param loginUser 登录用户
     * @param searchVal 搜索关键字
     * @param ruleType  规则类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result queryRuleListPaging(User loginUser,
                               String searchVal,
                               Integer ruleType,
                               String startTime,
                               String endTime,
                               Integer pageNo, Integer pageSize);

    /**
     * 根据数据源ID获取该数据源可用的选项信息。
     *
     * @param datasourceId 数据源ID
     * @return 数据源选项信息
     */
    Map<String,Object> getDatasourceOptionsById(int datasourceId);
}
