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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

/**
 * 同级任务实例优先级队列，用于管理同一流程实例中的所有任务实例。基于 PriorityQueue 实现，支持任务的入队、出队、移除和包含检查等操作。
 */
public class PeerTaskInstancePriorityQueue implements TaskPriorityQueue<TaskInstance> {
    /**
     * queue size
     */
    private static final Integer QUEUE_MAX_SIZE = 3000;

    /**
     * queue
     */
    private final PriorityQueue<TaskInstance> queue = new PriorityQueue<>(QUEUE_MAX_SIZE, new TaskInfoComparator());
    private final Set<String> taskInstanceIdentifySet = Collections.synchronizedSet(new HashSet<>());

    /**
     * 将任务实例放入优先级队列。
     *
     * @param taskInstance the task instance to put
     */
    @Override
    public void put(TaskInstance taskInstance) {
        Preconditions.checkNotNull(taskInstance);
        queue.add(taskInstance);
        taskInstanceIdentifySet.add(getTaskInstanceIdentify(taskInstance));
    }

    /**
     * 从优先级队列中取出任务实例（阻塞等待）。
     *
     * @return the task instance removed from the queue
     * @throws TaskPriorityQueueException if queue operation fails
     */
    @Override
    public TaskInstance take() throws TaskPriorityQueueException {
        TaskInstance taskInstance = queue.poll();
        if (taskInstance != null) {
            taskInstanceIdentifySet.remove(getTaskInstanceIdentify(taskInstance));
        }
        return taskInstance;
    }

    /**
     * 带超时的轮询操作。注意：此实现未考虑超时精度，建议使用 PriorityBlockingQueue 替代。
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the task instance, or null if timeout
     * @throws TaskPriorityQueueException if operation is not supported
     */
    @Override
    public TaskInstance poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException {
        throw new TaskPriorityQueueException("This operation is not currently supported and suggest to use PriorityBlockingQueue if you want！");
    }

    /**
     * 查看队列头部的任务实例但不移除。
     *
     * @return the head task instance, or null if empty
     */
    public TaskInstance peek() {
        return queue.peek();
    }

    /**
     * 获取队列中的任务实例数量。
     *
     * @return the number of task instances in the queue
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * 清空任务实例队列。
     */
    public void clear() {
        queue.clear();
        taskInstanceIdentifySet.clear();
    }

    /**
     * 判断队列中是否包含指定的任务实例。
     *
     * @param taskInstance the task instance to check
     * @return true if the task instance is contained
     */
    public boolean contains(TaskInstance taskInstance) {
        Preconditions.checkNotNull(taskInstance);
        return taskInstanceIdentifySet.contains(getTaskInstanceIdentify(taskInstance));
    }

    /**
     * 从队列中移除指定的任务实例。
     *
     * @param taskInstance the task instance to remove
     * @return true if the task instance was removed successfully
     */
    public boolean remove(TaskInstance taskInstance) {
        Preconditions.checkNotNull(taskInstance);
        taskInstanceIdentifySet.remove(getTaskInstanceIdentify(taskInstance));
        return queue.remove(taskInstance);
    }

    /**
     * 获取队列的迭代器，用于遍历所有任务实例。
     *
     * @return an iterator over the task instances in the queue
     */
    public Iterator<TaskInstance> iterator() {
        return queue.iterator();
    }

    // since the task instance will not contain taskInstanceId until insert into database
    // So we use processInstanceId + taskCode + version to identify a taskInstance.
    private String getTaskInstanceIdentify(TaskInstance taskInstance) {
        return String.join(
                String.valueOf(taskInstance.getProcessInstanceId()),
                String.valueOf(taskInstance.getTaskCode()),
                String.valueOf(taskInstance.getTaskDefinitionVersion())
                , "-");
    }

    /**
     * TaskInfoComparator
     */
    private static class TaskInfoComparator implements Comparator<TaskInstance> {

        /**
         * compare o1 o2
         *
         * @param o1 o1
         * @param o2 o2
         * @return compare result
         */
        @Override
        public int compare(TaskInstance o1, TaskInstance o2) {
            if(o1.getTaskInstancePriority().equals(o2.getTaskInstancePriority())){
                // larger number, higher priority
                return Constants.OPPOSITE_VALUE * Integer.compare(o1.getTaskGroupPriority(),o2.getTaskGroupPriority());
            }
            return o1.getTaskInstancePriority().compareTo(o2.getTaskInstancePriority());
        }
    }
}
