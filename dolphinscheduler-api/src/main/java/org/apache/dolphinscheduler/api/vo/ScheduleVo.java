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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.time.ZoneId;
import java.util.Date;

import lombok.Data;

/**
 * 调度视图对象。用于返回调度配置的前端展示数据，包含流程定义信息、定时策略、告警配置等。
 */
@Data
public class ScheduleVo {

    private int id;

    /**
     * 流程定义Code
     */
    private long processDefinitionCode;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 调度描述
     */
    private String definitionDescription;

    /**
     * 调度开始时间
     */
    private String startTime;

    /**
     * 调度结束时间
     */
    private String endTime;

    /**
     * 时区ID
     * <p>see {@link java.util.TimeZone#getTimeZone(String)}
     */
    private String timezoneId;

    /**
     * Crontab表达式
     */
    private String crontab;

    /**
     * 失败策略
     */
    private FailureStrategy failureStrategy;

    /**
     * 告警类型
     */
    private WarningType warningType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户ID
     */
    private int userId;

    /**
     * 创建用户名
     */
    private String userName;

    /**
     * 发布状态
     */
    private ReleaseState releaseState;

    /**
     * 告警组ID
     */
    private int warningGroupId;

    /**
     * 流程实例优先级
     */
    private Priority processInstancePriority;

    /**
     * Worker分组
     */
    private String workerGroup;

    /**
     * 环境Code
     */
    private Long environmentCode;

    public ScheduleVo(Schedule schedule) {
        this.setId(schedule.getId());
        this.setCrontab(schedule.getCrontab());
        this.setProjectName(schedule.getProjectName());
        this.setUserName(schedule.getUserName());
        this.setWorkerGroup(schedule.getWorkerGroup());
        this.setWarningType(schedule.getWarningType());
        this.setWarningGroupId(schedule.getWarningGroupId());
        this.setUserId(schedule.getUserId());
        this.setCreateTime(schedule.getCreateTime());
        this.setUpdateTime(schedule.getUpdateTime());
        this.setTimezoneId(schedule.getTimezoneId());
        this.setReleaseState(schedule.getReleaseState());
        this.setProcessInstancePriority(schedule.getProcessInstancePriority());
        this.setProcessDefinitionName(schedule.getProcessDefinitionName());
        this.setProcessDefinitionCode(schedule.getProcessDefinitionCode());
        this.setFailureStrategy(schedule.getFailureStrategy());
        this.setEnvironmentCode(schedule.getEnvironmentCode());
        this.setStartTime(DateUtils.dateToString(schedule.getStartTime(), ZoneId.systemDefault().getId()));
        this.setEndTime(DateUtils.dateToString(schedule.getEndTime(), ZoneId.systemDefault().getId()));
    }
}
