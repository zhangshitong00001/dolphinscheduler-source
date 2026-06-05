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

package org.apache.dolphinscheduler.server.master.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.experimental.UtilityClass;

/**
 * Master服务器指标收集工具类。用于记录和暴露Master服务器的过载次数和命令消费量等运行指标。
 */
@UtilityClass
public class MasterServerMetrics {

    /**
     * 用于统计Master服务器过载的次数。
     */
    private final Counter masterOverloadCounter =
            Counter.builder("ds.master.overload.count")
                    .description("Master server overload count")
                    .register(Metrics.globalRegistry);

    /**
     * 用于统计Master服务器消费的命令数量。
     */
    private final Counter masterConsumeCommandCounter =
            Counter.builder("ds.master.consume.command.count")
                    .description("Master server consume command count")
                    .register(Metrics.globalRegistry);

    /**
     * 增加Master过载计数。
     */
    public void incMasterOverload() {
        masterOverloadCounter.increment();
    }

    /**
     * 增加Master命令消费计数。
     *
     * @param commandCount 本次消费的命令数量
     */
    public void incMasterConsumeCommand(int commandCount) {
        masterConsumeCommandCounter.increment(commandCount);
    }

}
