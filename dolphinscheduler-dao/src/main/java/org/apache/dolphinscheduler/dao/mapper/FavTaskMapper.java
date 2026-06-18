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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.FavTask;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * 收藏任务 Mapper 接口，封装对 t_ds_fav_task 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，管理用户收藏的任务类型，提供查询和删除收藏的能力。
 */
public interface FavTaskMapper extends BaseMapper<FavTask> {

    /**
     * 查询指定用户收藏的所有任务类型名称集合。
     * SELECT DISTINCT task_name FROM t_ds_fav_task WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 用户收藏的任务类型名称集合
     */
    Set<String> getUserFavTaskTypes(@Param("userId") int userId);

    /**
     * 删除指定用户对某个任务类型的收藏记录。
     * DELETE FROM t_ds_fav_task WHERE user_id = #{userId} AND task_name = #{taskName}
     *
     * @param userId 用户ID
     * @param taskName 任务类型名称
     * @return 删除成功返回 true，否则返回 false
     */
    boolean deleteUserFavTask(@Param("userId") int userId, @Param("taskName") String taskName);
}
