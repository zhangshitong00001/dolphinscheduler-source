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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.audit.AuditMessage;
import org.apache.dolphinscheduler.api.audit.AuditPublishService;
import org.apache.dolphinscheduler.api.dto.AuditDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.AuditResourceType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 审计服务实现类。负责审计日志的记录和分页查询，支持按资源类型、操作类型、日期和用户等条件过滤。
 */
@Service
public class AuditServiceImpl extends BaseServiceImpl implements AuditService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private AuditPublishService publishService;

    /**
     * 添加审计日志。通过消息发布服务异步记录用户操作。
     *
     * @param user 操作用户
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param operation 操作类型
     */
    @Override
    public void addAudit(User user, AuditResourceType resourceType, Integer resourceId, AuditOperationType operation) {
        publishService.publish(new AuditMessage(user, new Date(), resourceType, operation, resourceId));
    }

    /**
     * 分页查询审计日志，支持按资源类型、操作类型、日期范围和用户名等条件过滤。
     *
     * @param loginUser 当前登录用户
     * @param resourceType 资源类型（可为null）
     * @param operationType 操作类型（可为null）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userName 操作人用户名
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页审计日志DTO的结果对象
     */
    @Override
    public Result queryLogListPaging(User loginUser, AuditResourceType resourceType,
                                     AuditOperationType operationType, String startDate,
                                     String endDate, String userName,
                                     Integer pageNo, Integer pageSize) {
        Result result = new Result();

        Map<String, Object> checkAndParseDateResult = checkAndParseDateParameters(startDate, endDate);
        Status resultEnum = (Status) checkAndParseDateResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            putMsg(result,resultEnum);
            return result;
        }

        int[] resourceArray = null;
        if (resourceType != null) {
            resourceArray = new int[]{resourceType.getCode()};
        }

        int[] opsArray = null;
        if (operationType != null) {
            opsArray = new int[]{operationType.getCode()};
        }

        Date start = (Date) checkAndParseDateResult.get(Constants.START_TIME);
        Date end = (Date) checkAndParseDateResult.get(Constants.END_TIME);

        Page<AuditLog> page = new Page<>(pageNo, pageSize);
        IPage<AuditLog> logIPage = auditLogMapper.queryAuditLog(page, resourceArray, opsArray, userName, start, end);
        List<AuditLog> logList = logIPage != null ? logIPage.getRecords() : new ArrayList<>();
        PageInfo<AuditDto> pageInfo = new PageInfo<>(pageNo, pageSize);

        List<AuditDto> auditDtos = logList.stream().map(this::transformAuditLog).collect(Collectors.toList());
        pageInfo.setTotal((int) (auditDtos != null ? auditDtos.size() : 0L));
        pageInfo.setTotalList(auditDtos);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 将审计日志实体转换为前端DTO，解析资源类型和操作类型的编码为可读信息。
     *
     * @param auditLog 审计日志实体
     * @return 审计日志DTO
     */
    private AuditDto transformAuditLog(AuditLog auditLog) {
        AuditDto auditDto = new AuditDto();
        String resourceType = AuditResourceType.of(auditLog.getResourceType()).getMsg();
        auditDto.setResource(resourceType);
        auditDto.setOperation(AuditOperationType.of(auditLog.getOperation()).getMsg());
        auditDto.setUserName(auditLog.getUserName());
        auditDto.setResourceName(auditLogMapper.queryResourceNameByType(resourceType, auditLog.getResourceId()));
        auditDto.setTime(auditLog.getTime());
        return auditDto;
    }
}
