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

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

/**
 * 状态事件处理器接口。定义了处理各类状态事件的契约，每种状态事件类型对应一个处理器实现，通过 SPI 机制自动注册。
 */
public interface StateEventHandler {

    /**
     * 处理状态事件。处理成功返回 true，否则返回 false。
     *
     * @param workflowExecuteRunnable 当前正在执行的工作流运行实例
     * @param stateEvent 待处理的状态事件
     * @return 处理成功返回 true，否则返回 false
     * @throws StateEventHandleException 可恢复的异常，系统将重试该事件
     * @throws StateEventHandleError 不可恢复的异常，系统将丢弃该事件
     * @throws StateEventHandleFailure 处理失败异常，事件将被移入失败队列
     */
    boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable,
                             StateEvent stateEvent) throws StateEventHandleException, StateEventHandleError, StateEventHandleFailure;

    StateEventType getEventType();
}
