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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.common.enums.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * 故障转移服务，根据节点类型（Master 或 Worker）将故障转移到对应的服务处理。
 * 作为故障转移的统一入口，监听注册中心节点变化并根据节点类型分派到具体的故障转移实现。
 */
@Component
public class FailoverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverService.class);

    private final MasterFailoverService masterFailoverService;
    private final WorkerFailoverService workerFailoverService;

    public FailoverService(@NonNull MasterFailoverService masterFailoverService,
                           @NonNull WorkerFailoverService workerFailoverService) {
        this.masterFailoverService = masterFailoverService;
        this.workerFailoverService = workerFailoverService;
    }

    /**
     * 当服务器宕机时执行故障转移，根据节点类型分派到 Master 或 Worker 故障转移处理。
     *
     * @param serverHost 故障服务器地址
     * @param nodeType   节点类型（MASTER 或 WORKER）
     */
    public void failoverServerWhenDown(String serverHost, NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                LOGGER.info("Master failover starting, masterServer: {}", serverHost);
                masterFailoverService.failoverMaster(serverHost);
                LOGGER.info("Master failover finished, masterServer: {}", serverHost);
                break;
            case WORKER:
                LOGGER.info("Worker failover staring, workerServer: {}", serverHost);
                workerFailoverService.failoverWorker(serverHost);
                LOGGER.info("Worker failover finished, workerServer: {}", serverHost);
                break;
            default:
                break;
        }
    }

}
