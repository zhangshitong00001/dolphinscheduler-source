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
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Worker 故障转移服务，负责在 Worker 节点宕机后将其上运行的任务进行故障转移。
 * 查找状态为 SUBMITTED_SUCCESS/DISPATCH/RUNNING_EXECUTION/DELAY_EXECUTION/READY_PAUSE/READY_STOP
 * 且属于故障 Worker 的任务，将其状态改为 NEED_FAULT_TOLERANCE 并通过事件通知对应的 WorkflowExecuteRunnable 重新调度。
 * 仅故障转移属于当前 Master 的工作流实例中的任务。
 */
@Service
public class WorkerFailoverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerFailoverService.class);

    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final WorkflowExecuteThreadPool workflowExecuteThreadPool;
    private final ProcessInstanceExecCacheManager cacheManager;
    private final LogClient logClient;
    private final String localAddress;

    public WorkerFailoverService(@NonNull RegistryClient registryClient,
                                 @NonNull MasterConfig masterConfig,
                                 @NonNull ProcessService processService,
                                 @NonNull WorkflowExecuteThreadPool workflowExecuteThreadPool,
                                 @NonNull ProcessInstanceExecCacheManager cacheManager,
                                 @NonNull LogClient logClient) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
        this.cacheManager = cacheManager;
        this.logClient = logClient;
        this.localAddress = masterConfig.getMasterAddress();
    }

    /**
     * 执行 Worker 故障转移，查找属于故障 Worker 且需要故障转移的任务实例，逐个进行故障转移。
     * 注意：仅故障转移属于当前 Master 的工作流实例。
     *
     * @param workerHost 故障 Worker 的主机地址
     */
    public void failoverWorker(@NonNull String workerHost) {
        LOGGER.info("Worker[{}] failover starting", workerHost);
        final StopWatch failoverTimeCost = StopWatch.createStarted();

        // we query the task instance from cache, so that we can directly update the cache
        final Optional<Date> needFailoverWorkerStartTime =
                getServerStartupTime(registryClient.getServerList(NodeType.WORKER), workerHost);

        final List<TaskInstance> needFailoverTaskInstanceList = getNeedFailoverTaskInstance(workerHost);
        if (CollectionUtils.isEmpty(needFailoverTaskInstanceList)) {
            LOGGER.info("Worker[{}] failover finished there are no taskInstance need to failover", workerHost);
            return;
        }
        LOGGER.info(
                "Worker[{}] failover there are {} taskInstance may need to failover, will do a deep check, taskInstanceIds: {}",
                workerHost,
                needFailoverTaskInstanceList.size(),
                needFailoverTaskInstanceList.stream().map(TaskInstance::getId).collect(Collectors.toList()));
        final Map<Integer, ProcessInstance> processInstanceCacheMap = new HashMap<>();
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskInstance.getProcessInstanceId(), taskInstance.getId());
            try {
                ProcessInstance processInstance = processInstanceCacheMap.computeIfAbsent(
                        taskInstance.getProcessInstanceId(), k -> {
                            WorkflowExecuteRunnable workflowExecuteRunnable = cacheManager.getByProcessInstanceId(
                                    taskInstance.getProcessInstanceId());
                            if (workflowExecuteRunnable == null) {
                                return null;
                            }
                            return workflowExecuteRunnable.getProcessInstance();
                        });
                if (!checkTaskInstanceNeedFailover(needFailoverWorkerStartTime, processInstance, taskInstance)) {
                    LOGGER.info("Worker[{}] the current taskInstance doesn't need to failover", workerHost);
                    continue;
                }
                LOGGER.info(
                        "Worker[{}] failover: begin to failover taskInstance, will set the status to NEED_FAULT_TOLERANCE",
                        workerHost);
                failoverTaskInstance(processInstance, taskInstance);
                LOGGER.info("Worker[{}] failover: Finish failover taskInstance", workerHost);
            } catch (Exception ex) {
                LOGGER.info("Worker[{}] failover taskInstance occur exception", workerHost, ex);
            } finally {
                LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
        failoverTimeCost.stop();
        LOGGER.info("Worker[{}] failover finished, useTime:{}ms",
                workerHost,
                failoverTimeCost.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * 执行单个任务实例的故障转移：
     * 1. 如果非 Master 本地任务，终止相关 Yarn 任务
     * 2. 将任务状态改为 NEED_FAULT_TOLERANCE，flag 设为 NO
     * 3. 提交 TaskStateEvent 到工作流线程池，触发重新调度
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
        } else {
            LOGGER.info("The failover taskInstance is a master task");
        }

        taskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstance.setFlag(Flag.NO);
        processService.saveTaskInstance(taskInstance);

        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(processInstance.getId())
                .taskInstanceId(taskInstance.getId())
                .status(TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                .type(StateEventType.TASK_STATE_CHANGE)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    /**
     * 检查任务实例是否需要故障转移：
     * - 工作流必须属于当前 Master
     * - 任务未完成
     * - 如果 Worker 已恢复，则只故障转移在 Worker 启动前提交的任务
     *
     * @param needFailoverWorkerStartTime 故障 Worker 的启动时间
     * @param processInstance             工作流实例
     * @param taskInstance                任务实例
     * @return 是否需要故障转移
     */
    private boolean checkTaskInstanceNeedFailover(Optional<Date> needFailoverWorkerStartTime,
                                                  @Nullable ProcessInstance processInstance,
                                                  TaskInstance taskInstance) {
        if (processInstance == null) {
            // This case should be happened.
            LOGGER.error(
                    "Failover task instance error, cannot find the related processInstance form memory, this case shouldn't happened");
            return false;
        }
        if (taskInstance == null) {
            // This case should be happened.
            LOGGER.error("Master failover task instance error, taskInstance is null, this case shouldn't happened");
            return false;
        }
        // only failover the task owned myself if worker down.
        if (!StringUtils.equalsIgnoreCase(processInstance.getHost(), localAddress)) {
            LOGGER.error(
                    "Master failover task instance error, the taskInstance's processInstance's host: {} is not the current master: {}",
                    processInstance.getHost(),
                    localAddress);
            return false;
        }
        if (taskInstance.getState() != null && taskInstance.getState().isFinished()) {
            // The taskInstance is already finished, doesn't need to failover
            LOGGER.info("The task is already finished, doesn't need to failover");
            return false;
        }
        if (!needFailoverWorkerStartTime.isPresent()) {
            // The worker is still down
            return true;
        }
        // The worker is active, may already send some new task to it
        if (taskInstance.getSubmitTime() != null && taskInstance.getSubmitTime()
                .after(needFailoverWorkerStartTime.get())) {
            LOGGER.info(
                    "The taskInstance's submitTime: {} is after the need failover worker's start time: {}, the taskInstance is newly submit, it doesn't need to failover",
                    taskInstance.getSubmitTime(),
                    needFailoverWorkerStartTime.get());
            return false;
        }

        return true;
    }

    private List<TaskInstance> getNeedFailoverTaskInstance(@NonNull String failoverWorkerHost) {
        // we query the task instance from cache, so that we can directly update the cache
        return cacheManager.getAll()
                .stream()
                .flatMap(workflowExecuteRunnable -> workflowExecuteRunnable.getAllTaskInstances().stream())
                // If the worker is in dispatching and the host is not set
                .filter(taskInstance -> failoverWorkerHost.equals(taskInstance.getHost())
                        && taskInstance.getState().shouldFailover())
                .collect(Collectors.toList());
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
}
