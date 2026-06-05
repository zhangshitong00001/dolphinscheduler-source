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
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.AuditResourceType;
import org.apache.dolphinscheduler.dao.entity.User;

/**
 * 审计信息服务接口。提供审计日志的记录和分页查询功能，用于追踪系统中的操作行为。
 * 记录用户在各类资源上的操作类型、操作时间等信息。
 */
public interface AuditService {

    /**
     * 添加新的审计记录。
     *
     * @param user         登录用户
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     * @param operation    操作类型
     */
    void addAudit(User user, AuditResourceType resourceType, Integer resourceId, AuditOperationType operation);

    /**
     * 分页查询审计日志列表。
     *
     * @param loginUser     登录用户
     * @param resourceType  资源类型
     * @param operationType 操作类型
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param userName      操作用户名
     * @param pageNo        页码
     * @param pageSize      每页大小
     * @return 分页查询结果
     */
    Result queryLogListPaging(User loginUser, AuditResourceType resourceType,
                              AuditOperationType operationType, String startTime,
                              String endTime, String userName,
                              Integer pageNo, Integer pageSize);
}
