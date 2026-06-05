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
 * 告警插件实例服务接口。提供告警插件实例的CRUD操作，用于管理各告警渠道（邮件、短信等）的配置实例。
 * 同一个告警插件可创建多个不同配置的实例。
 */
public interface AlertPluginInstanceService {

    /**
     * 创建告警插件实例。
     *
     * @param loginUser            登录用户
     * @param pluginDefineId       插件定义ID
     * @param instanceName         实例名称
     * @param pluginInstanceParams 插件实例参数
     * @return 创建结果
     */
    Map<String, Object> create(User loginUser,int pluginDefineId,String instanceName,String pluginInstanceParams);

    /**
     * 更新告警插件实例。
     *
     * @param loginUser              登录用户
     * @param alertPluginInstanceId  插件实例ID
     * @param instanceName           实例名称
     * @param pluginInstanceParams   插件实例参数
     * @return 更新结果
     */
    Map<String, Object> update(User loginUser, int alertPluginInstanceId,String instanceName,String pluginInstanceParams);

    /**
     * 删除告警插件实例。
     *
     * @param loginUser 登录用户
     * @param id        实例ID
     * @return 删除结果
     */
    Map<String, Object> delete(User loginUser, int id);

    /**
     * 根据ID获取告警插件实例。
     *
     * @param loginUser 登录用户
     * @param id        实例ID
     * @return 实例信息
     */
    Map<String, Object> get(User loginUser, int id);

    /**
     * 查询所有告警插件实例。
     *
     * @return 所有实例列表
     */
    Map<String, Object> queryAll();

    /**
     * 检查插件实例名称是否已存在。
     *
     * @param pluginName 插件实例名称
     * @return 存在返回true，否则返回false
     */
    boolean checkExistPluginInstanceName(String pluginName);

    /**
     * 分页查询告警插件实例。
     *
     * @param loginUser 登录用户
     * @param searchVal 搜索关键字
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 分页查询结果
     */
    Result listPaging(User loginUser, String searchVal, int pageNo, int pageSize);
}
