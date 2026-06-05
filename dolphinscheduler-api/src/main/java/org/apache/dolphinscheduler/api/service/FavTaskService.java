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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * 收藏任务服务接口。提供用户收藏任务的管理功能，包括添加收藏、删除收藏和查询收藏列表。
 * 用于快速访问常用或重要的任务定义。
 */
public interface FavTaskService {

    /**
     * 获取当前用户的收藏任务列表。
     *
     * @param loginUser 登录用户
     * @return 收藏任务DTO列表
     */
    List<FavTaskDto> getFavTaskList(User loginUser);

    /**
     * 删除指定任务名的收藏记录。
     *
     * @param loginUser 登录用户
     * @param taskName  任务名称
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteFavTask(User loginUser, String taskName);

    /**
     * 添加任务到收藏列表。
     *
     * @param loginUser 登录用户
     * @param taskName  任务名称
     * @return 添加结果，返回受影响行数
     */
    int addFavTask(User loginUser, String taskName);
}
