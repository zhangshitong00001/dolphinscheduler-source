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

import org.apache.dolphinscheduler.dao.entity.TaskGroup;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 任务组 Mapper 接口，封装对 t_ds_task_group 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供任务组的资源抢占、释放、分页查询及授权管理等能力。
 */
public interface TaskGroupMapper extends BaseMapper<TaskGroup> {

    /**
     * 抢占任务组资源，通过CAS方式更新已使用大小和队列状态。
     * UPDATE t_ds_task_group SET use_size = use_size + #{currentUseSize} WHERE id = #{id} AND group_size >= use_size + #{currentUseSize}
     *
     * @param id 任务组ID
     * @param currentUseSize 当前需要占用的资源大小
     * @param queueId 关联的队列ID
     * @param queueStatus 队列状态
     * @return 受影响的行数，0 表示抢占失败
     */
    int robTaskGroupResource(@Param("id") int id,
                             @Param("currentUseSize") int currentUseSize,
                             @Param("queueId") int queueId,
                             @Param("queueStatus") int queueStatus);

    /**
     * 释放任务组资源，减少已使用大小并更新对应队列状态。
     * UPDATE t_ds_task_group SET use_size = use_size - #{useSize} WHERE id = #{id}
     *
     * @param id 任务组主键ID
     * @param useSize 需要释放的资源大小
     * @param queueId 关联的队列ID
     * @param queueStatus 队列状态
     * @return 受影响的行数
     */
    int releaseTaskGroupResource(@Param("id") int id, @Param("useSize") int useSize,
                                 @Param("queueId") int queueId, @Param("queueStatus") int queueStatus);

    /**
     * 分页查询任务组，支持按用户ID列表、名称和状态筛选。
     * SELECT * FROM t_ds_task_group WHERE user_id IN (...) AND name LIKE ... AND status = #{status}
     *
     * @param page 分页对象
     * @param ids 用户ID列表
     * @param name 任务组名称
     * @param status 任务组状态
     * @return 任务组分页结果
     */
    IPage<TaskGroup> queryTaskGroupPaging(IPage<TaskGroup> page, @Param("ids") List<Integer> ids,
                                          @Param("name") String name, @Param("status") Integer status);

    /**
     * 根据用户ID和任务组名称精确查询任务组。
     * SELECT * FROM t_ds_task_group WHERE user_id = #{userId} AND name = #{name}
     *
     * @param userId 用户ID
     * @param name 任务组名称
     * @return 任务组实体，若不存在则返回 null
     */
    TaskGroup queryByName(@Param("userId") int userId, @Param("name") String name);

    /**
     * 查询指定任务组中可用资源槽位数量（groupSize > useSize 的记录数）。
     * SELECT COUNT(*) FROM t_ds_task_group WHERE id = #{groupId} AND group_size > use_size
     *
     * @param groupId 任务组ID
     * @return 可用槽位数量
     */
    int selectAvailableCountById(@Param("groupId") int groupId);

    /**
     * 根据任务组ID和状态统计记录数。
     * SELECT COUNT(*) FROM t_ds_task_group WHERE id = #{id} AND status = #{status}
     *
     * @param id 任务组ID
     * @param status 任务组状态
     * @return 符合条件的记录数
     */
    int selectCountByIdStatus(@Param("id") int id,@Param("status") int status);

    /**
     * 按项目编码分页查询任务组，支持按用户ID列表和项目编码筛选。
     * SELECT * FROM t_ds_task_group WHERE user_id IN (...) AND project_code = #{projectCode}
     *
     * @param page 分页对象
     * @param ids 用户ID列表
     * @param projectCode 项目编码
     * @return 任务组分页结果
     */
    IPage<TaskGroup> queryTaskGroupPagingByProjectCode(Page<TaskGroup> page, @Param("ids") List<Integer> ids, @Param("projectCode") Long projectCode);

    /**
     * 查询指定用户已授权的任务组资源列表。
     * SELECT * FROM t_ds_task_group WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 已授权的任务组列表
     */
    List<TaskGroup> listAuthorizedResource(@Param("userId") int userId);
}
