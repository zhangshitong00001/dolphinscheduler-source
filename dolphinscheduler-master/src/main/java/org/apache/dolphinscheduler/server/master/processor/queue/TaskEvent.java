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
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResultCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRunningCommand;
import org.apache.dolphinscheduler.remote.command.TaskRejectCommand;

import java.util.Date;

import lombok.Data;
import io.netty.channel.Channel;

/**
 * 任务事件实体类。封装任务执行过程中各类事件的数据，包括分发、运行中、结果和召回等事件类型。
 */
@Data
public class TaskEvent {

    /**
     * 任务实例ID
     */
    private int taskInstanceId;

    /**
     * Worker节点地址
     */
    private String workerAddress;

    /**
     * 任务执行状态
     */
    private TaskExecutionStatus state;

    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务结束时间
     */
    private Date endTime;

    /**
     * 任务执行路径
     */
    private String executePath;

    /**
     * 任务日志路径
     */
    private String logPath;

    /**
     * 进程ID
     */
    private int processId;

    /**
     * 应用ID列表
     */
    private String appIds;

    /**
     * 任务事件类型（DISPATCH/RUNNING/RESULT/WORKER_REJECT）
     */
    private TaskEventType event;

    /**
     * 变量池JSON字符串
     */
    private String varPool;

    /**
     * Netty通道
     */
    private Channel channel;

    private int processInstanceId;

    /**
     * 创建任务分发事件。
     *
     * @param processInstanceId 工作流实例ID
     * @param taskInstanceId 任务实例ID
     * @param workerAddress 目标Worker地址
     * @return 分发类型的任务事件
     */
    public static TaskEvent newDispatchEvent(int processInstanceId, int taskInstanceId, String workerAddress) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(processInstanceId);
        event.setTaskInstanceId(taskInstanceId);
        event.setWorkerAddress(workerAddress);
        event.setEvent(TaskEventType.DISPATCH);
        return event;
    }

    /**
     * 创建任务运行中事件。
     *
     * @param command 任务执行运行命令
     * @param channel Netty通道
     * @param workerAddress Worker节点地址
     * @return 运行中类型的任务事件
     */
    public static TaskEvent newRunningEvent(TaskExecuteRunningCommand command, Channel channel, String workerAddress) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setState(command.getStatus());
        event.setStartTime(command.getStartTime());
        event.setExecutePath(command.getExecutePath());
        event.setLogPath(command.getLogPath());
        event.setAppIds(command.getAppIds());
        event.setChannel(channel);
        event.setWorkerAddress(workerAddress);
        event.setEvent(TaskEventType.RUNNING);
        return event;
    }

    /**
     * 创建任务结果事件。
     *
     * @param command 任务执行结果命令
     * @param channel Netty通道
     * @param workerAddress Worker节点地址
     * @return 结果类型的任务事件
     */
    public static TaskEvent newResultEvent(TaskExecuteResultCommand command, Channel channel, String workerAddress) {
        TaskEvent event = new TaskEvent();
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setState(TaskExecutionStatus.of(command.getStatus()));
        event.setStartTime(command.getStartTime());
        event.setExecutePath(command.getExecutePath());
        event.setLogPath(command.getLogPath());
        event.setEndTime(command.getEndTime());
        event.setProcessId(command.getProcessId());
        event.setAppIds(command.getAppIds());
        event.setVarPool(command.getVarPool());
        event.setChannel(channel);
        event.setWorkerAddress(workerAddress);
        event.setEvent(TaskEventType.RESULT);
        return event;
    }

    /**
     * 创建任务召回/拒绝事件。
     *
     * @param command 任务拒绝命令
     * @param channel Netty通道
     * @return Worker拒绝类型的任务事件
     */
    public static TaskEvent newRecallEvent(TaskRejectCommand command, Channel channel) {
        TaskEvent event = new TaskEvent();
        event.setTaskInstanceId(command.getTaskInstanceId());
        event.setProcessInstanceId(command.getProcessInstanceId());
        event.setChannel(channel);
        event.setEvent(TaskEventType.WORKER_REJECT);
        return event;
    }
}
