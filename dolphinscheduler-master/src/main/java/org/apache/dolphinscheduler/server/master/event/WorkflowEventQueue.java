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

package org.apache.dolphinscheduler.server.master.event;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 工作流事件队列。基于 LinkedBlockingQueue 实现的工作流事件阻塞队列，提供事件的入队、出队和清空操作。内部的 WorkflowEventDispatchThread 持续从该队列中消费事件并调度到对应的 WorkflowEventHandler 执行。
 */
@Component
public class WorkflowEventQueue {

    private final Logger logger = LoggerFactory.getLogger(WorkflowEventQueue.class);

    private static final LinkedBlockingQueue<WorkflowEvent> workflowEventQueue = new LinkedBlockingQueue<>();

    /**
     * 添加工作流事件到队列。
     *
     * @param workflowEvent 待添加的工作流事件
     */
    public void addEvent(WorkflowEvent workflowEvent) {
        workflowEventQueue.add(workflowEvent);
        logger.info("Added workflow event to workflowEvent queue, event: {}", workflowEvent);
    }

    /**
     * 从队列头部取出一个工作流事件，若队列为空则阻塞等待。
     *
     * @return 队列头部的工作流事件
     * @throws InterruptedException 等待过程中线程被中断
     */
    public WorkflowEvent poolEvent() throws InterruptedException {
        return workflowEventQueue.take();
    }

    public void clearWorkflowEventQueue() {
        workflowEventQueue.clear();
    }
}
