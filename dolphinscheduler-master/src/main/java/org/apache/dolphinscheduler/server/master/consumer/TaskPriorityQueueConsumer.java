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

package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskDispatchCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务优先级队列消费者。以守护线程方式运行，不断从优先级队列中拉取任务并分发到 Worker 执行。
 */
@Component
public class TaskPriorityQueueConsumer extends BaseDaemonThread {

    private static final Logger logger = LoggerFactory.getLogger(TaskPriorityQueueConsumer.class);

    /**
     * 任务优先级队列。
     */
    @Autowired
    private TaskPriorityQueue<TaskPriority> taskPriorityQueue;

    /**
     * 流程服务。
     */
    @Autowired
    private ProcessService processService;

    /**
     * 执行器分发器。
     */
    @Autowired
    private ExecutorDispatcher dispatcher;

    /**
     * 流程实例执行缓存管理器。
     */
    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    /**
     * Master 配置。
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * 任务事件服务。
     */
    @Autowired
    private TaskEventService taskEventService;

    /**
     * 消费者线程池。
     */
    private ThreadPoolExecutor consumerThreadPoolExecutor;

    protected TaskPriorityQueueConsumer() {
        super("TaskPriorityQueueConsumeThread");
    }

    @PostConstruct
    public void init() {
        this.consumerThreadPoolExecutor = (ThreadPoolExecutor) ThreadUtils
                .newDaemonFixedThreadExecutor("TaskUpdateQueueConsumerThread", masterConfig.getDispatchTaskNumber());
        logger.info("Task priority queue consume thread staring");
        super.start();
        logger.info("Task priority queue consume thread started");
    }

    @Override
    public void run() {
        int fetchTaskNum = masterConfig.getDispatchTaskNumber();
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                List<TaskPriority> failedDispatchTasks = this.batchDispatch(fetchTaskNum);

                if (CollectionUtils.isNotEmpty(failedDispatchTasks)) {
                    logger.info("{} tasks dispatch failed, will retry to dispatch", failedDispatchTasks.size());
                    TaskMetrics.incTaskDispatchFailed(failedDispatchTasks.size());
                    for (TaskPriority dispatchFailedTask : failedDispatchTasks) {
                        taskPriorityQueue.put(dispatchFailedTask);
                    }
                    // If the all task dispatch failed, will sleep for 1s to avoid the master cpu higher.
                    if (fetchTaskNum == failedDispatchTasks.size()) {
                        logger.info("All tasks dispatch failed, will sleep a while to avoid the master cpu higher");
                        TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS);
                    }
                }
            } catch (Exception e) {
                TaskMetrics.incTaskDispatchError();
                logger.error("dispatcher task error", e);
            }
        }
    }

    /**
     * 使用线程池批量分发任务。
     *
     * @param fetchTaskNum 每次拉取的任务数量
     * @return 分发失败的任务列表
     * @throws TaskPriorityQueueException 任务优先级队列异常
     * @throws InterruptedException 线程中断异常
     */
    public List<TaskPriority> batchDispatch(int fetchTaskNum) throws TaskPriorityQueueException, InterruptedException {
        List<TaskPriority> failedDispatchTasks = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(fetchTaskNum);

        for (int i = 0; i < fetchTaskNum; i++) {
            TaskPriority taskPriority = taskPriorityQueue.poll(Constants.SLEEP_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (Objects.isNull(taskPriority)) {
                latch.countDown();
                continue;
            }

            consumerThreadPoolExecutor.submit(() -> {
                try {
                    boolean dispatchResult = this.dispatchTask(taskPriority);
                    if (!dispatchResult) {
                        failedDispatchTasks.add(taskPriority);
                    }
                } finally {
                    // make sure the latch countDown
                    latch.countDown();
                }
            });
        }

        latch.await();

        return failedDispatchTasks;
    }

    /**
     * 将任务分发到 Worker。
     *
     * @param taskPriority 任务优先级对象
     * @return 分发结果，true 表示分发成功，false 表示分发失败
     */
    protected boolean dispatchTask(TaskPriority taskPriority) {
        TaskMetrics.incTaskDispatch();
        boolean result = false;
        try {
            WorkflowExecuteRunnable workflowExecuteRunnable =
                    processInstanceExecCacheManager.getByProcessInstanceId(taskPriority.getProcessInstanceId());
            if (workflowExecuteRunnable == null) {
                logger.error("Cannot find the related processInstance of the task, taskPriority: {}", taskPriority);
                return true;
            }
            Optional<TaskInstance> taskInstanceOptional =
                    workflowExecuteRunnable.getTaskInstance(taskPriority.getTaskId());
            if (!taskInstanceOptional.isPresent()) {
                logger.error("Cannot find the task instance from related processInstance, taskPriority: {}",
                        taskPriority);
                // we return true, so that we will drop this task.
                return true;
            }
            TaskInstance taskInstance = taskInstanceOptional.get();
            TaskExecutionContext context = taskPriority.getTaskExecutionContext();
            ExecutionContext executionContext =
                    new ExecutionContext(toCommand(context), ExecutorType.WORKER, context.getWorkerGroup(),
                            taskInstance);

            if (isTaskNeedToCheck(taskPriority)) {
                if (taskInstanceIsFinalState(taskPriority.getTaskId())) {
                    // when task finish, ignore this task, there is no need to dispatch anymore
                    logger.info("Task {} is already finished, no need to dispatch, task instance id: {}",
                            taskInstance.getName(), taskInstance.getId());
                    return true;
                }
            }

            result = dispatcher.dispatch(executionContext);

            if (result) {
                logger.info("Master success dispatch task to worker, taskInstanceId: {}, worker: {}",
                        taskPriority.getTaskId(),
                        executionContext.getHost());
                addDispatchEvent(context, executionContext);
            } else {
                logger.info("Master failed to dispatch task to worker, taskInstanceId: {}, worker: {}",
                        taskPriority.getTaskId(),
                        executionContext.getHost());
            }
        } catch (RuntimeException | ExecuteException e) {
            logger.error("Master dispatch task to worker error, taskPriority: {}", taskPriority, e);
        }
        return result;
    }

    /**
     * 添加任务分发事件。
     */
    private void addDispatchEvent(TaskExecutionContext context, ExecutionContext executionContext) {
        TaskEvent taskEvent = TaskEvent.newDispatchEvent(context.getProcessInstanceId(), context.getTaskInstanceId(),
                executionContext.getHost().getAddress());
        taskEventService.addEvent(taskEvent);
    }

    private Command toCommand(TaskExecutionContext taskExecutionContext) {
        // todo: we didn't set the host here, since right now we didn't need to retry this message.
        TaskDispatchCommand requestCommand = new TaskDispatchCommand(taskExecutionContext,
                masterConfig.getMasterAddress(),
                taskExecutionContext.getHost(),
                System.currentTimeMillis());
        return requestCommand.convert2Command();
    }

    /**
     * 判断任务实例是否为终态。success、failure、kill、stop、pause、threadwaiting 均为终态。
     *
     * @param taskInstanceId 任务实例 ID
     * @return true 表示任务实例处于终态
     */
    public boolean taskInstanceIsFinalState(int taskInstanceId) {
        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceId);
        return taskInstance.getState().isFinished();
    }

    /**
     * 检查任务是否需要检查状态，若需要则刷新检查点时间。
     */
    private boolean isTaskNeedToCheck(TaskPriority taskPriority) {
        long now = System.currentTimeMillis();
        if (now - taskPriority.getCheckpoint() > Constants.SECOND_TIME_MILLIS) {
            taskPriority.setCheckpoint(now);
            return true;
        }
        return false;
    }
}
