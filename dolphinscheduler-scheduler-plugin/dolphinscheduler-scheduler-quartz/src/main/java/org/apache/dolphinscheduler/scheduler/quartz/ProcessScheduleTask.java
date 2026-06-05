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

package org.apache.dolphinscheduler.scheduler.quartz;

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.scheduler.quartz.utils.QuartzTaskUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 * 基于Quartz的定时调度任务执行器，继承自Spring的 {@link QuartzJobBean}。
 * <p>
 * 当Quartz触发器触发时，会调用 {@link #executeInternal} 方法。
 * 该方法执行以下逻辑：
 * <ol>
 *   <li>从JobDataMap中提取项目ID和调度ID</li>
 *   <li>校验调度记录和工作流定义是否仍在线（在线状态）</li>
 *   <li>如果已下线，删除对应的Quartz任务</li>
 *   <li>如果在线，创建一条类型为 {@link CommandType#SCHEDULER} 的命令，交由Master处理</li>
 * </ol>
 * <p>
 * 同时集成了 Micrometer 指标收集，记录Quartz任务执行次数和耗时。
 */
public class ProcessScheduleTask extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(ProcessScheduleTask.class);

    @Autowired
    private ProcessService processService;

    /** Micrometer计数：Quartz任务执行次数 */
    @Counted(value = "ds.master.quartz.job.executed")
    /** Micrometer计时：Quartz任务执行耗时，采集50/75/95/99分位数和直方图 */
    @Timed(value = "ds.master.quartz.job.execution.time", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        int projectId = dataMap.getInt(QuartzTaskUtils.PROJECT_ID);
        int scheduleId = dataMap.getInt(QuartzTaskUtils.SCHEDULE_ID);

        Date scheduledFireTime = context.getScheduledFireTime();

        Date fireTime = context.getFireTime();

        logger.info("scheduled fire time :{}, fire time :{}, process id :{}", scheduledFireTime, fireTime, scheduleId);

        // 查询调度记录，校验是否存在且在线
        Schedule schedule = processService.querySchedule(scheduleId);
        if (schedule == null || ReleaseState.OFFLINE == schedule.getReleaseState()) {
            logger.warn("process schedule does not exist in db or process schedule offline，delete schedule job in quartz, projectId:{}, scheduleId:{}", projectId, scheduleId);
            deleteJob(context, projectId, scheduleId);
            return;
        }

        ProcessDefinition processDefinition = processService.findProcessDefinitionByCode(schedule.getProcessDefinitionCode());
        // 校验工作流定义是否在线
        ReleaseState releaseState = processDefinition.getReleaseState();
        if (releaseState == ReleaseState.OFFLINE) {
            logger.warn("process definition does not exist in db or offline，need not to create command, projectId:{}, processId:{}", projectId, processDefinition.getId());
            return;
        }

        // 构建调度命令并创建
        Command command = new Command();
        command.setCommandType(CommandType.SCHEDULER);
        command.setExecutorId(schedule.getUserId());
        command.setFailureStrategy(schedule.getFailureStrategy());
        command.setProcessDefinitionCode(schedule.getProcessDefinitionCode());
        command.setScheduleTime(scheduledFireTime);
        command.setStartTime(fireTime);
        command.setWarningGroupId(schedule.getWarningGroupId());
        String workerGroup = StringUtils.isEmpty(schedule.getWorkerGroup()) ? Constants.DEFAULT_WORKER_GROUP : schedule.getWorkerGroup();
        command.setWorkerGroup(workerGroup);
        command.setEnvironmentCode(schedule.getEnvironmentCode());
        command.setWarningType(schedule.getWarningType());
        command.setProcessInstancePriority(schedule.getProcessInstancePriority());
        command.setProcessDefinitionVersion(processDefinition.getVersion());

        processService.createCommand(command);
    }

    /**
     * 从Quartz中删除指定的调度任务（当调度记录已下线或不存在时调用）
     */
    private void deleteJob(JobExecutionContext context, int projectId, int scheduleId) {
        final Scheduler scheduler = context.getScheduler();
        JobKey jobKey = QuartzTaskUtils.getJobKey(scheduleId, projectId);
        try {
            if (scheduler.checkExists(jobKey)) {
                logger.info("Try to delete job: {}, projectId: {}, schedulerId", projectId, scheduleId);
                scheduler.deleteJob(jobKey);
            }
        } catch (Exception e) {
            logger.error("Failed to delete job: {}", jobKey);
        }
    }
}
