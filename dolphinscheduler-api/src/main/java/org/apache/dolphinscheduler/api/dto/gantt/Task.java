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

package org.apache.dolphinscheduler.api.dto.gantt;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 甘特图任务 DTO，表示甘特图中的单个任务节点，包含任务名称、时间区间、状态和持续时间。
 */
public class Task {
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务开始日期列表
     */
    private List<Long> startDate = new LinkedList<>();
    /**
     * 任务结束日期列表
     */
    private List<Long> endDate = new LinkedList<>();

    /**
     * 任务执行日期
     */
    private Date executionDate;

    /**
     * 任务 ISO 格式开始时间
     */
    private Date isoStart;

    /**
     * 任务 ISO 格式结束时间
     */
    private Date isoEnd;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务持续时间
     */
    private String duration;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<Long> getStartDate() {
        return startDate;
    }

    public void setStartDate(List<Long> startDate) {
        this.startDate = startDate;
    }

    public List<Long> getEndDate() {
        return endDate;
    }

    public void setEndDate(List<Long> endDate) {
        this.endDate = endDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Date getIsoStart() {
        return isoStart;
    }

    public void setIsoStart(Date isoStart) {
        this.isoStart = isoStart;
    }

    public Date getIsoEnd() {
        return isoEnd;
    }

    public void setIsoEnd(Date isoEnd) {
        this.isoEnd = isoEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
