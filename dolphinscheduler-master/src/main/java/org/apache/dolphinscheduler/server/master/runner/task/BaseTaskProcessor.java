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

package org.apache.dolphinscheduler.server.master.runner.task;

import static org.apache.dolphinscheduler.common.constants.Constants.ADDRESS;
import static org.apache.dolphinscheduler.common.constants.Constants.DATABASE;
import static org.apache.dolphinscheduler.common.constants.Constants.JDBC_URL;
import static org.apache.dolphinscheduler.common.constants.Constants.OTHER;
import static org.apache.dolphinscheduler.common.constants.Constants.PASSWORD;
import static org.apache.dolphinscheduler.common.constants.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.constants.Constants.USER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DATA_QUALITY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_K8S;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_DATASOURCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TARGET_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TARGET_DATASOURCE_ID;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ConnectorType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.model.JdbcInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.K8sTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.DataQualityParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.UdfFuncParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.JdbcUrlParser;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.server.master.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.storage.impl.HadoopUtils;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.apache.dolphinscheduler.spi.plugin.SPIIdentify;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

/**
 * 任务处理器的抽象基类，实现了 {@link ITaskProcessor} 接口的通用逻辑。
 * 提供任务提交、运行、分发、暂停、停止和超时处理的标准流程框架，
 * 同时封装了任务执行上下文的构建、数据质量任务配置、K8s 任务配置以及资源信息解析等公共功能。
 * 具体的任务类型处理器需继承此类并实现对应的抽象方法。
 */
