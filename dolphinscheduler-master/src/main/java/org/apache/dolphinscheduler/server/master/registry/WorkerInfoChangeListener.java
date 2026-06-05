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

import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;

import java.util.Map;
import java.util.Set;

/**
 * Worker信息变更监听器接口。用于在Worker节点信息或分组映射发生变化时接收通知，由ServerNodeManager负责回调。
 */
public interface WorkerInfoChangeListener {

    /**
     * 通知Worker信息发生变更。
     *
     * @param workerGroups Worker分组映射，key为分组名称，value为该分组下的Worker地址集合
     * @param workerNodeInfo Worker节点信息映射，key为Worker地址，value为Worker心跳信息
     */
    void notify(Map<String, Set<String>> workerGroups, Map<String, WorkerHeartBeat> workerNodeInfo);

}
