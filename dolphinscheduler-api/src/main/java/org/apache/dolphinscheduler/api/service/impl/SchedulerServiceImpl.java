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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT;

import org.apache.dolphinscheduler.api.dto.ScheduleParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.ScheduleVo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cronutils.model.Cron;

/**
 * 调度服务实现类。负责定时调度的增删改查、上线/下线管理、Cron表达式解析和预览，调度任务通过SchedulerApi与Master节点交互。
 */
@Service
public class SchedulerServiceImpl extends BaseServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private SchedulerApi schedulerApi;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    /**
     * 创建定时调度。校验Cron表达式、时间参数和流程定义状态，创建后默认为离线状态。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processDefineCode 流程定义编码
     * @param schedule 调度参数（JSON格式，含crontab、起止时间、时区）
     * @param warningType 告警类型
     * @param warningGroupId 告警组ID
     * @param failureStrategy 失败策略
     * @param processInstancePriority 流程实例优先级
     * @param workerGroup 工作组
     * @param environmentCode 环境编码
     * @return 包含创建的调度对象的结果Map
     */
    @Override
    @Transactional
    public Map<String, Object> insertSchedule(User loginUser,
                                              long projectCode,
                                              long processDefineCode,
                                              String schedule,
                                              WarningType warningType,
                                              int warningGroupId,
                                              FailureStrategy failureStrategy,
                                              Priority processInstancePriority,
                                              String workerGroup,
                                              Long environmentCode) {

        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        // check work flow define release state
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefineCode);
        result = executorService.checkProcessDefinitionValid(projectCode, processDefinition, processDefineCode,
                processDefinition.getVersion());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        Schedule scheduleObj = new Schedule();
        Date now = new Date();

        scheduleObj.setProjectName(project.getName());
        scheduleObj.setProcessDefinitionCode(processDefineCode);
        scheduleObj.setProcessDefinitionName(processDefinition.getName());

        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
            logger.warn("The start time must not be the same as the end");
            putMsg(result, Status.SCHEDULE_START_TIME_END_TIME_SAME);
            return result;
        }
        if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
            logger.warn("The start time must smaller than end time");
            putMsg(result, Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
            return result;
        }

        scheduleObj.setStartTime(scheduleParam.getStartTime());
        scheduleObj.setEndTime(scheduleParam.getEndTime());
        if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
            logger.error("Schedule crontab verify failure, crontab:{}.", scheduleParam.getCrontab());
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleParam.getCrontab());
            return result;
        }
        scheduleObj.setCrontab(scheduleParam.getCrontab());
        scheduleObj.setTimezoneId(scheduleParam.getTimezoneId());
        scheduleObj.setWarningType(warningType);
        scheduleObj.setWarningGroupId(warningGroupId);
        scheduleObj.setFailureStrategy(failureStrategy);
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setUserName(loginUser.getUserName());
        scheduleObj.setReleaseState(ReleaseState.OFFLINE);
        scheduleObj.setProcessInstancePriority(processInstancePriority);
        scheduleObj.setWorkerGroup(workerGroup);
        scheduleObj.setEnvironmentCode(environmentCode);
        scheduleMapper.insert(scheduleObj);

        /**
         * updateProcessInstance receivers and cc by process definition id
         */
        processDefinition.setWarningGroupId(warningGroupId);
        processDefinitionMapper.updateById(processDefinition);

        // return scheduler object with ID
        result.put(Constants.DATA_LIST, scheduleMapper.selectById(scheduleObj.getId()));
        putMsg(result, Status.SUCCESS);

        result.put("scheduleId", scheduleObj.getId());
        return result;
    }

    /**
     * 更新定时调度。校验项目权限和调度存在性，调用统一的更新方法处理变更。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param id 调度ID
     * @param scheduleExpression 新的调度参数（JSON格式）
     * @param warningType 告警类型
     * @param warningGroupId 告警组ID
     * @param failureStrategy 失败策略
     * @param processInstancePriority 流程实例优先级
     * @param workerGroup 工作组
     * @param environmentCode 环境编码
     * @return 包含更新结果的结果Map
     */
    @Override
    @Transactional
    public Map<String, Object> updateSchedule(User loginUser,
                                              long projectCode,
                                              Integer id,
                                              String scheduleExpression,
                                              WarningType warningType,
                                              int warningGroupId,
                                              FailureStrategy failureStrategy,
                                              Priority processInstancePriority,
                                              String workerGroup,
                                              Long environmentCode) {
        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        // check schedule exists
        Schedule schedule = scheduleMapper.selectById(id);

        if (schedule == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, id);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(schedule.getProcessDefinitionCode());
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(schedule.getProcessDefinitionCode()));
            return result;
        }

        updateSchedule(result, schedule, processDefinition, scheduleExpression, warningType, warningGroupId,
                failureStrategy, processInstancePriority, workerGroup, environmentCode);
        return result;
    }

    /**
     * 设置调度上线或离线。上线前检查流程定义和子流程的发布状态、DAG节点存在性及Master服务可用性。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param id 调度ID
     * @param scheduleStatus 目标调度状态（ONLINE/OFFLINE）
     * @throws ServiceException 条件不满足时抛出
     */
    @Override
    @Transactional
    public void setScheduleState(User loginUser,
                                 long projectCode,
                                 Integer id,
                                 ReleaseState scheduleStatus) {
        Project project = projectMapper.queryByCode(projectCode);
        // check project auth
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        // check schedule exists
        Schedule scheduleObj = scheduleMapper.selectById(id);

        if (scheduleObj == null) {
            logger.error("Schedule does not exist, scheduleId:{}.", id);
            throw new ServiceException(Status.SCHEDULE_CRON_NOT_EXISTS, id);
        }
        // check schedule release state
        if (scheduleObj.getReleaseState() == scheduleStatus) {
            logger.warn("Schedule state does not need to change due to schedule state is already {}, scheduleId:{}.",
                    scheduleObj.getReleaseState().getDescp(), scheduleObj.getId());
            throw new ServiceException(Status.SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE, scheduleStatus);
        }
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByCode(scheduleObj.getProcessDefinitionCode());
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            logger.error("Process definition does not exist, processDefinitionCode:{}.",
                    scheduleObj.getProcessDefinitionCode());
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST,
                    String.valueOf(scheduleObj.getProcessDefinitionCode()));
        }
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryByProcessCode(projectCode, scheduleObj.getProcessDefinitionCode());
        if (processTaskRelations.isEmpty()) {
            logger.error("Process task relations do not exist, projectCode:{}, processDefinitionCode:{}.", projectCode,
                    processDefinition.getCode());
            throw new ServiceException(Status.PROCESS_DAG_IS_EMPTY);
        }
        if (scheduleStatus == ReleaseState.ONLINE) {
            // check process definition release state
            if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
                logger.warn("Only process definition state is {} can change schedule state, processDefinitionCode:{}.",
                        ReleaseState.ONLINE.getDescp(), processDefinition.getCode());
                throw new ServiceException(Status.PROCESS_DEFINE_NOT_RELEASE, processDefinition.getName());
            }
            // check sub process definition release state
            List<Long> subProcessDefineCodes = new ArrayList<>();
            processService.recurseFindSubProcess(processDefinition.getCode(), subProcessDefineCodes);
            if (!subProcessDefineCodes.isEmpty()) {
                List<ProcessDefinition> subProcessDefinitionList =
                        processDefinitionMapper.queryByCodes(subProcessDefineCodes);
                if (subProcessDefinitionList != null && !subProcessDefinitionList.isEmpty()) {
                    for (ProcessDefinition subProcessDefinition : subProcessDefinitionList) {
                        /**
                         * if there is no online process, exit directly
                         */
                        if (subProcessDefinition.getReleaseState() != ReleaseState.ONLINE) {
                            logger.warn(
                                    "Only sub process definition state is {} can change schedule state, subProcessDefinitionCode:{}.",
                                    ReleaseState.ONLINE.getDescp(), subProcessDefinition.getCode());
                            throw new ServiceException(Status.PROCESS_DEFINE_NOT_RELEASE,
                                    String.valueOf(subProcessDefinition.getId()));
                        }
                    }
                }
            }
        }

        // check master server exists
        List<Server> masterServers = monitorService.getServerListFromRegistry(true);

        if (masterServers.isEmpty()) {
            logger.error("Master does not exist.");
            throw new ServiceException(Status.MASTER_NOT_EXISTS);
        }

        // set status
        scheduleObj.setReleaseState(scheduleStatus);

        scheduleMapper.updateById(scheduleObj);

        try {
            switch (scheduleStatus) {
                case ONLINE:
                    logger.info("Call master client set schedule online, project id: {}, flow id: {},host: {}",
                            project.getId(), processDefinition.getId(), masterServers);
                    setSchedule(project.getId(), scheduleObj);
                    break;
                case OFFLINE:
                    logger.info("Call master client set schedule offline, project id: {}, flow id: {},host: {}",
                            project.getId(), processDefinition.getId(), masterServers);
                    deleteSchedule(project.getId(), id);
                    break;
                default:
                    throw new ServiceException(Status.SCHEDULE_STATUS_UNKNOWN, scheduleStatus.toString());
            }
        } catch (Exception e) {
            Status status = scheduleStatus == ReleaseState.ONLINE ? Status.PUBLISH_SCHEDULE_ONLINE_ERROR
                    : Status.OFFLINE_SCHEDULE_ERROR;
            throw new ServiceException(status, e);
        }
    }

    /**
     * 分页查询指定流程定义的调度列表。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processDefineCode 流程定义编码
     * @param searchVal 搜索关键字
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页调度VO列表的结果对象
     */
    @Override
    public Result querySchedule(User loginUser, long projectCode, long processDefineCode, String searchVal,
                                Integer pageNo, Integer pageSize) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefineCode);
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefineCode));
            return result;
        }

        Page<Schedule> page = new Page<>(pageNo, pageSize);
        IPage<Schedule> scheduleIPage =
                scheduleMapper.queryByProcessDefineCodePaging(page, processDefineCode, searchVal);

        List<ScheduleVo> scheduleList = new ArrayList<>();
        for (Schedule schedule : scheduleIPage.getRecords()) {
            scheduleList.add(new ScheduleVo(schedule));
        }

        PageInfo<ScheduleVo> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) scheduleIPage.getTotal());
        pageInfo.setTotalList(scheduleList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    public List<Schedule> queryScheduleByProcessDefinitionCodes(@NonNull List<Long> processDefinitionCodes) {
        if (CollectionUtils.isEmpty(processDefinitionCodes)) {
            return Collections.emptyList();
        }
        return scheduleMapper.querySchedulesByProcessDefinitionCodes(processDefinitionCodes);
    }

    /**
     * 查询项目下所有调度列表。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @return 包含调度VO列表的结果Map
     */
    @Override
    public Map<String, Object> queryScheduleList(User loginUser, long projectCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        List<Schedule> schedules = scheduleMapper.querySchedulerListByProjectName(project.getName());
        List<ScheduleVo> scheduleList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            scheduleList.add(new ScheduleVo(schedule));
        }

        result.put(Constants.DATA_LIST, scheduleList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    public void setSchedule(int projectId, Schedule schedule) {
        logger.info("set schedule, project id: {}, scheduleId: {}", projectId, schedule.getId());
        schedulerApi.insertOrUpdateScheduleTask(projectId, schedule);
    }

    /**
     * 通过SchedulerApi删除Master上的调度任务。
     *
     * @param projectId 项目ID
     * @param scheduleId 调度ID
     */
    @Override
    public void deleteSchedule(int projectId, int scheduleId) {
        logger.info("delete schedules of project id:{}, schedule id:{}", projectId, scheduleId);
        schedulerApi.deleteScheduleTask(projectId, scheduleId);
    }

    /**
     * 校验条件的辅助方法，如果条件为true则将指定状态放入结果Map。
     *
     * @param result 结果Map
     * @param bool 校验条件
     * @param status 校验失败时使用的状态
     * @return true表示校验不通过
     */
    private boolean checkValid(Map<String, Object> result, boolean bool, Status status) {
        // timeout is valid
        if (bool) {
            putMsg(result, status);
            return true;
        }
        return false;
    }

    /**
     * 根据ID删除调度。只允许调度创建者或管理员删除，且调度必须为离线状态。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param scheduleId 调度ID
     * @return 包含删除结果的结果Map
     */
    @Override
    public Map<String, Object> deleteScheduleById(User loginUser, long projectCode, Integer scheduleId) {

        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        Schedule schedule = scheduleMapper.selectById(scheduleId);

        if (schedule == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, scheduleId);
            return result;
        }

        // Determine if the login user is the owner of the schedule
        if (loginUser.getId() != schedule.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // check schedule is already online
        if (schedule.getReleaseState() == ReleaseState.ONLINE) {
            putMsg(result, Status.SCHEDULE_CRON_STATE_ONLINE, schedule.getId());
            return result;
        }

        int delete = scheduleMapper.deleteById(scheduleId);

        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_SCHEDULE_CRON_BY_ID_ERROR);
        }
        return result;
    }

    /**
     * 预览定时调度的执行时间。根据Cron表达式和起止时间，返回后续N次触发的时间列表。
     *
     * @param loginUser 当前登录用户
     * @param schedule 调度参数（JSON格式）
     * @return 包含预览触发时间列表的结果Map
     */
    @Override
    public Map<String, Object> previewSchedule(User loginUser, String schedule) {
        Map<String, Object> result = new HashMap<>();
        Cron cron;
        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);

        assert scheduleParam != null;
        ZoneId zoneId = TimeZone.getTimeZone(scheduleParam.getTimezoneId()).toZoneId();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime startTime = ZonedDateTime.ofInstant(scheduleParam.getStartTime().toInstant(), zoneId);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(scheduleParam.getEndTime().toInstant(), zoneId);
        startTime = now.isAfter(startTime) ? now : startTime;

        try {
            cron = CronUtils.parse2Cron(scheduleParam.getCrontab());
        } catch (CronParseException e) {
            logger.error(e.getMessage(), e);
            putMsg(result, Status.PARSE_TO_CRON_EXPRESSION_ERROR);
            return result;
        }
        List<ZonedDateTime> selfFireDateList =
                CronUtils.getSelfFireDateList(startTime, endTime, cron, Constants.PREVIEW_SCHEDULE_EXECUTE_COUNT);
        List<String> previewDateList =
                selfFireDateList.stream().map(t -> DateUtils.dateToString(t, zoneId)).collect(Collectors.toList());
        result.put(Constants.DATA_LIST, previewDateList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 通过流程定义编码更新其关联的调度参数。如果调度已上线则禁止更新。
     *
     * @param loginUser 当前登录用户
     * @param projectCode 项目编码
     * @param processDefinitionCode 流程定义编码
     * @param scheduleExpression 新的调度参数（JSON格式）
     * @param warningType 告警类型
     * @param warningGroupId 告警组ID
     * @param failureStrategy 失败策略
     * @param processInstancePriority 流程实例优先级
     * @param workerGroup 工作组
     * @param environmentCode 环境编码
     * @return 包含更新结果的结果Map
     */
    @Override
    public Map<String, Object> updateScheduleByProcessDefinitionCode(User loginUser,
                                                                     long projectCode,
                                                                     long processDefinitionCode,
                                                                     String scheduleExpression,
                                                                     WarningType warningType,
                                                                     int warningGroupId,
                                                                     FailureStrategy failureStrategy,
                                                                     Priority processInstancePriority,
                                                                     String workerGroup,
                                                                     long environmentCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check schedule exists
        Schedule schedule = scheduleMapper.queryByProcessDefinitionCode(processDefinitionCode);
        if (schedule == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, processDefinitionCode);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefinitionCode));
            return result;
        }

        updateSchedule(result, schedule, processDefinition, scheduleExpression, warningType, warningGroupId,
                failureStrategy, processInstancePriority, workerGroup, environmentCode);
        return result;
    }

    private void updateSchedule(Map<String, Object> result, Schedule schedule, ProcessDefinition processDefinition,
                                String scheduleExpression, WarningType warningType, int warningGroupId,
                                FailureStrategy failureStrategy, Priority processInstancePriority, String workerGroup,
                                long environmentCode) {
        if (checkValid(result, schedule.getReleaseState() == ReleaseState.ONLINE,
                Status.SCHEDULE_CRON_ONLINE_FORBID_UPDATE)) {
            return;
        }

        Date now = new Date();

        // updateProcessInstance param
        if (!StringUtils.isEmpty(scheduleExpression)) {
            ScheduleParam scheduleParam = JSONUtils.parseObject(scheduleExpression, ScheduleParam.class);
            if (scheduleParam == null) {
                putMsg(result, Status.PARSE_TO_CRON_EXPRESSION_ERROR);
                return;
            }
            if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
                logger.warn("The start time must not be the same as the end");
                putMsg(result, Status.SCHEDULE_START_TIME_END_TIME_SAME);
                return;
            }
            if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
                logger.warn("The start time must smaller than end time");
                putMsg(result, Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
                return;
            }

            schedule.setStartTime(scheduleParam.getStartTime());
            schedule.setEndTime(scheduleParam.getEndTime());
            if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
                logger.error("Schedule crontab verify failure, crontab:{}.", scheduleParam.getCrontab());
                putMsg(result, Status.SCHEDULE_CRON_CHECK_FAILED, scheduleParam.getCrontab());
                return;
            }
            schedule.setCrontab(scheduleParam.getCrontab());
            schedule.setTimezoneId(scheduleParam.getTimezoneId());
        }

        if (warningType != null) {
            schedule.setWarningType(warningType);
        }

        schedule.setWarningGroupId(warningGroupId);

        if (failureStrategy != null) {
            schedule.setFailureStrategy(failureStrategy);
        }

        schedule.setWorkerGroup(workerGroup);
        schedule.setEnvironmentCode(environmentCode);
        schedule.setUpdateTime(now);
        schedule.setProcessInstancePriority(processInstancePriority);
        scheduleMapper.updateById(schedule);

        processDefinition.setWarningGroupId(warningGroupId);

        processDefinitionMapper.updateById(processDefinition);

        putMsg(result, Status.SUCCESS);
    }
}