public abstract class BaseTaskProcessor implements ITaskProcessor {

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));

    protected boolean killed = false;

    protected boolean paused = false;

    protected boolean timeout = false;

    protected TaskInstance taskInstance = null;

    protected ProcessInstance processInstance;

    protected int maxRetryTimes;

    protected long commitInterval;

    protected ProcessService processService;

    protected ProcessInstanceDao processInstanceDao;

    protected MasterConfig masterConfig;

    protected TaskPluginManager taskPluginManager;

    protected CuringParamsService curingParamsService;

    protected String threadLoggerInfoName;

    @Override
    public void init(@NonNull TaskInstance taskInstance, @NonNull ProcessInstance processInstance) {
        processService = SpringApplicationContext.getBean(ProcessService.class);
        processInstanceDao = SpringApplicationContext.getBean(ProcessInstanceDao.class);
        masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
        taskPluginManager = SpringApplicationContext.getBean(TaskPluginManager.class);
        curingParamsService = SpringApplicationContext.getBean(CuringParamsService.class);
        this.taskInstance = taskInstance;
        this.processInstance = processInstance;
        this.maxRetryTimes = masterConfig.getTaskCommitRetryTimes();
        this.commitInterval = masterConfig.getTaskCommitInterval().toMillis();
    }

    protected javax.sql.DataSource defaultDataSource =
            SpringApplicationContext.getBean(javax.sql.DataSource.class);

    /**
     * 暂停任务，子类实现具体的暂停逻辑。
     *
     * @return 是否暂停成功
     */
    protected abstract boolean pauseTask();

    /**
     * 终止任务，子类实现具体的终止逻辑。
     *
     * @return 是否终止成功
     */
    protected abstract boolean killTask();

    /**
     * 任务超时处理，子类实现具体的超时逻辑。
     *
     * @return 是否超时处理成功
     */
    protected abstract boolean taskTimeout();

    /**
     * 提交任务，子类实现具体的提交逻辑。
     *
     * @return 是否提交成功
     */
    protected abstract boolean submitTask();

    /*
     * 重新提交任务，子类实现具体的重提交逻辑。
     */
    protected abstract boolean resubmitTask();

    /**
     * 运行任务，子类实现具体的运行逻辑。
     *
     * @return 是否运行成功
     */
    protected abstract boolean runTask();

    /**
     * 分发任务到 Worker 执行，子类实现具体的分发逻辑。
     *
     * @return 是否分发成功
     */
    protected abstract boolean dispatchTask();

    /**
     * 根据指定的任务动作执行对应的操作，是任务状态变更的统一入口。
     *
     * @param taskAction 任务动作（SUBMIT, RUN, DISPATCH, PAUSE, STOP, TIMEOUT, RESUBMIT）
     * @return 是否执行成功
     */
    @Override
    public boolean action(TaskAction taskAction) {
        String threadName = Thread.currentThread().getName();
        if (StringUtils.isNotEmpty(threadLoggerInfoName)) {
            Thread.currentThread().setName(threadLoggerInfoName);
        }
        boolean result = false;
        try {
            switch (taskAction) {
                case STOP:
                    result = stop();
                    break;
                case PAUSE:
                    result = pause();
                    break;
                case TIMEOUT:
                    result = timeout();
                    break;
                case SUBMIT:
                    result = submit();
                    break;
                case RUN:
                    result = run();
                    break;
                case DISPATCH:
                    result = dispatch();
                    break;
                case RESUBMIT:
                    result = resubmit();
                    break;
                default:
                    logger.error("unknown task action: {}", taskAction);
            }
            return result;
        } finally {
            // reset thread name
            Thread.currentThread().setName(threadName);

        }
    }

    protected boolean resubmit() {
        return resubmitTask();
    }

    protected boolean submit() {
        return submitTask();
    }

    protected boolean run() {
        return runTask();
    }

    protected boolean dispatch() {
        return dispatchTask();
    }

    protected boolean timeout() {
        if (timeout) {
            return true;
        }
        timeout = taskTimeout();
        return timeout;
    }

    protected boolean pause() {
        if (paused) {
            return true;
        }
        paused = pauseTask();
        return paused;
    }

    protected boolean stop() {
        if (killed) {
            return true;
        }
        killed = killTask();
        return killed;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("This abstract class doesn's has type");
    }

    @Override
    public SPIIdentify getIdentify() {
        return SPIIdentify.builder().name(getType()).build();
    }

    @Override
    public TaskInstance taskInstance() {
        return this.taskInstance;
    }

    /**
     * 设置 Master 任务运行日志名称，将线程名格式化为包含任务标识信息的日志前缀。
     */
    public void setTaskExecutionLogger() {
        threadLoggerInfoName = LoggerUtils.buildTaskId(taskInstance.getFirstSubmitTime(),
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId());
        Thread.currentThread().setName(threadLoggerInfoName);
    }

    /**
     * 构建任务执行上下文，包括租户、队列、资源、参数等信息。
     *
     * @param taskInstance 任务实例
     * @return 任务执行上下文，若租户校验失败则返回 null
     */
    protected TaskExecutionContext getTaskExecutionContext(TaskInstance taskInstance) {
        int userId = taskInstance.getProcessDefine() == null ? 0 : taskInstance.getProcessDefine().getUserId();
        Tenant tenant = processService.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(), userId);

        // verify tenant is null
        if (verifyTenantIsNull(tenant, taskInstance)) {
            logger.info("Task state changes to {}", TaskExecutionStatus.FAILURE);
            taskInstance.setState(TaskExecutionStatus.FAILURE);
            processService.saveTaskInstance(taskInstance);
            return null;
        }
        // set queue for process instance, user-specified queue takes precedence over tenant queue
        String userQueue = processService.queryUserQueueByProcessInstance(taskInstance.getProcessInstance());
        taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
        taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());
        taskInstance.setResources(getResourceFullNames(taskInstance));

        TaskChannel taskChannel = taskPluginManager.getTaskChannel(taskInstance.getTaskType());
        ResourceParametersHelper resources = taskChannel.getResources(taskInstance.getTaskParams());
        this.setTaskResourceInfo(resources);

        // TODO to be optimized
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();
        if (TASK_TYPE_DATA_QUALITY.equalsIgnoreCase(taskInstance.getTaskType())) {
            setDataQualityTaskRelation(dataQualityTaskExecutionContext, taskInstance, tenant.getTenantCode());
        }
        K8sTaskExecutionContext k8sTaskExecutionContext = new K8sTaskExecutionContext();
        if (TASK_TYPE_K8S.equalsIgnoreCase(taskInstance.getTaskType())) {
            setK8sTaskRelation(k8sTaskExecutionContext, taskInstance);
        }

        Map<String, Property> businessParamsMap = curingParamsService.preBuildBusinessParams(processInstance);

        AbstractParameters baseParam = taskPluginManager.getParameters(ParametersNode.builder()
                .taskType(taskInstance.getTaskType()).taskParams(taskInstance.getTaskParams()).build());
        Map<String, Property> propertyMap =
                curingParamsService.paramParsingPreparation(taskInstance, baseParam, processInstance);
        return TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildTaskDefinitionRelatedInfo(taskInstance.getTaskDefine())
                .buildProcessInstanceRelatedInfo(taskInstance.getProcessInstance())
                .buildProcessDefinitionRelatedInfo(taskInstance.getProcessDefine())
                .buildResourceParametersInfo(resources)
                .buildDataQualityTaskExecutionContext(dataQualityTaskExecutionContext)
                .buildK8sTaskRelatedInfo(k8sTaskExecutionContext)
                .buildBusinessParamsMap(businessParamsMap)
                .buildParamInfo(propertyMap)
                .create();
    }

    /**
     * 设置任务资源信息，包括数据源资源和 UDF 函数资源。
     *
     * @param resourceParametersHelper 资源参数帮助类
     */
    public void setTaskResourceInfo(ResourceParametersHelper resourceParametersHelper) {
        if (Objects.isNull(resourceParametersHelper)) {
            return;
        }
        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            switch (type) {
                case DATASOURCE:
                    this.setTaskDataSourceResourceInfo(map);
                    break;
                case UDF:
                    this.setTaskUdfFuncResourceInfo(map);
                    break;
                default:
                    break;
            }
        });
    }

    private void setTaskDataSourceResourceInfo(Map<Integer, AbstractResourceParameters> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        map.forEach((code, parameters) -> {
            DataSource datasource = processService.findDataSourceById(code);
            if (Objects.isNull(datasource)) {
                return;
            }
            DataSourceParameters dataSourceParameters = new DataSourceParameters();
            dataSourceParameters.setType(datasource.getType());
            dataSourceParameters.setConnectionParams(datasource.getConnectionParams());
            map.put(code, dataSourceParameters);
        });

    }

    private void setTaskUdfFuncResourceInfo(Map<Integer, AbstractResourceParameters> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        List<UdfFunc> udfFuncList = processService.queryUdfFunListByIds(map.keySet().toArray(new Integer[map.size()]));

        udfFuncList.forEach(udfFunc -> {
            UdfFuncParameters udfFuncParameters =
                    JSONUtils.parseObject(JSONUtils.toJsonString(udfFunc), UdfFuncParameters.class);
            udfFuncParameters.setDefaultFS(HadoopUtils.getInstance().getDefaultFS());
            String tenantCode = processService.queryTenantCodeByResName(udfFunc.getResourceName(), ResourceType.UDF);
            udfFuncParameters.setTenantCode(tenantCode);
            map.put(udfFunc.getId(), udfFuncParameters);
        });
    }

    /**
     * 设置数据质量任务的相关配置，包括规则输入、执行 SQL、源/目标数据源配置等。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     * @param taskInstance                    任务实例
     * @param tenantCode                      租户编码
     */
    private void setDataQualityTaskRelation(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                            TaskInstance taskInstance, String tenantCode) {
        DataQualityParameters dataQualityParameters =
                JSONUtils.parseObject(taskInstance.getTaskParams(), DataQualityParameters.class);
        if (dataQualityParameters == null) {
            return;
        }

        Map<String, String> config = dataQualityParameters.getRuleInputParameter();

        int ruleId = dataQualityParameters.getRuleId();
        DqRule dqRule = processService.getDqRule(ruleId);
        if (dqRule == null) {
            logger.error("Can not get dataQuality rule by id {}", ruleId);
            return;
        }

        dataQualityTaskExecutionContext.setRuleId(ruleId);
        dataQualityTaskExecutionContext.setRuleType(dqRule.getType());
        dataQualityTaskExecutionContext.setRuleName(dqRule.getName());

        List<DqRuleInputEntry> ruleInputEntryList = processService.getRuleInputEntry(ruleId);
        if (CollectionUtils.isEmpty(ruleInputEntryList)) {
            logger.error("Rule input entry list is empty, ruleId: {}", ruleId);
            return;
        }
        List<DqRuleExecuteSql> executeSqlList = processService.getDqExecuteSql(ruleId);
        setComparisonParams(dataQualityTaskExecutionContext, config, ruleInputEntryList, executeSqlList);
        dataQualityTaskExecutionContext.setRuleInputEntryList(JSONUtils.toJsonString(ruleInputEntryList));
        dataQualityTaskExecutionContext.setExecuteSqlList(JSONUtils.toJsonString(executeSqlList));

        // set the path used to store data quality task check error data
        dataQualityTaskExecutionContext.setHdfsPath(
                PropertyUtils.getString(Constants.FS_DEFAULT_FS)
                        + PropertyUtils.getString(
                                Constants.DATA_QUALITY_ERROR_OUTPUT_PATH,
                                "/user/" + tenantCode + "/data_quality_error_data"));

        setSourceConfig(dataQualityTaskExecutionContext, config);
        setTargetConfig(dataQualityTaskExecutionContext, config);
        setWriterConfig(dataQualityTaskExecutionContext);
        setStatisticsValueWriterConfig(dataQualityTaskExecutionContext);
    }

    /**
     * 设置比较参数，包含比较名称、比较表和执行 SQL。
     * 当比较类型为固定值（fixed_value，id=1）时，参数为空。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     * @param config                          规则输入参数
     * @param ruleInputEntryList              规则输入条目列表
     * @param executeSqlList                  执行 SQL 列表
     */
    private void setComparisonParams(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                     Map<String, String> config,
                                     List<DqRuleInputEntry> ruleInputEntryList,
                                     List<DqRuleExecuteSql> executeSqlList) {
        if (config.get(COMPARISON_TYPE) != null) {
            int comparisonTypeId = Integer.parseInt(config.get(COMPARISON_TYPE));
            // comparison type id 1 is fixed value ,do not need set param
            if (comparisonTypeId > 1) {
                DqComparisonType type = processService.getComparisonTypeById(comparisonTypeId);
                if (type != null) {
                    DqRuleInputEntry comparisonName = new DqRuleInputEntry();
                    comparisonName.setField(COMPARISON_NAME);
                    comparisonName.setValue(type.getName());
                    ruleInputEntryList.add(comparisonName);

                    DqRuleInputEntry comparisonTable = new DqRuleInputEntry();
                    comparisonTable.setField(COMPARISON_TABLE);
                    comparisonTable.setValue(type.getOutputTable());
                    ruleInputEntryList.add(comparisonTable);

                    if (executeSqlList == null) {
                        executeSqlList = new ArrayList<>();
                    }

                    DqRuleExecuteSql dqRuleExecuteSql = new DqRuleExecuteSql();
                    dqRuleExecuteSql.setType(ExecuteSqlType.MIDDLE.getCode());
                    dqRuleExecuteSql.setIndex(1);
                    dqRuleExecuteSql.setSql(type.getExecuteSql());
                    dqRuleExecuteSql.setTableAlias(type.getOutputTable());
                    executeSqlList.add(0, dqRuleExecuteSql);

                    if (Boolean.TRUE.equals(type.getIsInnerSource())) {
                        dataQualityTaskExecutionContext.setComparisonNeedStatisticsValueTable(true);
                    }
                }
            } else if (comparisonTypeId == 1) {
                dataQualityTaskExecutionContext.setCompareWithFixedValue(true);
            }
        }
    }

    /**
     * 获取默认数据源（DolphinScheduler 自身的数据源），用于 StatisticsValueConfig 和 WriterConfig。
     *
     * @return 默认数据源配置
     */
    public DataSource getDefaultDataSource() {
        DataSource dataSource = new DataSource();

        HikariDataSource hikariDataSource = (HikariDataSource) defaultDataSource;
        dataSource.setUserName(hikariDataSource.getUsername());
        JdbcInfo jdbcInfo = JdbcUrlParser.getJdbcInfo(hikariDataSource.getJdbcUrl());
        if (jdbcInfo != null) {
            Properties properties = new Properties();
            properties.setProperty(USER, hikariDataSource.getUsername());
            properties.setProperty(PASSWORD, hikariDataSource.getPassword());
            properties.setProperty(DATABASE, jdbcInfo.getDatabase());
            properties.setProperty(ADDRESS, jdbcInfo.getAddress());
            properties.setProperty(OTHER, jdbcInfo.getParams());
            properties.setProperty(JDBC_URL, jdbcInfo.getAddress() + SINGLE_SLASH + jdbcInfo.getDatabase());
            dataSource.setType(DbType.of(JdbcUrlParser.getDbType(jdbcInfo.getDriverName()).getCode()));
            dataSource.setConnectionParams(JSONUtils.toJsonString(properties));
        }

        return dataSource;
    }

    /**
     * 设置统计值写入配置，用于 DataQualityApplication 将统计值写入 DolphinScheduler 数据源。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     */
    private void setStatisticsValueWriterConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DataSource dataSource = getDefaultDataSource();
        ConnectorType writerConnectorType = ConnectorType.of(dataSource.getType().isHive() ? 1 : 0);
        dataQualityTaskExecutionContext.setStatisticsValueConnectorType(writerConnectorType.getDescription());
        dataQualityTaskExecutionContext.setStatisticsValueType(dataSource.getType().getCode());
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(dataSource.getConnectionParams());
        dataQualityTaskExecutionContext.setStatisticsValueTable("t_ds_dq_task_statistics_value");
    }

    /**
     * 设置写入配置，用于 DataQualityApplication 将数据质量检查结果写入 DolphinScheduler 数据源。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     */
    private void setWriterConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DataSource dataSource = getDefaultDataSource();
        ConnectorType writerConnectorType = ConnectorType.of(dataSource.getType().isHive() ? 1 : 0);
        dataQualityTaskExecutionContext.setWriterConnectorType(writerConnectorType.getDescription());
        dataQualityTaskExecutionContext.setWriterType(dataSource.getType().getCode());
        dataQualityTaskExecutionContext.setWriterConnectionParams(dataSource.getConnectionParams());
        dataQualityTaskExecutionContext.setWriterTable("t_ds_dq_execute_result");
    }

    /**
     * 设置目标端配置，用于 DataQualityApplication 获取与源值进行比较的数据。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     * @param config                          规则输入参数
     */
    private void setTargetConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                 Map<String, String> config) {
        if (StringUtils.isNotEmpty(config.get(TARGET_DATASOURCE_ID))) {
            DataSource dataSource =
                    processService.findDataSourceById(Integer.parseInt(config.get(TARGET_DATASOURCE_ID)));
            if (dataSource != null) {
                ConnectorType targetConnectorType = ConnectorType.of(
                        DbType.of(Integer.parseInt(config.get(TARGET_CONNECTOR_TYPE))).isHive() ? 1 : 0);
                dataQualityTaskExecutionContext.setTargetConnectorType(targetConnectorType.getDescription());
                dataQualityTaskExecutionContext.setTargetType(dataSource.getType().getCode());
                dataQualityTaskExecutionContext.setTargetConnectionParams(dataSource.getConnectionParams());
            }
        }
    }

    /**
     * 设置源端配置，用于 DataQualityApplication 获取统计数据。
     *
     * @param dataQualityTaskExecutionContext 数据质量任务执行上下文
     * @param config                          规则输入参数
     */
    private void setSourceConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                 Map<String, String> config) {
        if (StringUtils.isNotEmpty(config.get(SRC_DATASOURCE_ID))) {
            DataSource dataSource = processService.findDataSourceById(Integer.parseInt(config.get(SRC_DATASOURCE_ID)));
            if (dataSource != null) {
                ConnectorType srcConnectorType = ConnectorType.of(
                        DbType.of(Integer.parseInt(config.get(SRC_CONNECTOR_TYPE))).isHive() ? 1 : 0);
                dataQualityTaskExecutionContext.setSourceConnectorType(srcConnectorType.getDescription());
                dataQualityTaskExecutionContext.setSourceType(dataSource.getType().getCode());
                dataQualityTaskExecutionContext.setSourceConnectionParams(dataSource.getConnectionParams());
            }
        }
    }

    /**
     * 校验租户是否为空。
     *
     * @param tenant       租户
     * @param taskInstance 任务实例
     * @return 如果租户为空返回 true
     */
    protected boolean verifyTenantIsNull(Tenant tenant, TaskInstance taskInstance) {
        if (tenant == null) {
            logger.error("Tenant does not exists");
            return true;
        }
        return false;
    }

    /**
     * 获取资源全名到租户编码的映射。
     *
     * @param taskInstance 任务实例
     * @return key 为资源全名，value 为租户编码的映射
     */
    public Map<String, String> getResourceFullNames(TaskInstance taskInstance) {
        Map<String, String> resourcesMap = new HashMap<>();
        AbstractParameters baseParam = taskPluginManager.getParameters(ParametersNode.builder()
                .taskType(taskInstance.getTaskType()).taskParams(taskInstance.getTaskParams()).build());
        if (baseParam != null) {
            List<ResourceInfo> projectResourceFiles = baseParam.getResourceFilesList();
            if (CollectionUtils.isNotEmpty(projectResourceFiles)) {

                // filter the resources that the resource id equals 0
                Set<ResourceInfo> oldVersionResources =
                        projectResourceFiles.stream().filter(t -> t.getId() == null).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(oldVersionResources)) {
                    oldVersionResources.forEach(t -> resourcesMap.put(t.getRes(),
                            processService.queryTenantCodeByResName(t.getRes(), ResourceType.FILE)));
                }

                // get the resource id in order to get the resource names in batch
                Stream<Integer> resourceIdStream = projectResourceFiles.stream().map(ResourceInfo::getId);
                Set<Integer> resourceIdsSet = resourceIdStream.collect(Collectors.toSet());

                if (CollectionUtils.isNotEmpty(resourceIdsSet)) {
                    Integer[] resourceIds = resourceIdsSet.toArray(new Integer[resourceIdsSet.size()]);

                    List<Resource> resources = processService.listResourceByIds(resourceIds);
                    resources.forEach(t -> resourcesMap.put(t.getFullName(),
                            processService.queryTenantCodeByResName(t.getFullName(), ResourceType.FILE)));
                }
            }
        }

        return resourcesMap;
    }

    /**
     * 设置 K8s 任务相关配置，包括命名空间和 ConfigMap YAML 配置。
     *
     * @param k8sTaskExecutionContext K8s 任务执行上下文
     * @param taskInstance            任务实例
     */
    private void setK8sTaskRelation(K8sTaskExecutionContext k8sTaskExecutionContext, TaskInstance taskInstance) {
        K8sTaskParameters k8sTaskParameters =
                JSONUtils.parseObject(taskInstance.getTaskParams(), K8sTaskParameters.class);
        Map<String, String> namespace = JSONUtils.toMap(k8sTaskParameters.getNamespace());
        String clusterName = namespace.get(CLUSTER);
        String configYaml = processService.findConfigYamlByName(clusterName);
        if (configYaml != null) {
            k8sTaskExecutionContext.setConfigYaml(configYaml);
        }
    }
}
