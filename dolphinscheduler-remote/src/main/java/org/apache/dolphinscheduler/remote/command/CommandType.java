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

package org.apache.dolphinscheduler.remote.command;

/**
 * 命令类型枚举。定义Netty远程通信中所有支持的命令类型，涵盖日志查看、任务执行、告警发送、心跳检测、状态事件和缓存过期等场景。
 */
public enum CommandType {

    /** 获取AppId请求 */
    GET_APP_ID_REQUEST,
    /** 获取AppId响应 */
    GET_APP_ID_RESPONSE,

    /** 删除任务日志请求 */
    REMOVE_TAK_LOG_REQUEST,

    /** 删除任务日志响应 */
    REMOVE_TAK_LOG_RESPONSE,

    /** 滚动查看日志请求 */
    ROLL_VIEW_LOG_REQUEST,

    /** 滚动查看日志响应 */
    ROLL_VIEW_LOG_RESPONSE,

    /** 查看完整日志请求 */
    VIEW_WHOLE_LOG_REQUEST,

    /** 查看完整日志响应 */
    VIEW_WHOLE_LOG_RESPONSE,

    /** 获取日志字节数据请求 */
    GET_LOG_BYTES_REQUEST,

    /** 获取日志字节数据响应 */
    GET_LOG_BYTES_RESPONSE,


    /** Worker请求 */
    WORKER_REQUEST,
    /** Master响应 */
    MASTER_RESPONSE,

    /**
     * 任务执行启动命令，从API发送到Master
     */
    TASK_EXECUTE_START,

    /**
     * 任务分发请求
     */
    TASK_DISPATCH_REQUEST,

    /**
     * 任务执行运行中状态通知，从Worker发送到Master
     */
    TASK_EXECUTE_RUNNING,

    /**
     * 任务执行运行中确认，从Master发送到Worker
     */
    TASK_EXECUTE_RUNNING_ACK,

    /**
     * 任务执行结果通知，从Worker发送到Master
     */
    TASK_EXECUTE_RESULT,

    /**
     * 任务执行结果确认，从Master发送到Worker
     */
    TASK_EXECUTE_RESULT_ACK,

    /** 任务终止请求 */
    TASK_KILL_REQUEST,

    /** 任务终止响应 */
    TASK_KILL_RESPONSE,

    /** 任务拒绝 */
    TASK_REJECT,

    /** 任务拒绝确认 */
    TASK_REJECT_ACK,

    /**
     * 任务保存点请求，用于流式任务
     */
    TASK_SAVEPOINT_REQUEST,

    /**
     * 任务保存点响应，用于流式任务
     */
    TASK_SAVEPOINT_RESPONSE,

    /** 心跳检测 */
    HEART_BEAT,

    /** Ping探测 */
    PING,

    /** Pong响应 */
    PONG,

    /** 告警发送请求 */
    ALERT_SEND_REQUEST,

    /** 告警发送响应 */
    ALERT_SEND_RESPONSE,

    /**
     * 进程主机更新请求
     */
    PROCESS_HOST_UPDATE_REQUEST,

    /**
     * 进程主机更新响应
     */
    PROCESS_HOST_UPDATE_RESPONSE,

    /**
     * 状态事件请求
     */
    STATE_EVENT_REQUEST,
    /**
     * 缓存过期通知
     */
    CACHE_EXPIRE,
    /**
     * 任务强制状态事件请求
     */
    TASK_FORCE_STATE_EVENT_REQUEST,
    /**
     * 任务唤醒事件请求
     */
    TASK_WAKEUP_EVENT_REQUEST,

    /**
     * 工作流执行数据请求，从API发送到Master
     */
    WORKFLOW_EXECUTING_DATA_REQUEST,

    /**
     * 工作流执行数据响应，从Master发送到API
     */
    WORKFLOW_EXECUTING_DATA_RESPONSE;
}
