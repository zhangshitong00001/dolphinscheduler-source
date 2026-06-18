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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 任务事件管理服务。负责管理任务事件的队列、分发和处理，包含事件分发线程和事件处理线程两个守护线程。
 */
@Component
public class TaskEventService {

    /**
     * 日志记录器
     */
    private final Logger logger = LoggerFactory.getLogger(TaskEventService.class);

    /**
     * 任务事件阻塞队列
     */
    private final BlockingQueue<TaskEvent> eventQueue = new LinkedBlockingQueue<>();

    /**
     * 任务事件分发工作线程
     */
    private Thread taskEventThread;

    private Thread taskEventHandlerThread;

    @Autowired
    private TaskExecuteThreadPool taskExecuteThreadPool;

    /**
     * 启动任务事件服务。创建并启动事件分发线程和事件处理线程。
     */
    @PostConstruct
    public void start() {
        this.taskEventThread = new TaskEventDispatchThread();
        logger.info("TaskEvent dispatch thread starting");
        this.taskEventThread.start();
        logger.info("TaskEvent dispatch thread started");

        this.taskEventHandlerThread = new TaskEventHandlerThread();
        logger.info("TaskEvent handle thread staring");
        this.taskEventHandlerThread.start();
        logger.info("TaskEvent handle thread started");
    }

    /**
     * 停止任务事件服务。中断工作线程并处理队列中剩余的事件。
     */
    @PreDestroy
    public void stop() {
        try {
            this.taskEventThread.interrupt();
            this.taskEventHandlerThread.interrupt();
            if (!eventQueue.isEmpty()) {
                List<TaskEvent> remainEvents = new ArrayList<>(eventQueue.size());
                eventQueue.drainTo(remainEvents);
                for (TaskEvent taskEvent : remainEvents) {
                    taskExecuteThreadPool.submitTaskEvent(taskEvent);
                }
                taskExecuteThreadPool.eventHandler();
            }
        } catch (Exception e) {
            logger.error("TaskEventService stop error:", e);
        }
    }

    /**
     * 将任务事件添加到事件队列中。
     *
     * @param taskEvent 任务事件
     */
    public void addEvent(TaskEvent taskEvent) {
        eventQueue.add(taskEvent);
    }

    /**
     * 任务事件分发线程。循环从事件队列中取出任务事件并提交到线程池进行处理。
     */
    class TaskEventDispatchThread extends BaseDaemonThread {

        protected TaskEventDispatchThread() {
            super("TaskEventLoopThread");
        }

        @Override
        public void run() {
            while (!ServerLifeCycleManager.isStopped()) {
                try {
                    // if not task event, blocking here
                    TaskEvent taskEvent = eventQueue.take();
                    taskExecuteThreadPool.submitTaskEvent(taskEvent);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("persist task error", e);
                }
            }
            logger.info("StateEventResponseWorker stopped");
        }
    }

    /**
     * 任务事件处理线程。定时调用线程池的事件处理方法，处理已提交的任务事件。
     */
    class TaskEventHandlerThread extends BaseDaemonThread {

        protected TaskEventHandlerThread() {
            super("TaskEventHandlerThread");
        }

        @Override
        public void run() {
            logger.info("event handler thread started");
            while (!ServerLifeCycleManager.isStopped()) {
                try {
                    taskExecuteThreadPool.eventHandler();
                    TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("TaskEvent handle thread interrupted, will return this loop");
                    break;
                } catch (Exception e) {
                    logger.error("event handler thread error", e);
                }
            }
        }
    }
}