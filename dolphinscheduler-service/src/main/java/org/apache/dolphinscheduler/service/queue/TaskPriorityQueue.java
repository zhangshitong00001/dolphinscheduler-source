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

package org.apache.dolphinscheduler.service.queue;

import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.concurrent.TimeUnit;

/**
 * 任务优先级队列接口，定义了任务优先级队列的基本操作。支持任务的放入、取出、带超时的轮询以及获取队列大小。
 * 用于Master节点管理和调度任务实例。
 *
 * @param <T> 队列中存储的元素类型
 */
public interface TaskPriorityQueue<T> {

    /**
     * put task info
     *
     * @param taskInfo taskInfo
     * @throws TaskPriorityQueueException
     */
    void put(T taskInfo) throws TaskPriorityQueueException;

    /**
     * take taskInfo
     *
     * @return taskInfo
     * @throws TaskPriorityQueueException
     */
    T take() throws TaskPriorityQueueException, InterruptedException;


    /**
     * poll taskInfo with timeout
     * @param timeout
     * @param unit
     * @return
     * @throws TaskPriorityQueueException
     * @throws InterruptedException
     */
    T poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException, InterruptedException;

    /**
     * size
     *
     * @return size
     * @throws TaskPriorityQueueException
     */
    int size() throws TaskPriorityQueueException;
}