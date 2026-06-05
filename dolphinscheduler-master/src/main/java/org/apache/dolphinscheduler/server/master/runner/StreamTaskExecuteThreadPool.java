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

import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * 流任务执行线程池，用于异步执行 {@link StreamTaskExecuteRunnable} 的事件处理。
 * 提供线程池初始化、容量配置以及所有流任务事件处理的执行入口。
 */
@Component
public class StreamTaskExecuteThreadPool extends ThreadPoolTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(StreamTaskExecuteThreadPool.class);

    @Autowired
    private MasterConfig masterConfig;

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("StreamTaskExecuteThread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    /**
     * 处理指定流任务的所有待处理事件，异步提交执行并通过回调记录结果。
     *
     * @param streamTaskExecuteRunnable 流任务执行器
     */
    public void executeEvent(final StreamTaskExecuteRunnable streamTaskExecuteRunnable) {
        if (!streamTaskExecuteRunnable.isStart() || streamTaskExecuteRunnable.eventSize() == 0) {
            return;
        }
        int taskInstanceId = streamTaskExecuteRunnable.getTaskInstance().getId();
        ListenableFuture<?> future = this.submitListenable(streamTaskExecuteRunnable::handleEvents);
        future.addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
                logger.error("Stream task instance events handle failed", ex);
                LoggerUtils.removeTaskInstanceIdMDC();
            }

            @Override
            public void onSuccess(Object result) {
                LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
                logger.info("Stream task instance is finished.");
                LoggerUtils.removeTaskInstanceIdMDC();
            }
        });
    }
}
