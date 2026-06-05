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

package org.apache.dolphinscheduler.server.master.cache;

import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.util.Collection;

import lombok.NonNull;

/**
 * 流式任务实例执行缓存管理器接口。定义流式任务实例 ID 与 StreamTaskExecuteRunnable 的缓存操作。
 */
public interface StreamTaskInstanceExecCacheManager {

    /**
     * 根据任务实例 ID 获取 StreamTaskExecuteRunnable。
     *
     * @param taskInstanceId 任务实例 ID
     * @return StreamTaskExecuteRunnable
     */
    StreamTaskExecuteRunnable getByTaskInstanceId(int taskInstanceId);

    /**
     * 判断任务实例是否存在于缓存中。
     *
     * @param taskInstanceId 任务实例 ID
     * @return true 表示任务实例 ID 存在于缓存中
     */
    boolean contains(int taskInstanceId);

    /**
     * 根据任务实例 ID 移除缓存。
     *
     * @param taskInstanceId 任务实例 ID
     */
    void removeByTaskInstanceId(int taskInstanceId);

    /**
     * 缓存任务实例与 StreamTaskExecuteRunnable 的映射。
     *
     * @param taskInstanceId             任务实例 ID
     * @param streamTaskExecuteRunnable 若为 null，则不会被缓存
     */
    void cache(int taskInstanceId, @NonNull StreamTaskExecuteRunnable streamTaskExecuteRunnable);

    /**
     * 获取缓存中所有的 StreamTaskExecuteRunnable。
     *
     * @return 缓存中所有的 StreamTaskExecuteRunnable
     */
    Collection<StreamTaskExecuteRunnable> getAll();
}