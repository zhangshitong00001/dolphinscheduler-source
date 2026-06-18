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

import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 告警插件实例 Mapper 接口，封装对 t_ds_alert_plugin_instance 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供告警插件实例的查询、分页及存在性校验能力。
 */
public interface AlertPluginInstanceMapper extends BaseMapper<AlertPluginInstance> {

    /**
     * 查询所有告警插件实例列表。
     * SELECT * FROM t_ds_alert_plugin_instance
     *
     * @return 告警插件实例列表
     */
    List<AlertPluginInstance> queryAllAlertPluginInstanceList();

    /**
     * 根据告警插件实例ID列表批量查询对应的告警插件实例。
     * SELECT * FROM t_ds_alert_plugin_instance WHERE id IN (#{ids})
     *
     * @param ids 告警插件实例ID列表
     * @return 告警插件实例列表
     */
    List<AlertPluginInstance> queryByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据实例名称分页查询告警插件实例，支持 LIKE 模糊匹配。
     *
     * @param page 分页对象
     * @param instanceName 告警插件实例名称，用于模糊查询
     * @return 告警插件实例分页结果
     */
    IPage<AlertPluginInstance> queryByInstanceNamePage(Page page, @Param("instanceName") String instanceName);

    /**
     * 判断指定名称的告警插件实例是否已存在。
     * SELECT COUNT(*) > 0 FROM t_ds_alert_plugin_instance WHERE instance_name = #{instanceName}
     *
     * @param instanceName 实例名称
     * @return 存在返回 true，否则返回 false
     */
    Boolean existInstanceName(@Param("instanceName") String instanceName);
}
