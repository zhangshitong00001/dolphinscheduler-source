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

import lombok.NonNull;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.util.Collection;

import lombok.NonNull;

/**
 * 流程实例执行缓存管理器接口。定义流程实例 ID 与 WorkflowExecuteRunnable 的缓存操作。
 */
public interface ProcessInstanceExecCacheManager {

    /**
     * 根据流程实例 ID 获取 WorkflowExecuteRunnable。
     *
     * @param processInstanceId 流程实例 ID
     * @return WorkflowExecuteRunnable
     */
    WorkflowExecuteRunnable getByProcessInstanceId(int processInstanceId);

    /**
     * 判断流程实例是否存在于缓存中。
     *
     * @param processInstanceId 流程实例 ID
     * @return true 表示流程实例 ID 存在于缓存中
     */
    boolean contains(int processInstanceId);

    /**
     * 根据流程实例 ID 移除缓存。
     *
     * @param processInstanceId 流程实例 ID
     */
    void removeByProcessInstanceId(int processInstanceId);

    /**
     * 缓存流程实例与 WorkflowExecuteRunnable 的映射。
     *
     * @param processInstanceId     流程实例 ID
     * @param workflowExecuteThread 若为 null，则不会被缓存
     */
    void cache(int processInstanceId, @NonNull WorkflowExecuteRunnable workflowExecuteThread);

    /**
     * 获取缓存中所有的 WorkflowExecuteRunnable。
     *
     * @return 缓存中所有的 WorkflowExecuteRunnable
     */
    Collection<WorkflowExecuteRunnable> getAll();

    /**
     * 清空缓存。
     */
    void clearCache();
}