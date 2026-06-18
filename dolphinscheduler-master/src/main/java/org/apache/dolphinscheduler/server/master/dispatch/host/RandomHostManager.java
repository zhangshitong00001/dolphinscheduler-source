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

import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.RandomSelector;

import java.util.Collection;

/**
 * 随机主机管理器。根据主机权重按比例随机选择一个 Worker 节点。
 */
public class RandomHostManager extends CommonHostManager {

    /**
     * 随机选择器。
     */
    private final RandomSelector selector;

    public RandomHostManager() {
        this.selector = new RandomSelector();
    }

    /**
     * 从候选节点中随机选择一个 HostWorker。
     *
     * @param nodes 候选节点集合
     * @return 选中的 HostWorker
     */
    @Override
    public HostWorker select(Collection<HostWorker> nodes) {
        return selector.select(nodes);
    }

}
