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

import org.apache.dolphinscheduler.dao.entity.DqRule;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 数据质量规则 Mapper 接口，封装对 t_ds_dq_rule 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供数据质量规则的分页查询、按类型/时间范围过滤等能力。
 */
public interface DqRuleMapper extends BaseMapper<DqRule> {

    /**
     * 多条件分页查询数据质量规则列表。
     * 支持按搜索值（LIKE 模糊匹配）、规则类型和时间范围过滤。
     *
     * @param page 分页对象
     * @param searchVal 搜索关键字，用于模糊匹配规则名称
     * @param ruleType 规则类型
     * @param startTime 创建时间范围起始
     * @param endTime 创建时间范围结束
     * @return 数据质量规则分页结果
     */
    IPage<DqRule> queryRuleListPaging(IPage<DqRule> page,
                                      @Param("searchVal") String searchVal,
                                      @Param("ruleType") int ruleType,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime);
}
