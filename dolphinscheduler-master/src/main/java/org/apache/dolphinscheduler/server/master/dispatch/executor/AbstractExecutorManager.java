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
 * 抽象执行器管理器。提供 beforeExecute 和 afterExecute 的默认空实现，子类可按需覆写。
 *
 * @param <T> 执行结果类型
 */
public abstract class AbstractExecutorManager<T> implements ExecutorManager<T> {

    /**
     * 执行前的回调，可在此添加时间监控和超时控制。
     *
     * @param context 执行上下文
     * @throws ExecuteException 若执行异常则抛出
     */
    @Override
    public void beforeExecute(ExecutionContext context) throws ExecuteException {
    }

    /**
     * 执行后的回调，可在此添加分发监控。
     * @param context 执行上下文
     * @throws ExecuteException 若执行异常则抛出
     */
    @Override
    public void afterExecute(ExecutionContext context) throws ExecuteException {
    }
}
