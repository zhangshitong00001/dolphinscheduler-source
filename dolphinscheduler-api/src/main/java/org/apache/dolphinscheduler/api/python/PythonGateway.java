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

package org.apache.dolphinscheduler.api.python;

import org.apache.dolphinscheduler.api.configuration.PythonGatewayConfiguration;
import org.apache.dolphinscheduler.api.dto.EnvironmentDto;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import py4j.GatewayServer;
import py4j.GatewayServer.GatewayServerBuilder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Python网关服务。通过Py4J启动网关服务器，使Python客户端可以通过本地Java接口调用DolphinScheduler的各类服务。
 * 提供工作流、租户、用户、数据源、资源等管理操作的统一入口，支持工作流即代码（workflow-as-code）模式。
 */
@Component
public class PythonGateway {
    private static final Logger logger = LoggerFactory.getLogger(PythonGateway.class);

    private static final FailureStrategy DEFAULT_FAILURE_STRATEGY = FailureStrategy.CONTINUE;
    private static final Priority DEFAULT_PRIORITY = Priority.MEDIUM;
    private static final Long DEFAULT_ENVIRONMENT_CODE = -1L;

    private static final TaskDependType DEFAULT_TASK_DEPEND_TYPE = TaskDependType.TASK_POST;
    private static final RunMode DEFAULT_RUN_MODE = RunMode.RUN_MODE_SERIAL;
    private static final int DEFAULT_DRY_RUN = 0;
    private static final ComplementDependentMode COMPLEMENT_DEPENDENT_MODE = ComplementDependentMode.OFF_MODE;
    // We use admin user's user_id to skip some permission issue from python gateway service
    private static final int ADMIN_USER_ID = 1;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ResourcesService resourceService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private PythonGatewayConfiguration pythonGatewayConfiguration;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    // TODO replace this user to build in admin user if we make sure build in one could not be change
    private final User dummyAdminUser = new User() {
        {
            setId(ADMIN_USER_ID);
            setUserName("dummyUser");
            setUserType(UserType.ADMIN_USER);
        }
    };

    private final Queue queuePythonGateway = new Queue() {
        {
            setId(Integer.MAX_VALUE);
            setQueueName("queuePythonGateway");
        }
    };

    /**
     * 健康检查接口，用于确认网关服务是否正常运行。
     *
     * @return 固定返回 "PONG"
     */
    public String ping() {
        return "PONG";
    }

    // TODO Should we import package in python client side? utils package can but service can not, why
    // Core api
    /**
     * 批量生成任务编码列表。
     *
     * @param genNum 需要生成的编码数量
     * @return 包含生成的任务编码列表的结果Map
     */
    public Map<String, Object> genTaskCodeList(Integer genNum) {
        return taskDefinitionService.genTaskCodeList(genNum);
    }

