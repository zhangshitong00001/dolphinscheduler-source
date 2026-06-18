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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskKillRequestCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.apache.dolphinscheduler.service.utils.ProcessUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 * Master 故障转移服务，负责在 Master 节点宕机后接管其工作流实例和任务实例。
 * 包括：识别需要故障转移的 Master、深检查工作流实例是否需要转移、
 * 对任务实例进行故障转移（终止 Yarn 任务、发送 Kill 命令、设置 NEED_FAULT_TOLERANCE 状态），
 * 以及清理原 Master 的注册信息。
 */
@Service
public class MasterFailoverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterFailoverService.class);
    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final String localAddress;

    private final NettyExecutorManager nettyExecutorManager;

    private final ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private final LogClient logClient;

    public MasterFailoverService(@NonNull RegistryClient registryClient,
                                 @NonNull MasterConfig masterConfig,
                                 @NonNull ProcessService processService,
                                 @NonNull NettyExecutorManager nettyExecutorManager,
                                 @NonNull ProcessInstanceExecCacheManager processInstanceExecCacheManager,
                                 @NonNull LogClient logClient) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.nettyExecutorManager = nettyExecutorManager;
        this.localAddress = masterConfig.getMasterAddress();
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
        this.logClient = logClient;

    }

    /**
     * 检查是否有 Master 需要故障转移，筛选出已宕机或属于本机的待转移 Master 列表。
     */
    @Counted(value = "ds.master.scheduler.failover.check.count")
    @Timed(value = "ds.master.scheduler.failover.check.time", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    public void checkMasterFailover() {
        List<String> needFailoverMasterHosts = processService.queryNeedFailoverProcessInstanceHost()
                .stream()
                // failover myself || dead server
                .filter(host -> localAddress.equals(host) || !registryClient.checkNodeExists(host, NodeType.MASTER))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(needFailoverMasterHosts)) {
            return;
        }
        LOGGER.info("Master failover service {} begin to failover hosts:{}", localAddress, needFailoverMasterHosts);

        for (String needFailoverMasterHost : needFailoverMasterHosts) {
            failoverMaster(needFailoverMasterHost);
        }
    }

    /**
     * 执行指定 Master 的故障转移，获取分布式锁后调用 doFailoverMaster。
     *
     * @param masterHost 故障 Master 的主机地址
     */
    public void failoverMaster(String masterHost) {
        String failoverPath = Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS + "/" + masterHost;
        try {
            registryClient.getLock(failoverPath);
            doFailoverMaster(masterHost);
        } catch (Exception e) {
            LOGGER.error("Master server failover failed, host:{}", masterHost, e);
        } finally {
            registryClient.releaseLock(failoverPath);
        }
    }

    /**
     * 执行 Master 故障转移的核心逻辑，遍历并故障转移属于该 Master 的工作流及其关联任务。
     * 只有在启用了 Master 启动时间校验的情况下，才会判断 processInstance 的重启时间是否在 Master 启动时间之后。
     *
     * @param masterHost 故障 Master 的主机地址
     */
    private void doFailoverMaster(@NonNull String masterHost) {
        StopWatch failoverTimeCost = StopWatch.createStarted();

        Optional<Date> masterStartupTimeOptional = getServerStartupTime(registryClient.getServerList(NodeType.MASTER),
                masterHost);
        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(
                masterHost);
        if (CollectionUtils.isEmpty(needFailoverProcessInstanceList)) {
            return;
        }

        LOGGER.info(
                "Master[{}] failover starting there are {} workflowInstance may need to failover, will do a deep check, workflowInstanceIds: {}",
                masterHost,
                needFailoverProcessInstanceList.size(),
                needFailoverProcessInstanceList.stream().map(ProcessInstance::getId).collect(Collectors.toList()));

        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            try {
                LoggerUtils.setWorkflowInstanceIdMDC(processInstance.getId());
                LOGGER.info("WorkflowInstance failover starting");
                if (!checkProcessInstanceNeedFailover(masterStartupTimeOptional, processInstance)) {
                    LOGGER.info("WorkflowInstance doesn't need to failover");
                    continue;
                }
                // todo: use batch query
                ProcessDefinition processDefinition =
                        processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                                processInstance.getProcessDefinitionVersion());
                processInstance.setProcessDefinition(processDefinition);
                int processInstanceId = processInstance.getId();
                List<TaskInstance> taskInstanceList =
                        processService.findValidTaskListByProcessId(processInstanceId);
                for (TaskInstance taskInstance : taskInstanceList) {
                    try {
                        LoggerUtils.setTaskInstanceIdMDC(taskInstance.getId());
                        LOGGER.info("TaskInstance failover starting");
                        if (!checkTaskInstanceNeedFailover(taskInstance)) {
                            LOGGER.info("The taskInstance doesn't need to failover");
                            continue;
                        }
                        failoverTaskInstance(processInstance, taskInstance);
                        LOGGER.info("TaskInstance failover finished");
                    } finally {
                        LoggerUtils.removeTaskInstanceIdMDC();
                    }
                }

                ProcessInstanceMetrics.incProcessInstanceByState("failover");
                // updateProcessInstance host is null to mark this processInstance has been failover
                // and insert a failover command
                processInstance.setHost(Constants.NULL);
                processService.processNeedFailoverProcessInstances(processInstance);
                LOGGER.info("WorkflowInstance failover finished");
            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }

        failoverTimeCost.stop();
        LOGGER.info("Master[{}] failover finished, useTime:{}ms",
                masterHost,
                failoverTimeCost.getTime(TimeUnit.MILLISECONDS));
    }

    private Optional<Date> getServerStartupTime(List<Server> servers, String host) {
        if (CollectionUtils.isEmpty(servers)) {
            return Optional.empty();
        }
        Date serverStartupTime = null;
        for (Server server : servers) {
            if (host.equals(server.getHost() + Constants.COLON + server.getPort())) {
                serverStartupTime = server.getCreateTime();
                break;
            }
        }
        return Optional.ofNullable(serverStartupTime);
    }

    /**
     * 执行单个任务实例的故障转移：
     * 1. 如果非 Master 本地任务且在 Worker 上运行，终止相关 Yarn 任务
     * 2. 将任务状态改为 NEED_FAULT_TOLERANCE
     * 3. 向 Worker 发送 Kill 命令
     *
     * @param processInstance 工作流实例
     * @param taskInstance    任务实例
     */
    private void failoverTaskInstance(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskMetrics.incTaskInstanceByState("failover");
        boolean isMasterTask = TaskProcessorFactory.isMasterTask(taskInstance.getTaskType());

        taskInstance.setProcessInstance(processInstance);

        if (!isMasterTask) {
            LOGGER.info("The failover taskInstance is not master task");
            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                    .buildTaskInstanceRelatedInfo(taskInstance)
                    .buildProcessInstanceRelatedInfo(processInstance)
                    .buildProcessDefinitionRelatedInfo(processInstance.getProcessDefinition())
                    .create();

            if (masterConfig.isKillYarnJobWhenTaskFailover()) {
                // only kill yarn job if exists , the local thread has exited
                LOGGER.info("TaskInstance failover begin kill the task related yarn job");
                ProcessUtils.killYarnJob(logClient, taskExecutionContext);
            }
            // kill worker task, When the master failover and worker failover happened in the same time,
            // the task may not be failover if we don't set NEED_FAULT_TOLERANCE.
            // This can be improved if we can load all task when cache a workflowInstance in memory
            sendKillCommandToWorker(taskInstance);
        } else {
            LOGGER.info("The failover taskInstance is a master task");
        }

        taskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
        processService.saveTaskInstance(taskInstance);
    }

    private void sendKillCommandToWorker(@NonNull TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            return;
        }
        try {
            TaskKillRequestCommand killCommand = new TaskKillRequestCommand(taskInstance.getId());
            Host workerHost = Host.of(taskInstance.getHost());
            nettyExecutorManager.doExecute(workerHost, killCommand.convert2Command());
            LOGGER.info("Failover task success, has killed the task in worker: {}", taskInstance.getHost());
        } catch (ExecuteException e) {
            LOGGER.error("Kill task failed", e);
        }
    }

    private boolean checkTaskInstanceNeedFailover(@NonNull TaskInstance taskInstance) {
        if (taskInstance.getState() != null && taskInstance.getState().isFinished()) {
            // The task is already finished, so we don't need to failover this task instance
            return false;
        }
        return true;
    }

    private boolean checkProcessInstanceNeedFailover(Optional<Date> beFailoveredMasterStartupTimeOptional,
                                                     @NonNull ProcessInstance processInstance) {
        // The process has already been failover, since when we do master failover we will hold a lock, so we can
        // guarantee
        // the host will not be set concurrent.
        if (Constants.NULL.equals(processInstance.getHost())) {
            return false;
        }
        if (!beFailoveredMasterStartupTimeOptional.isPresent()) {
            // the master is not active, we can failover all it's processInstance
            return true;
        }
        Date beFailoveredMasterStartupTime = beFailoveredMasterStartupTimeOptional.get();

        if (processInstance.getStartTime().after(beFailoveredMasterStartupTime)) {
            // The processInstance is newly created
            return false;
        }
        if (processInstance.getRestartTime() != null
                && processInstance.getRestartTime().after(beFailoveredMasterStartupTime)) {
            // the processInstance is already be failovered.
            return false;
        }

        if (processInstanceExecCacheManager.contains(processInstance.getId())) {
            // the processInstance is a running process instance in the current master
            return false;
        }

        return true;
    }

}
