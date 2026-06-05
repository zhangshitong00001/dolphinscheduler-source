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

import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 数据质量规则输入项 Mapper 接口，封装对 t_ds_dq_rule_input_entry 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供数据质量规则中输入项定义（如期望值、阈值等）的查询能力。
 */
public interface DqRuleInputEntryMapper extends BaseMapper<DqRuleInputEntry> {

    /**
     * 根据规则ID查询该规则的所有输入项列表。
     * SELECT * FROM t_ds_dq_rule_input_entry WHERE rule_id = #{ruleId}
     *
     * @param ruleId 数据质量规则ID
     * @return 规则输入项列表
     */
    List<DqRuleInputEntry> getRuleInputEntryList(@Param("ruleId") Integer ruleId);
}
