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
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.event.WorkflowEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventHandleError;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventHandleException;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventHandler;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventQueue;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventType;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流事件循环器，负责从工作流事件队列中消费事件并分发给对应的处理器。
 * 管理所有 WorkflowEventHandler 实现，根据事件类型路由到对应的处理器。
 * 处理失败的事件会重新入队重试，处理错误的事件会被丢弃。
 */
@Component
public class WorkflowEventLooper extends BaseDaemonThread {

    private final Logger logger = LoggerFactory.getLogger(WorkflowEventLooper.class);

    @Autowired
    private WorkflowEventQueue workflowEventQueue;

    @Autowired
    private List<WorkflowEventHandler> workflowEventHandlerList;

    private final Map<WorkflowEventType, WorkflowEventHandler> workflowEventHandlerMap = new HashMap<>();

    protected WorkflowEventLooper() {
        super("WorkflowEventLooper");
    }

    @PostConstruct
    public void init() {
        workflowEventHandlerList.forEach(
                workflowEventHandler -> workflowEventHandlerMap.put(workflowEventHandler.getHandleWorkflowEventType(),
                        workflowEventHandler));
    }

    @Override
    public synchronized void start() {
        logger.info("WorkflowEventLooper thread starting");
        super.start();
        logger.info("WorkflowEventLooper thread started");
    }

    /**
     * 事件循环主逻辑，从事件队列中持续获取工作流事件并分发处理。
     */
    public void run() {
        WorkflowEvent workflowEvent = null;
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                workflowEvent = workflowEventQueue.poolEvent();
                LoggerUtils.setWorkflowInstanceIdMDC(workflowEvent.getWorkflowInstanceId());
                logger.info("Workflow event looper receive a workflow event: {}, will handle this", workflowEvent);
                WorkflowEventHandler workflowEventHandler =
                        workflowEventHandlerMap.get(workflowEvent.getWorkflowEventType());
                workflowEventHandler.handleWorkflowEvent(workflowEvent);
            } catch (InterruptedException e) {
                logger.warn("WorkflowEventLooper thread is interrupted, will close this loop", e);
                Thread.currentThread().interrupt();
                break;
            } catch (WorkflowEventHandleException workflowEventHandleException) {
                logger.error("Handle workflow event failed, will add this event to event queue again, event: {}",
                        workflowEvent, workflowEventHandleException);
                workflowEventQueue.addEvent(workflowEvent);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (WorkflowEventHandleError workflowEventHandleError) {
                logger.error("Handle workflow event error, will drop this event, event: {}",
                        workflowEvent,
                        workflowEventHandleError);
            } catch (Exception unknownException) {
                logger.error(
                        "Handle workflow event failed, get a unknown exception, will add this event to event queue again, event: {}",
                        workflowEvent, unknownException);
                workflowEventQueue.addEvent(workflowEvent);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            } finally {
                LoggerUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

}
