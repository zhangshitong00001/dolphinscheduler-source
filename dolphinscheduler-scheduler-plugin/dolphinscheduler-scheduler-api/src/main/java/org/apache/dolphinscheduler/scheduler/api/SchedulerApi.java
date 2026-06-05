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

package org.apache.dolphinscheduler.scheduler.api;

import org.apache.dolphinscheduler.dao.entity.Schedule;

/**
 * 调度器抽象接口，定义了操作定时调度任务的核心方法。
 * <p>
 * 该接口是DolphinScheduler调度层的SPI契约，允许通过不同实现
 * （如Quartz、自定义调度器等）来驱动定时工作流的执行。
 * 继承自 {@link AutoCloseable}，确保调度器资源可以被正确释放。
 * <p>
 * 核心操作包括：启动调度器、插入/更新调度任务、删除调度任务、关闭调度器。
 */
public interface SchedulerApi extends AutoCloseable{

    /**
     * 启动调度器，启动后调度器才会开始执行已注册的定时任务
     *
     * @throws SchedulerException 启动失败时抛出
     */
    void start() throws SchedulerException;

    /**
     * 插入或更新一个定时调度任务。如果该任务已存在，则更新；否则新建。
     *
     * @param projectId 任务所属的项目ID
     * @param schedule  调度元数据（包含CRON表达式、时间范围、失败策略等）
     * @throws SchedulerException 插入/更新失败时抛出
     */
    void insertOrUpdateScheduleTask(int projectId, Schedule schedule) throws SchedulerException;

    /**
     * 删除指定的定时调度任务
     *
     * @param projectId  任务所属的项目ID
     * @param scheduleId 调度任务ID
     * @throws SchedulerException 删除失败时抛出
     */
    void deleteScheduleTask(int projectId, int scheduleId) throws SchedulerException;

    /**
     * 关闭调度器并释放底层资源
     *
     * @throws Exception 关闭失败时抛出
     */
    void close() throws Exception;
}
