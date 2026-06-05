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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.configuration.TaskTypeConfiguration;
import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.api.service.FavTaskService;
import org.apache.dolphinscheduler.dao.entity.FavTask;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.FavTaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 收藏任务服务实现类。负责用户收藏任务类型的查询、添加和删除，结合默认任务类型配置返回收藏状态。
 */
@Service
public class FavTaskServiceImpl extends BaseServiceImpl implements FavTaskService {

    @Resource
    private TaskTypeConfiguration taskTypeConfiguration;
    @Resource
    private FavTaskMapper favMapper;

    /**
     * 获取用户的收藏任务列表，包含默认任务类型及其收藏状态。
     *
     * @param loginUser 当前登录用户
     * @return 包含收藏状态的任务类型DTO列表
     */
    @Override
    public List<FavTaskDto> getFavTaskList(User loginUser) {
        List<FavTaskDto> result = new ArrayList<>();
        Set<String> userFavTaskTypes = favMapper.getUserFavTaskTypes(loginUser.getId());

        Set<FavTaskDto> defaultTaskTypes = taskTypeConfiguration.getDefaultTaskTypes();
        defaultTaskTypes.forEach(e -> {
            if (userFavTaskTypes.contains(e.getTaskName())) {
                e.setCollection(true);
            }
            result.add(e);
        });
        return result;
    }

    /**
     * 删除用户对指定任务类型的收藏。
     *
     * @param loginUser 当前登录用户
     * @param taskName 任务类型名称
     * @return true表示删除成功，false表示删除失败
     */
    @Override
    public boolean deleteFavTask(User loginUser, String taskName) {
        return favMapper.deleteUserFavTask(loginUser.getId(), taskName);
    }

    /**
     * 添加用户对指定任务类型的收藏。先删除已有记录再插入新记录，实现幂等操作。
     *
     * @param loginUser 当前登录用户
     * @param taskName 任务类型名称
     * @return 插入操作影响的行数
     */
    @Override
    public int addFavTask(User loginUser, String taskName) {
        favMapper.deleteUserFavTask(loginUser.getId(), taskName);
        return favMapper.insert(new FavTask(null, taskName, loginUser.getId()));
    }
}
