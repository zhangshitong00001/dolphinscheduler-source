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

package org.apache.dolphinscheduler.server.master.registry;

import org.apache.dolphinscheduler.registry.api.StrategyType;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Master停止连接策略。当Master与注册中心断开连接时，直接停止Master服务器。该策略为默认策略。
 */
@Service
@ConditionalOnProperty(prefix = "master.registry-disconnect-strategy", name = "strategy", havingValue = "stop", matchIfMissing = true)
public class MasterStopStrategy implements MasterConnectStrategy {

    private final Logger logger = LoggerFactory.getLogger(MasterStopStrategy.class);

    @Autowired
    private RegistryClient registryClient;
    @Autowired
    private MasterConfig masterConfig;

    /**
     * 断开连接时的处理逻辑。直接调用Stoppable接口停止Master服务器。
     */
    @Override
    public void disconnect() {
        registryClient.getStoppable()
                .stop("Master disconnected from registry, will stop myself due to the stop strategy");
    }

    /**
     * 重连时的处理逻辑。停止策略下不会尝试重连，仅记录警告日志。
     */
    @Override
    public void reconnect() {
        logger.warn("The current connect strategy is stop, so the master will not reconnect to registry");
    }

    /**
     * 获取策略类型。
     *
     * @return 策略类型，始终返回STOP
     */
    @Override
    public StrategyType getStrategyType() {
        return StrategyType.STOP;
    }
}
