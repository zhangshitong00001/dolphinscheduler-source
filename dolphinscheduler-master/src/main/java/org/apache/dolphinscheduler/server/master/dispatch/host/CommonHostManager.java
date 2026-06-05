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

package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 通用主机管理器抽象类。提供根据执行上下文选择主机的基础逻辑，子类实现具体的 HostWorker 选择策略。
 */
public abstract class CommonHostManager implements HostManager {

    /**
     * 服务节点管理器。
     */
    @Autowired
    protected ServerNodeManager serverNodeManager;

    /**
     * 根据执行上下文选择目标主机。按执行器类型获取候选节点列表，再委托子类选择。
     *
     * @param context 执行上下文
     * @return 选中的主机
     */
    @Override
    public Host select(ExecutionContext context) {
        List<HostWorker> candidates = null;
        String workerGroup = context.getWorkerGroup();
        ExecutorType executorType = context.getExecutorType();
        switch (executorType) {
            case WORKER:
                candidates = getWorkerCandidates(workerGroup);
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executorType : " + executorType);
        }

        if (CollectionUtils.isEmpty(candidates)) {
            return new Host();
        }
        return select(candidates);
    }

    /**
     * 子类实现从候选节点列表中选择一个 HostWorker 的具体策略。
     *
     * @param nodes 候选节点列表
     * @return 选中的 HostWorker
     */
    protected abstract HostWorker select(Collection<HostWorker> nodes);

    /**
     * 获取指定 Worker 分组的候选节点列表。
     *
     * @param workerGroup Worker 分组名称
     * @return 候选 HostWorker 列表
     */
    protected List<HostWorker> getWorkerCandidates(String workerGroup) {
        List<HostWorker> hostWorkers = new ArrayList<>();
        Set<String> nodes = serverNodeManager.getWorkerGroupNodes(workerGroup);
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (String node : nodes) {
                serverNodeManager.getWorkerNodeInfo(node).ifPresent(
                        workerNodeInfo -> hostWorkers
                                .add(HostWorker.of(node, workerNodeInfo.getWorkerHostWeight(), workerGroup)));
            }
        }
        return hostWorkers;
    }
}
