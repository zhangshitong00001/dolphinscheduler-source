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

import lombok.NonNull;
import org.apache.dolphinscheduler.common.enums.StateEventType;

import io.netty.channel.Channel;

import javax.annotation.Nullable;

/**
 * 状态事件接口。定义了流程实例和任务实例相关的状态事件契约，所有状态事件（如任务状态变化、工作流状态变化、任务重试、任务超时等）均需实现此接口。
 */
public interface StateEvent {

    int getProcessInstanceId();

    Integer getTaskInstanceId();

    @NonNull
    StateEventType getType();

    @Nullable
    String getKey();

    @Nullable
    Channel getChannel();

    @Nullable
    String getContext();

}
