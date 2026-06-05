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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * 告警组服务接口。提供告警组的创建、查询、更新、删除以及名称校验等功能。
 * 告警组用于将多个告警插件实例组合在一起，统一管理告警通知的发送。
 */
public interface AlertGroupService {

    /**
     * 查询当前用户的告警组列表。
     *
     * @param loginUser 登录用户
     * @return 告警组列表
     */
    Map<String, Object> queryAlertgroup(User loginUser);

    /**
     * 根据ID查询告警组详情。
     *
     * @param loginUser 登录用户
     * @param id        告警组ID
     * @return 单个告警组信息
     */
    Map<String, Object> queryAlertGroupById(User loginUser, Integer id);

    /**
     * 分页查询告警组列表。
     *
     * @param loginUser 登录用户
     * @param searchVal 搜索关键字
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * 创建告警组。
     *
     * @param loginUser        登录用户
     * @param groupName        告警组名称
     * @param desc             描述
     * @param alertInstanceIds 告警插件实例ID列表
     * @return 创建结果
     */
    Map<String, Object> createAlertgroup(User loginUser, String groupName, String desc, String alertInstanceIds);

    /**
     * 更新告警组信息。
     *
     * @param loginUser        登录用户
     * @param id               告警组ID
     * @param groupName        告警组名称
     * @param desc             描述
     * @param alertInstanceIds 告警插件实例ID列表
     * @return 更新结果
     */
    Map<String, Object> updateAlertgroup(User loginUser, int id, String groupName, String desc, String alertInstanceIds);

    /**
     * 根据ID删除告警组。
     *
     * @param loginUser 登录用户
     * @param id        告警组ID
     * @return 删除结果
     */
    Map<String, Object> delAlertgroupById(User loginUser, int id);

    /**
     * 检查告警组名称是否已存在。
     *
     * @param groupName 告警组名称
     * @return 存在返回true，否则返回false
     */
    boolean existGroupName(String groupName);
}
