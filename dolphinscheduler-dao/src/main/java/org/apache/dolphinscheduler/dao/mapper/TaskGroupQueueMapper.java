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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 任务组队列 Mapper 接口，封装对 t_ds_task_group_queue 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供任务组队列的分页查询、优先级调度、状态更新及CAS入队等能力。
 */
public interface TaskGroupQueueMapper extends BaseMapper<TaskGroupQueue> {

    /**
     * 分页查询指定任务组下的队列记录。
     * SELECT * FROM t_ds_task_group_queue WHERE group_id = #{groupId}
     *
     * @param page 分页对象
     * @param groupId 任务组ID
     * @return 任务组队列分页结果
     */
    IPage<TaskGroupQueue> queryTaskGroupQueuePaging(IPage<TaskGroupQueue> page,
                                                    @Param("groupId") int groupId
    );

    /**
     * 根据任务ID查询对应的任务组队列记录。
     * SELECT * FROM t_ds_task_group_queue WHERE task_id = #{taskId}
     *
     * @param taskId 任务ID
     * @return 任务组队列实体，若不存在则返回 null
     */
    TaskGroupQueue queryByTaskId(@Param("taskId") int taskId);

    /**
     * 根据状态查询所有任务组队列记录。
     * SELECT * FROM t_ds_task_group_queue WHERE status = #{status}
     *
     * @param status 队列状态
     * @return 任务组队列列表
     */
    List<TaskGroupQueue> queryByStatus(@Param("status") int status);

    /**
     * 根据任务ID删除对应的任务组队列记录。
     * DELETE FROM t_ds_task_group_queue WHERE task_id = #{taskId}
     *
     * @param taskId 任务ID
     * @return 受影响的行数
     */
    int deleteByTaskId(@Param("taskId") int taskId);

    /**
     * 根据任务ID更新任务组队列的状态。
     * UPDATE t_ds_task_group_queue SET status = #{status} WHERE task_id = #{taskId}
     *
     * @param taskId 任务ID
     * @param status 新的队列状态
     * @return 受影响的行数
     */
    int updateStatusByTaskId(@Param("taskId") int taskId, @Param("status") int status);

    /**
     * 查询指定任务组中优先级高于给定值的队列记录。
     * SELECT * FROM t_ds_task_group_queue WHERE group_id = #{groupId} AND priority > #{priority} AND status = #{status}
     *
     * @param groupId 任务组ID
     * @param priority 优先级阈值
     * @param status 队列状态
     * @return 高优先级任务组队列列表
     */
    List<TaskGroupQueue> queryHighPriorityTasks(@Param("groupId") int groupId, @Param("priority") int priority, @Param("status") int status);

    /**
     * 查询指定任务组中优先级最高的队列记录，支持按强制启动和入队状态筛选。
     * SELECT * FROM t_ds_task_group_queue WHERE group_id = #{groupId} AND status = #{status} ... ORDER BY priority ASC LIMIT 1
     *
     * @param groupId 任务组ID
     * @param status 队列状态
     * @param forceStart 强制启动标记
     * @param inQueue 入队标记
     * @return 最高优先级的任务组队列实体，若不存在则返回 null
     */
    TaskGroupQueue queryTheHighestPriorityTasks(@Param("groupId") int groupId, @Param("status") int status,
                                                @Param("forceStart") int forceStart, @Param("inQueue") int inQueue);

    /**
     * 更新指定队列记录的入队状态。
     * UPDATE t_ds_task_group_queue SET in_queue = #{inQueue} WHERE id = #{id}
     *
     * @param inQueue 入队状态值
     * @param id 队列记录ID
     */
    void updateInQueue(@Param("inQueue") int inQueue, @Param("id") int id);

    /**
     * 更新指定队列记录的强制启动标记。
     * UPDATE t_ds_task_group_queue SET force_start = #{forceStart} WHERE queue_id = #{queueId}
     *
     * @param queueId 队列ID
     * @param forceStart 强制启动标记
     */
    void updateForceStart(@Param("queueId") int queueId, @Param("forceStart") int forceStart);

    /**
     * 在指定任务组中将一条记录的入队状态从旧值更新为新值（限制更新一条）。
     * UPDATE t_ds_task_group_queue SET in_queue = #{newValue} WHERE in_queue = #{oldValue} AND group_id = #{id} AND status = #{status} LIMIT 1
     *
     * @param oldValue 旧的入队状态值
     * @param newValue 新的入队状态值
     * @param id 任务组ID
     * @param status 队列状态
     * @return 受影响的行数
     */
    int updateInQueueLimit1(@Param("oldValue") int oldValue, @Param("newValue") int newValue
            , @Param("groupId") int id, @Param("status") int status);

    /**
     * 通过CAS原子操作更新队列记录的入队状态。
     * UPDATE t_ds_task_group_queue SET in_queue = #{newValue} WHERE in_queue = #{oldValue} AND id = #{id}
     *
     * @param oldValue 旧的入队状态值
     * @param newValue 新的入队状态值
     * @param id 队列记录ID
     * @return 受影响的行数
     */
    int updateInQueueCAS(@Param("oldValue") int oldValue, @Param("newValue") int newValue, @Param("id") int id);

    /**
     * 修改指定队列记录的优先级。
     * UPDATE t_ds_task_group_queue SET priority = #{priority} WHERE queue_id = #{queueId}
     *
     * @param queueId 队列ID
     * @param priority 新的优先级
     */
    void modifyPriority(@Param("queueId") int queueId, @Param("priority") int priority);

    /**
     * 按任务组ID分页查询任务组队列，支持按任务名称、流程名称、状态和所属项目筛选。
     * SELECT * FROM t_ds_task_group_queue WHERE group_id = #{groupId} AND task_name LIKE ... AND process_name LIKE ... AND status = #{status}
     *
     * @param page 分页对象
     * @param taskName 任务名称搜索关键字
     * @param processName 流程名称搜索关键字
     * @param status 队列状态
     * @param groupId 任务组ID
     * @param projects 关联的项目列表
     * @return 任务组队列分页结果
     */
    IPage<TaskGroupQueue> queryTaskGroupQueueByTaskGroupIdPaging(Page<TaskGroupQueue> page, @Param("taskName")String taskName
        ,@Param("processName") String processName,@Param("status") Integer status,@Param("groupId") int groupId
        ,@Param("projects") List<Project> projects);
}
