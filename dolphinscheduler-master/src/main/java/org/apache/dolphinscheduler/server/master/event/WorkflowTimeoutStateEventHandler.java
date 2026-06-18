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
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工作流超时状态事件处理器。用于处理工作流实例的超时事件，触发超时处理逻辑并记录相应的超时指标。
 */
@AutoService(StateEventHandler.class)
public class WorkflowTimeoutStateEventHandler implements StateEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowTimeoutStateEventHandler.class);

    /**
     * 处理工作流超时状态事件，记录超时指标并触发工作流超时逻辑。
     *
     * @param workflowExecuteRunnable 工作流执行运行实例
     * @param stateEvent 状态事件
     * @return 处理结果，始终返回true
     */
    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable, StateEvent stateEvent) {
        logger.info("Handle workflow instance timeout event");
        ProcessInstanceMetrics.incProcessInstanceByState("timeout");
        workflowExecuteRunnable.processTimeout();
        return true;
    }

    /**
     * 获取当前处理器对应的事件类型。
     *
     * @return 状态事件类型，始终为PROCESS_TIMEOUT
     */
    @Override
    public StateEventType getEventType() {
        return StateEventType.PROCESS_TIMEOUT;
    }
}
