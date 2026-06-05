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

package org.apache.dolphinscheduler.server.master.dispatch.executor;

import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;

/**
 * 执行器管理器接口。定义任务执行的生命周期方法：前置处理、执行、直接执行和后置处理。
 *
 * @param <T> 执行结果类型
 */
public interface ExecutorManager<T> {

    /**
     * 任务执行前的回调。
     *
     * @param executeContext 执行上下文
     * @throws ExecuteException 若执行异常则抛出
     */
    void beforeExecute(ExecutionContext executeContext) throws ExecuteException;

    /**
     * 执行任务，支持重试。
     * @param context 执行上下文
     * @return 执行结果
     * @throws ExecuteException 若执行异常则抛出
     */
    T execute(ExecutionContext context) throws ExecuteException;

    /**
     * 直接执行任务，不进行重试。
     * @param context 执行上下文
     * @throws ExecuteException 若执行异常则抛出
     */
    void executeDirectly(ExecutionContext context) throws ExecuteException;

    /**
     * 任务执行后的回调。
     * @param context 执行上下文
     * @throws ExecuteException 若执行异常则抛出
     */
    void afterExecute(ExecutionContext context) throws ExecuteException;
}
