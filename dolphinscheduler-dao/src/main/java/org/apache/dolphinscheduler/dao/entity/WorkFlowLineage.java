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

import java.util.Date;

/**
 * 工作流血缘信息，描述工作流之间的依赖关系和调度信息。
 */
public class WorkFlowLineage {
    /** 工作流编码 */
    private long workFlowCode;
    /** 工作流名称 */
    private String workFlowName;
    /** 工作流发布状态 */
    private String workFlowPublishStatus;
    /** 调度开始时间 */
    private Date scheduleStartTime;
    /** 调度结束时间 */
    private Date scheduleEndTime;
    /** Cron 表达式 */
    private String crontab;
    /** 调度发布状态 */
    private int schedulePublishStatus;
    /** 源工作流编码 */
    private String sourceWorkFlowCode;

    public long getWorkFlowCode() {
        return workFlowCode;
    }

    public void setWorkFlowCode(long workFlowCode) {
        this.workFlowCode = workFlowCode;
    }

    public String getWorkFlowName() {
        return workFlowName;
    }

    public void setWorkFlowName(String workFlowName) {
        this.workFlowName = workFlowName;
    }

    public String getWorkFlowPublishStatus() {
        return workFlowPublishStatus;
    }

    public void setWorkFlowPublishStatus(String workFlowPublishStatus) {
        this.workFlowPublishStatus = workFlowPublishStatus;
    }

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public Date getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(Date scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }

    public String getCrontab() {
        return crontab;
    }

    public void setCrontab(String crontab) {
        this.crontab = crontab;
    }

    public int getSchedulePublishStatus() {
        return schedulePublishStatus;
    }

    public void setSchedulePublishStatus(int schedulePublishStatus) {
        this.schedulePublishStatus = schedulePublishStatus;
    }

    public String getSourceWorkFlowCode() {
        return sourceWorkFlowCode;
    }

    public void setSourceWorkFlowCode(String sourceWorkFlowCode) {
        this.sourceWorkFlowCode = sourceWorkFlowCode;
    }
}
