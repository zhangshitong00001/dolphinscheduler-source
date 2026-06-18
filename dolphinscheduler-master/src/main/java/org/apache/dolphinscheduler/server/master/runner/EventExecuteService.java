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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.cache.StreamTaskInstanceExecCacheManager;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事件执行服务，负责轮询所有已缓存的工作流实例和流任务实例并触发其事件处理。
 * 作为 Master 的事件驱动引擎，定期检查内存中的工作流和流任务，将事件分发给对应的线程池执行。
 */
@Service
public class EventExecuteService extends BaseDaemonThread {

    private static final Logger logger = LoggerFactory.getLogger(EventExecuteService.class);

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StreamTaskInstanceExecCacheManager streamTaskInstanceExecCacheManager;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private StreamTaskExecuteThreadPool streamTaskExecuteThreadPool;

    protected EventExecuteService() {
        super("EventServiceStarted");
    }

    @Override
    public synchronized void start() {
        logger.info("Master Event execute service starting");
        super.start();
        logger.info("Master Event execute service started");
    }

    /**
     * 事件服务主循环，轮询处理工作流事件和流任务事件。
     * 在服务未停止时持续运行，以短间隔休眠以降低 CPU 占用。
     */
    @Override
    public void run() {
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                workflowEventHandler();
                streamTaskEventHandler();
                TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS_SHORT);
            } catch (InterruptedException interruptedException) {
                logger.warn("Master event service interrupted, will exit this loop", interruptedException);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Master event execute service error", e);
            }
        }
    }

    private void workflowEventHandler() {
        for (WorkflowExecuteRunnable workflowExecuteThread : this.processInstanceExecCacheManager.getAll()) {
            try {
                LoggerUtils.setWorkflowInstanceIdMDC(workflowExecuteThread.getProcessInstance().getId());
                workflowExecuteThreadPool.executeEvent(workflowExecuteThread);

            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void streamTaskEventHandler() {
        for (StreamTaskExecuteRunnable streamTaskExecuteRunnable : streamTaskInstanceExecCacheManager.getAll()) {
            try {
                LoggerUtils.setTaskInstanceIdMDC(streamTaskExecuteRunnable.getTaskInstance().getId());
                streamTaskExecuteThreadPool.executeEvent(streamTaskExecuteRunnable);

            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }
}
