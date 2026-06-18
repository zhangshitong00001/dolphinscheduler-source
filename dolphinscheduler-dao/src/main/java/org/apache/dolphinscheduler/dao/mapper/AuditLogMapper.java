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

import org.apache.dolphinscheduler.dao.entity.AuditLog;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 审计日志 Mapper 接口，封装对 t_ds_audit_log 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供审计日志的多条件分页查询及资源名称查询能力。
 */
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    /**
     * 多条件分页查询审计日志。
     * 支持按资源类型数组、操作类型数组、用户名（LIKE 模糊匹配）及日期范围过滤。
     * SELECT * FROM t_ds_audit_log WHERE resource_type IN (...) AND operation_type IN (...)
     * AND user_name LIKE CONCAT('%', #{userName}, '%') AND time BETWEEN #{startDate} AND #{endDate}
     *
     * @param page 分页对象
     * @param resourceArray 资源类型数组，用于 IN 条件过滤
     * @param operationType 操作类型数组，用于 IN 条件过滤
     * @param userName 用户名，用于模糊匹配
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 审计日志分页结果
     */
    IPage<AuditLog> queryAuditLog(IPage<AuditLog> page,
                                  @Param("resourceType") int[] resourceArray,
                                  @Param("operationType") int[] operationType,
                                  @Param("userName") String userName,
                                  @Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    /**
     * 根据资源类型和资源ID查询对应的资源名称。
     * 用于将审计日志中的 resource_type + resource_id 转换为可读的资源名称。
     *
     * @param resourceType 资源类型标识
     * @param resourceId 资源ID
     * @return 该资源对应的名称，若不存在则返回 null
     */
    String queryResourceNameByType(@Param("resourceType") String resourceType,
                                   @Param("resourceId") Integer resourceId);
}