    /**
     * 根据项目名、工作流名和任务名获取任务的编码和版本号。
     * 如果项目、工作流或任务不存在，会为其生成新的编码并返回初始版本0。
     *
     * @param projectName           项目名称
     * @param processDefinitionName 工作流名称
     * @param taskName              任务名称
     * @return 包含 code 和 version 的Map
     * @throws CodeGenerateUtils.CodeGenerateException 编码生成异常
     */
    public Map<String, Long> getCodeAndVersion(String projectName, String processDefinitionName, String taskName) throws CodeGenerateUtils.CodeGenerateException {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Long> result = new HashMap<>();
        // project do not exists, mean task not exists too, so we should directly return init value
        if (project == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
            result.put("version", 0L);
            return result;
        }

        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByDefineName(project.getCode(), processDefinitionName);
        // In the case project exists, but current workflow still not created, we should also return the init
        // version of it
        if (processDefinition == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
            result.put("version", 0L);
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(project.getCode(), processDefinition.getCode(), taskName);
        if (taskDefinition == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
            result.put("version", 0L);
        } else {
            result.put("code", taskDefinition.getCode());
            result.put("version", (long) taskDefinition.getVersion());
        }
        return result;
    }

    /**
     * 创建或更新工作流。如果工作流在项目中不存在则创建新的，如果已存在则更新。
     *
     * @param userName           操作用户名
     * @param projectName        项目名称
     * @param name               工作流名称
     * @param description        工作流描述
     * @param globalParams       全局参数
     * @param schedule           调度配置，为null时不设置调度，非null时总是刷新已有调度
     * @param onlineSchedule     是否将调度设置为在线状态
     * @param warningType        告警类型
     * @param warningGroupId     告警组ID
     * @param timeout            超时时间，运行超过该时间任务将被标记为失败
     * @param workerGroup        运行任务的Worker分组
     * @param taskRelationJson   任务节点关系JSON
     * @param taskDefinitionJson 任务定义JSON
     * @param otherParamsJson    其他参数JSON
     * @return 工作流编码
     */
    public Long createOrUpdateWorkflow(String userName,
                                       String projectName,
                                       String name,
                                       String description,
                                       String globalParams,
                                       String schedule,
                                       boolean onlineSchedule,
                                       String warningType,
                                       int warningGroupId,
                                       int timeout,
                                       String workerGroup,
                                       int releaseState,
                                       String taskRelationJson,
                                       String taskDefinitionJson,
                                       String otherParamsJson,
                                       String executionType) {
        User user = usersService.queryUser(userName);
        if (user.getTenantCode() == null) {
            throw new RuntimeException("Can not create or update workflow for user who not related to any tenant.");
        }

        Project project = projectMapper.queryByName(projectName);
        long projectCode = project.getCode();

        ProcessDefinition processDefinition = getWorkflow(user, projectCode, name);
        ProcessExecutionTypeEnum executionTypeEnum = ProcessExecutionTypeEnum.valueOf(executionType);
        long processDefinitionCode;
        // create or update workflow
        if (processDefinition != null) {
            processDefinitionCode = processDefinition.getCode();
            // make sure workflow offline which could edit
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode,
                    ReleaseState.OFFLINE);
            processDefinitionService.updateProcessDefinition(user, projectCode, name,
                    processDefinitionCode, description, globalParams,
                    null, timeout, user.getTenantCode(), taskRelationJson, taskDefinitionJson, otherParamsJson,
                    executionTypeEnum);
        } else {
            Map<String, Object> result = processDefinitionService.createProcessDefinition(user, projectCode, name,
                    description, globalParams,
                    null, timeout, user.getTenantCode(), taskRelationJson, taskDefinitionJson, otherParamsJson,
                    executionTypeEnum);
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                logger.error(result.get(Constants.MSG).toString());
                throw new ServiceException(result.get(Constants.MSG).toString());
            }
            processDefinition = (ProcessDefinition) result.get(Constants.DATA_LIST);
            processDefinitionCode = processDefinition.getCode();
        }

