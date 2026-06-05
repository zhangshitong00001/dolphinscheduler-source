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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.CycleEnum;

import java.util.Date;

/**
 * 周期依赖实体，用于表示工作流之间基于时间周期的依赖关系。
 * 非数据库表映射，在依赖任务解析过程中使用，用于判断上游工作流的调度周期与当前任务的时间依赖关系。
 */
public class CycleDependency {
    /** 工作流定义 ID，关联的上游工作流 */
    private int processDefineId;
    /** 上游工作流最后一次调度时间 */
    private Date lastScheduleTime;
    /** 依赖过期时间，超过该时间的依赖关系视为无效 */
    private Date expirationTime;
    /** 周期枚举，表示时间周期单位（小时、天、周、月、年等） */
    private CycleEnum cycleEnum;

    public CycleDependency(int processDefineId, Date lastScheduleTime, Date expirationTime, CycleEnum cycleEnum) {
        this.processDefineId = processDefineId;
        this.lastScheduleTime = lastScheduleTime;
        this.expirationTime = expirationTime;
        this.cycleEnum = cycleEnum;
    }

    public int getProcessDefineId() {
        return processDefineId;
    }

    public void setProcessDefineId(int processDefineId) {
        this.processDefineId = processDefineId;
    }

    public Date getLastScheduleTime() {
        return lastScheduleTime;
    }

    public void setLastScheduleTime(Date lastScheduleTime) {
        this.lastScheduleTime = lastScheduleTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public CycleEnum getCycleEnum() {
        return cycleEnum;
    }

    public void setCycleEnum(CycleEnum cycleEnum) {
        this.cycleEnum = cycleEnum;
    }

    @Override
    public String toString() {
        return "CycleDependency{"
                + "processDefineId=" + processDefineId
                + ", lastScheduleTime=" + lastScheduleTime
                + ", expirationTime=" + expirationTime
                + ", cycleEnum=" + cycleEnum
                + '}';
    }
}
