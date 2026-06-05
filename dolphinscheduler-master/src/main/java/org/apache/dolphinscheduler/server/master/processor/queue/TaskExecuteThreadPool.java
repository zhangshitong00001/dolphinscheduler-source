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

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.cache.StreamTaskInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.TaskEventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * 任务执行线程池。继承ThreadPoolTaskExecutor，管理任务事件的提交和异步执行，防止同一工作流实例的事件被并发处理。
 */
@Component
public class TaskExecuteThreadPool extends ThreadPoolTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteThreadPool.class);

    private final ConcurrentHashMap<String, TaskExecuteRunnable> multiThreadFilterMap = new ConcurrentHashMap<>();

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private List<TaskEventHandler> taskEventHandlerList;

    @Autowired
    private StreamTaskInstanceExecCacheManager streamTaskInstanceExecCacheManager;

    private Map<TaskEventType, TaskEventHandler> taskEventHandlerMap = new HashMap<>();

    /**
     * 任务事件执行线程映射表，按工作流实例ID关联对应的TaskExecuteRunnable
     */
    private final ConcurrentHashMap<Integer, TaskExecuteRunnable> taskExecuteThreadMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("Task-Execute-Thread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
        taskEventHandlerList.forEach(
            taskEventHandler -> taskEventHandlerMap.put(taskEventHandler.getHandleEventType(), taskEventHandler));
    }

    /**
     * 提交任务事件到对应工作流实例的执行队列中。
     *
     * @param taskEvent 任务事件
     */
    public void submitTaskEvent(TaskEvent taskEvent) {
        // stream task event handle
        if (taskEvent.getProcessInstanceId() == 0 && streamTaskInstanceExecCacheManager.contains(taskEvent.getTaskInstanceId())) {
            streamTaskInstanceExecCacheManager.getByTaskInstanceId(taskEvent.getTaskInstanceId()).addTaskEvent(taskEvent);
            return;
        }
        if (!processInstanceExecCacheManager.contains(taskEvent.getProcessInstanceId())) {
            logger.warn("Cannot find workflowExecuteThread from cacheManager, event: {}", taskEvent);
            return;
        }
        TaskExecuteRunnable taskExecuteRunnable = taskExecuteThreadMap.computeIfAbsent(taskEvent.getProcessInstanceId(),
            (processInstanceId) -> new TaskExecuteRunnable(processInstanceId, taskEventHandlerMap));
        taskExecuteRunnable.addEvent(taskEvent);
    }

    /**
     * 遍历所有工作流实例的事件队列，将待处理的事件提交异步执行。
     */
    public void eventHandler() {
        for (TaskExecuteRunnable taskExecuteThread : taskExecuteThreadMap.values()) {
            executeEvent(taskExecuteThread);
        }
    }

    /**
     * 异步执行指定工作流实例的任务事件。使用ListenableFuture防止同一工作流实例的重复提交。
     *
     * @param taskExecuteThread 任务执行运行单元
     */
    public void executeEvent(TaskExecuteRunnable taskExecuteThread) {
        if (taskExecuteThread.isEmpty()) {
            return;
        }
        if (multiThreadFilterMap.containsKey(taskExecuteThread.getKey())) {
            return;
        }
        multiThreadFilterMap.put(taskExecuteThread.getKey(), taskExecuteThread);
        ListenableFuture future = this.submitListenable(taskExecuteThread::run);
        future.addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                Integer processInstanceId = taskExecuteThread.getProcessInstanceId();
                logger.error("[WorkflowInstance-{}] persist event failed", processInstanceId, ex);
                if (!processInstanceExecCacheManager.contains(processInstanceId)) {
                    taskExecuteThreadMap.remove(processInstanceId);
                    logger.info("[WorkflowInstance-{}] Cannot find processInstance from cacheManager, remove process instance from threadMap",
                        processInstanceId);
                }
                multiThreadFilterMap.remove(taskExecuteThread.getKey());
            }

            @Override
            public void onSuccess(Object result) {
                Integer processInstanceId = taskExecuteThread.getProcessInstanceId();
                logger.info("[WorkflowInstance-{}] persist events succeeded", processInstanceId);
                if (!processInstanceExecCacheManager.contains(processInstanceId)) {
                    taskExecuteThreadMap.remove(processInstanceId);
                    logger.info("[WorkflowInstance-{}] Cannot find processInstance from cacheManager, remove process instance from threadMap",
                        processInstanceId);
                }
                multiThreadFilterMap.remove(taskExecuteThread.getKey());
            }
        });
    }
}