        // Fresh workflow schedule
        if (schedule != null) {
            createOrUpdateSchedule(user, projectCode, processDefinitionCode, schedule, onlineSchedule, workerGroup,
                    warningType,
                    warningGroupId);
        }
        processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.getEnum(releaseState));
        return processDefinitionCode;
    }

    /**
     * 根据项目编码和工作流名获取工作流定义。
     *
     * @param user         操作用户
     * @param projectCode  项目编码
     * @param workflowName 工作流名称
     * @return 找到的工作流定义，如果不存在则返回null
     */
    private ProcessDefinition getWorkflow(User user, long projectCode, String workflowName) {
        Map<String, Object> verifyProcessDefinitionExists =
                processDefinitionService.verifyProcessDefinitionName(user, projectCode, workflowName, 0);
        Status verifyStatus = (Status) verifyProcessDefinitionExists.get(Constants.STATUS);

        ProcessDefinition processDefinition = null;
        if (verifyStatus == Status.PROCESS_DEFINITION_NAME_EXIST) {
            processDefinition = processDefinitionMapper.queryByDefineName(projectCode, workflowName);
        } else if (verifyStatus != Status.SUCCESS) {
            String msg =
                    "Verify workflow exists status is invalid, neither SUCCESS or WORKFLOW_NAME_EXIST.";
            logger.error(msg);
            throw new RuntimeException(msg);
        }

        return processDefinition;
    }

    /**
     * 创建或更新工作流调度配置。如果调度不存在则创建，如果已存在则更新。
     *
     * @param user            操作用户
     * @param projectCode     项目编码
     * @param workflowCode    工作流编码
     * @param schedule        调度表达式
     * @param onlineSchedule  是否将调度设置为在线状态
     * @param workerGroup     Worker分组
     * @param warningType     告警类型
     * @param warningGroupId  告警组ID
     */
    private void createOrUpdateSchedule(User user,
                                        long projectCode,
                                        long workflowCode,
                                        String schedule,
                                        boolean onlineSchedule,
                                        String workerGroup,
                                        String warningType,
                                        int warningGroupId) {
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(workflowCode);
        // create or update schedule
        int scheduleId;
        if (scheduleObj == null) {
            processDefinitionService.releaseProcessDefinition(user, projectCode, workflowCode,
                    ReleaseState.ONLINE);
            Map<String, Object> result = schedulerService.insertSchedule(user, projectCode, workflowCode,
                    schedule, WarningType.valueOf(warningType),
                    warningGroupId, DEFAULT_FAILURE_STRATEGY, DEFAULT_PRIORITY, workerGroup, DEFAULT_ENVIRONMENT_CODE);
            scheduleId = (int) result.get("scheduleId");
        } else {
            scheduleId = scheduleObj.getId();
            processDefinitionService.releaseProcessDefinition(user, projectCode, workflowCode,
                    ReleaseState.OFFLINE);
            schedulerService.updateSchedule(user, projectCode, scheduleId, schedule, WarningType.valueOf(warningType),
                    warningGroupId, DEFAULT_FAILURE_STRATEGY, DEFAULT_PRIORITY, workerGroup, DEFAULT_ENVIRONMENT_CODE);
        }
        if (onlineSchedule) {
            // set workflow online to make sure we can set schedule online
            processDefinitionService.releaseProcessDefinition(user, projectCode, workflowCode, ReleaseState.ONLINE);
            schedulerService.setScheduleState(user, projectCode, scheduleId, ReleaseState.ONLINE);
        }
    }

    /**
     * 执行工作流实例。
     *
     * @param userName       用户名
     * @param projectName    项目名称
     * @param workflowName   工作流名称
     * @param cronTime       cron时间
     * @param workerGroup    Worker分组
     * @param warningType    告警类型
     * @param warningGroupId 告警组ID
     * @param timeout        超时时间
     */
    public void execWorkflowInstance(String userName,
                                     String projectName,
                                     String workflowName,
                                     String cronTime,
                                     String workerGroup,
                                     String warningType,
                                     Integer warningGroupId,
                                     Integer timeout) {
        User user = usersService.queryUser(userName);
        Project project = projectMapper.queryByName(projectName);
        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByDefineName(project.getCode(), workflowName);

        // make sure workflow online
        processDefinitionService.releaseProcessDefinition(user, project.getCode(), processDefinition.getCode(),
                ReleaseState.ONLINE);

        executorService.execProcessInstance(user,
                project.getCode(),
                processDefinition.getCode(),
                cronTime,
                null,
                DEFAULT_FAILURE_STRATEGY,
                null,
                DEFAULT_TASK_DEPEND_TYPE,
                WarningType.valueOf(warningType),
                warningGroupId,
                DEFAULT_RUN_MODE,
                DEFAULT_PRIORITY,
                workerGroup,
                DEFAULT_ENVIRONMENT_CODE,
                timeout,
                null,
                null,
                DEFAULT_DRY_RUN,
                COMPLEMENT_DEPENDENT_MODE
        );
    }

    // side object
    /*
     * Grant project's permission to user. Use when project's created user not current but Python API use it to change
     * workflow.
     */
    private Integer grantProjectToUser(Project project, User user) {
        Date now = new Date();
        ProjectUser projectUser = new ProjectUser();
        projectUser.setUserId(user.getId());
        projectUser.setProjectId(project.getId());
        projectUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
        projectUser.setCreateTime(now);
        projectUser.setUpdateTime(now);
        return projectUserMapper.insert(projectUser);
    }

    /**
     * 分配或创建项目。如果项目不存在则创建新项目，如果项目存在但用户无权访问则授予权限。
     *
     * @param userName 用户名
     * @param name     项目名称
     * @param desc     项目描述
     */
    public void createOrGrantProject(String userName, String name, String desc) {
        User user = usersService.queryUser(userName);

        Project project;
        project = projectMapper.queryByName(name);
        if (project == null) {
            projectService.createProject(user, name, desc);
        } else if (project.getUserId() != user.getId()) {
            ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), user.getId());
            if (projectUser == null) {
                grantProjectToUser(project, user);
            }
        }
    }

    /**
     * 根据项目名称查询项目信息。
     *
     * @param userName    用户名
     * @param projectName 项目名称
     * @return 项目实体
     */
    public Project queryProjectByName(String userName, String projectName) {
        User user = usersService.queryUser(userName);
        return (Project) projectService.queryByName(user, projectName).get(Constants.DATA_LIST);
    }

    /**
     * 更新项目信息。
     *
     * @param userName    用户名
     * @param projectCode 项目编码
     * @param projectName 新项目名称
     * @param desc        项目描述
     */
    public void updateProject(String userName, Long projectCode, String projectName, String desc) {
        User user = usersService.queryUser(userName);
        projectService.update(user, projectCode, projectName, desc, userName);
    }

    /**
     * 删除指定编码的项目。
     *
     * @param userName    用户名
     * @param projectCode 项目编码
     */
    public void deleteProject(String userName, Long projectCode) {
        User user = usersService.queryUser(userName);
        projectService.deleteProject(user, projectCode);
    }

    /**
     * 创建租户，如果租户已存在则返回已有租户。
     *
     * @param tenantCode 租户编码
     * @param desc       租户描述
     * @param queueName  队列名称
     * @return 租户实体
     */
    public Tenant createTenant(String tenantCode, String desc, String queueName) {
        return tenantService.createTenantIfNotExists(tenantCode, desc, queueName, queueName);
    }

    /**
     * 根据租户编码查询租户信息。
     *
     * @param tenantCode 租户编码
     * @return 租户实体
     */
    public Tenant queryTenantByCode(String tenantCode) {
        return (Tenant) tenantService.queryByTenantCode(tenantCode).get(Constants.DATA_LIST);
    }

    /**
     * 更新租户信息。
     *
     * @param userName   用户名
     * @param id         租户ID
     * @param tenantCode 新租户编码
     * @param queueId    队列ID
     * @param desc       租户描述
     * @throws Exception 更新失败时抛出
     */
    public void updateTenant(String userName, int id, String tenantCode, int queueId, String desc) throws Exception {
        User user = usersService.queryUser(userName);
        tenantService.updateTenant(user, id, tenantCode, queueId, desc);
    }

    /**
     * 删除指定ID的租户。
     *
     * @param userName  用户名
     * @param tenantId  租户ID
     * @throws Exception 删除失败时抛出
     */
    public void deleteTenantById(String userName, Integer tenantId) throws Exception {
        User user = usersService.queryUser(userName);
        tenantService.deleteTenantById(user, tenantId);
    }

    /**
     * 创建用户。如果用户不存在则自动创建。
     *
     * @param userName     用户名
     * @param userPassword 用户密码
     * @param email        邮箱
     * @param phone        电话
     * @param tenantCode   租户编码
     * @param queue        队列名称
     * @param state        用户状态
     * @return 用户实体
     * @throws IOException 创建失败时抛出
     */
    public User createUser(String userName,
                           String userPassword,
                           String email,
                           String phone,
                           String tenantCode,
                           String queue,
                           int state) throws IOException {
        return usersService.createUserIfNotExists(userName, userPassword, email, phone, tenantCode, queue, state);
    }

    /**
     * 根据用户ID查询用户信息。
     *
     * @param id 用户ID
     * @return 用户实体
     * @throws RuntimeException 用户不存在时抛出
     */
    public User queryUser(int id) {
        User user = usersService.queryUser(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    /**
     * 更新用户信息，如果用户不存在则自动创建。
     *
     * @param userName     用户名
     * @param userPassword 用户密码
     * @param email        邮箱
     * @param phone        电话
     * @param tenantCode   租户编码
     * @param queue        队列名称
     * @param state        用户状态
     * @return 用户实体
     * @throws Exception 操作失败时抛出
     */
    public User updateUser(String userName, String userPassword, String email, String phone, String tenantCode, String queue, int state) throws Exception {
        return usersService.createUserIfNotExists(userName, userPassword, email, phone, tenantCode, queue, state);
    }

    /**
     * 删除指定ID的用户。
     *
     * @param userName 操作用户名
     * @param id       要删除的用户ID
     * @return 操作用户实体
     * @throws Exception 删除失败时抛出
     */
    public User deleteUser(String userName, int id) throws Exception {
        User user = usersService.queryUser(userName);
        usersService.deleteUserById(user, id);
        return usersService.queryUser(userName);
    }

    /**
     * 根据数据源名称和类型获取单个数据源。如果指定了类型，则只返回匹配该类型的数据源。
     *
     * @param datasourceName 数据源名称
     * @param type           数据源类型，为null时不按类型过滤
     * @return 数据源实体
     */
    public DataSource getDatasource(String datasourceName, String type) {

        List<DataSource> dataSourceList = dataSourceMapper.queryDataSourceByName(datasourceName);
        if (dataSourceList == null || dataSourceList.isEmpty()) {
            String msg = String.format("Can not find any datasource by name %s", datasourceName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        List<DataSource> dataSourceListMatchType = dataSourceList.stream()
                .filter(dataSource -> type == null || StringUtils.equalsIgnoreCase(dataSource.getType().name(), type))
                .collect(Collectors.toList());

        logger.info("Get the datasource list match the type are: {}", dataSourceListMatchType);
        if (dataSourceListMatchType.size() > 1) {
            String msg = String.format("Get more than one datasource by name %s", datasourceName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return dataSourceListMatchType.stream().findFirst().orElseThrow(() -> {
            String msg = String.format("Can not find any datasource by name %s and type %s", datasourceName, type);
            logger.error(msg);
            return new IllegalArgumentException(msg);
        });
    }

    /**
     * 根据工作流名称获取工作流信息。返回包含工作流ID、名称和编码的Map。
     * 主要用于Python API创建子流程任务时获取工作流信息。
     *
     * @param userName     用户名
     * @param projectName  项目名称
     * @param workflowName 工作流名称
     * @return 包含 id、name 和 code 的工作流信息Map
     */
    public Map<String, Object> getWorkflowInfo(String userName, String projectName,
                                               String workflowName) {
        Map<String, Object> result = new HashMap<>();

        User user = usersService.queryUser(userName);
        Project project = (Project) projectService.queryByName(user, projectName).get(Constants.DATA_LIST);
        long projectCode = project.getCode();
        ProcessDefinition processDefinition = getWorkflow(user, projectCode, workflowName);
        // get workflow info
        if (processDefinition != null) {
            // make sure workflow online
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinition.getCode(),
                    ReleaseState.ONLINE);
            result.put("id", processDefinition.getId());
            result.put("name", processDefinition.getName());
            result.put("code", processDefinition.getCode());
        } else {
            String msg = String.format("Can not find valid workflow by name %s", workflowName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return result;
    }

    /**
     * 获取项目、工作流和任务编码信息。主要用于Python API创建依赖任务时获取工作流相关信息。
     *
     * @param projectName  项目名称
     * @param workflowName 工作流名称
     * @param taskName     任务名称，可为null
     * @return 包含 projectCode、processDefinitionCode 和可选的 taskDefinitionCode 的Map
     */
    public Map<String, Object> getDependentInfo(String projectName, String workflowName, String taskName) {
        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByName(projectName);
        if (project == null) {
            String msg = String.format("Can not find valid project by name %s", projectName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        long projectCode = project.getCode();
        result.put("projectCode", projectCode);

        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByDefineName(projectCode, workflowName);
        if (processDefinition == null) {
            String msg = String.format("Can not find valid workflow by name %s", workflowName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        result.put("processDefinitionCode", processDefinition.getCode());

        if (taskName != null) {
            TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(projectCode, processDefinition.getCode(), taskName);
            result.put("taskDefinitionCode", taskDefinition.getCode());
        }
        return result;
    }

    /**
     * 根据程序类型和资源全名获取资源文件信息。返回包含资源ID和名称的Map。
     * 主要用于Python API创建Flink或Spark任务时获取资源信息。
     *
     * @param programType 程序类型，可选值 SCALA、JAVA、PYTHON
     * @param fullName    资源的完整名称（含路径）
     * @return 包含 id 和 name 的资源信息Map
     */
    public Map<String, Object> getResourcesFileInfo(String programType, String fullName) {
        Map<String, Object> result = new HashMap<>();

        Result<Object> resources = resourceService.queryResourceByProgramType(dummyAdminUser, ResourceType.FILE, ProgramType.valueOf(programType));
        List<ResourceComponent> resourcesComponent = (List<ResourceComponent>) resources.getData();
        List<ResourceComponent> namedResources = resourcesComponent.stream().filter(s -> fullName.equals(s.getFullName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(namedResources)) {
            String msg = String.format("Can not find valid resource by program type %s and name %s", programType, fullName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        result.put("id", namedResources.get(0).getId());
        result.put("name", namedResources.get(0).getName());
        return result;
    }

    /**
     * 根据环境名称获取环境编码。主要用于Python API创建任务时获取环境信息。
     *
     * @param environmentName 环境名称
     * @return 环境编码
     */
    public Long getEnvironmentInfo(String environmentName) {
        Map<String, Object> result = environmentService.queryEnvironmentByName(environmentName);

        if (result.get("data") == null) {
            String msg = String.format("Can not find valid environment by name %s", environmentName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        EnvironmentDto environmentDto = EnvironmentDto.class.cast(result.get("data"));
        return environmentDto.getCode();
    }


    /**
     * 根据资源全名查询资源文件信息。主要用于Python API创建任务时获取资源信息。
     *
     * @param userName 用户名
     * @param fullName 资源完整名称
     * @return 资源实体
     */
    public Resource queryResourcesFileInfo(String userName, String fullName) {
        return resourceService.queryResourcesFileInfo(userName, fullName);
    }

    /**
     * 获取Python网关的版本号。
     *
     * @return 版本号字符串
     */
    public String getGatewayVersion() {
        return PythonGateway.class.getPackage().getImplementationVersion();
    }

    /**
     * 创建或更新资源文件。如果资源所在目录不存在则自动创建。
     *
     * @param userName        用户名
     * @param fullName        资源完整名称（含路径和后缀）
     * @param description     资源描述
     * @param resourceContent 资源内容
     */
    public void createOrUpdateResource(
            String userName, String fullName, String description, String resourceContent) {
        resourceService.createOrUpdateResource(userName, fullName, description, resourceContent);
    }

    /**
     * 服务初始化方法。在Spring容器启动后自动调用，当Python网关功能启用时启动网关服务。
     */
    @PostConstruct
    public void init() {
        if (pythonGatewayConfiguration.isEnabled()) {
            this.start();
        }
    }

    private void start() {
        try {
            InetAddress gatewayHost = InetAddress.getByName(pythonGatewayConfiguration.getGatewayServerAddress());
            GatewayServerBuilder serverBuilder = new GatewayServer.GatewayServerBuilder()
                    .entryPoint(this)
                    .javaAddress(gatewayHost)
                    .javaPort(pythonGatewayConfiguration.getGatewayServerPort())
                    .connectTimeout(pythonGatewayConfiguration.getConnectTimeout())
                    .readTimeout(pythonGatewayConfiguration.getReadTimeout());
            if (!StringUtils.isEmpty(pythonGatewayConfiguration.getAuthToken())) {
                serverBuilder.authToken(pythonGatewayConfiguration.getAuthToken());
            }

            GatewayServer.turnLoggingOn();
            logger.info("PythonGatewayService started on: " + gatewayHost.toString());
            serverBuilder.build().start();
        } catch (UnknownHostException e) {
            logger.error("exception occurred while constructing PythonGatewayService().", e);
        }
    }
}
