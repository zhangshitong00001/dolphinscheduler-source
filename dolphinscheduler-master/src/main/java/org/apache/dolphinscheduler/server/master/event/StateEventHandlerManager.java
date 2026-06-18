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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.StateEventType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * 状态事件处理器管理器。通过 Java SPI 机制自动加载所有 StateEventHandler 实现，并提供按事件类型获取对应处理器的能力。
 */
public class StateEventHandlerManager {

    private static final Map<StateEventType, StateEventHandler> stateEventHandlerMap = new HashMap<>();

    static {
        ServiceLoader.load(StateEventHandler.class)
            .forEach(stateEventHandler -> stateEventHandlerMap.put(stateEventHandler.getEventType(),
                stateEventHandler));
    }

    /**
     * 根据状态事件类型获取对应的状态事件处理器。
     *
     * @param stateEventType 状态事件类型
     * @return 对应的事件处理器 Optional 封装，不存在时返回 Optional.empty()
     */
    public static Optional<StateEventHandler> getStateEventHandler(StateEventType stateEventType) {
        return Optional.ofNullable(stateEventHandlerMap.get(stateEventType));
    }

}
