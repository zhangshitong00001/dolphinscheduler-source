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

package org.apache.dolphinscheduler.server.master.dispatch;

import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.dispatch.executor.ExecutorManager;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.dispatch.host.HostManager;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 执行器分发器。负责选择主机并通过对应的 ExecutorManager 执行任务分发。
 */
@Service
public class ExecutorDispatcher implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorDispatcher.class);

    /**
     * Netty 执行器管理器。
     */
    @Autowired
    private NettyExecutorManager nettyExecutorManager;

    /**
     * 主机管理器，负责主机选择。
     */
    @Autowired
    private HostManager hostManager;

    /**
     * 执行器管理器映射，按 ExecutorType 路由到对应的 ExecutorManager。
     */
    private final ConcurrentHashMap<ExecutorType, ExecutorManager<Boolean>> executorManagers;

    public ExecutorDispatcher() {
        this.executorManagers = new ConcurrentHashMap<>();
    }

    /**
     * 任务分发。先选择目标主机，再通过对应的 ExecutorManager 执行任务。
     *
     * @param context 执行上下文
     * @return 分发结果
     * @throws ExecuteException 若执行异常则抛出
     */
    public Boolean dispatch(final ExecutionContext context) throws ExecuteException {
        // get executor manager
        ExecutorManager<Boolean> executorManager = this.executorManagers.get(context.getExecutorType());
        if (executorManager == null) {
            throw new ExecuteException("no ExecutorManager for type : " + context.getExecutorType());
        }

        // host select
        Host host = hostManager.select(context);
        if (StringUtils.isEmpty(host.getAddress())) {
            logger.warn("fail to execute : {} due to no suitable worker, current task needs worker group {} to execute",
                context.getCommand(), context.getWorkerGroup());
            return false;
        }
        context.setHost(host);
        executorManager.beforeExecute(context);
        try {
            // task execute
            return executorManager.execute(context);
        } finally {
            executorManager.afterExecute(context);
        }
    }

    /**
     * Bean 初始化后注册各类型对应的 ExecutorManager。
     * @throws Exception 若初始化异常则抛出
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        register(ExecutorType.WORKER, nettyExecutorManager);
        register(ExecutorType.CLIENT, nettyExecutorManager);
    }

    /**
     * 注册执行器类型与 ExecutorManager 的映射关系。
     * @param type 执行器类型
     * @param executorManager 执行器管理器
     */
    public void register(ExecutorType type, ExecutorManager executorManager) {
        executorManagers.put(type, executorManager);
    }
}
