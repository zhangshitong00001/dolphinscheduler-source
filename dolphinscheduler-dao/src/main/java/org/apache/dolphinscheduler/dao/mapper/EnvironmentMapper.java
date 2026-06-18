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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Set;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 环境配置 Mapper 接口，封装对 t_ds_environment 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供环境信息的查询、分页、删除及按名称搜索等能力。
 */
public interface EnvironmentMapper extends BaseMapper<Environment> {

    /**
     * 根据环境名称精确查询环境配置。
     * SELECT * FROM t_ds_environment WHERE name = #{environmentName}
     *
     * @param name 环境名称
     * @return 环境实体，若不存在则返回 null
     */
    Environment queryByEnvironmentName(@Param("environmentName") String name);

    /**
     * 根据环境编码（唯一标识）查询环境配置。
     * SELECT * FROM t_ds_environment WHERE code = #{environmentCode}
     *
     * @param environmentCode 环境编码
     * @return 环境实体，若不存在则返回 null
     */
    Environment queryByEnvironmentCode(@Param("environmentCode") Long environmentCode);

    /**
     * 查询所有环境配置列表。
     * SELECT * FROM t_ds_environment
     *
     * @return 全部环境配置列表
     */
    List<Environment> queryAllEnvironmentList();

    /**
     * 分页查询环境配置列表，支持按名称搜索（LIKE 模糊匹配）。
     *
     * @param page 分页对象
     * @param searchName 搜索名称关键字
     * @return 环境配置分页结果
     */
    IPage<Environment> queryEnvironmentListPaging(IPage<Environment> page, @Param("searchName") String searchName);

    /**
     * 根据环境编码删除环境配置记录。
     * DELETE FROM t_ds_environment WHERE code = #{code}
     *
     * @param code 环境编码
     * @return 删除的记录数
     */
    int deleteByCode(@Param("code") Long code);

    /**
     * 根据指定的环境ID列表分页查询环境配置，支持按名称搜索（LIKE 模糊匹配）。
     * SELECT * FROM t_ds_environment WHERE id IN (#{ids}) AND name LIKE CONCAT('%', #{searchName}, '%')
     *
     * @param page 分页对象
     * @param ids 环境ID列表
     * @param searchVal 搜索关键字
     * @return 环境配置分页结果
     */
    IPage<Environment> queryEnvironmentListPagingByIds(Page<Environment> page, @Param("ids")List<Integer> ids, @Param("searchName")String searchVal);
}
