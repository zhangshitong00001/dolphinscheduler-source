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

/**
 * 工作流事件处理器接口。定义了处理工作流级别事件的契约，如工作流启动事件，通过 WorkflowEventQueue 消费工作流事件并调度到对应的处理器执行。
 */
public interface WorkflowEventHandler {

    /**
     * 处理工作流事件。
     *
     * @param workflowEvent 待处理的工作流事件
     * @throws WorkflowEventHandleError 不可恢复的错误，系统将丢弃该事件
     * @throws WorkflowEventHandleException 可恢复的异常，系统将重试该事件
     */
    void handleWorkflowEvent(WorkflowEvent workflowEvent) throws WorkflowEventHandleError, WorkflowEventHandleException;

    WorkflowEventType getHandleWorkflowEventType();
}
