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

import org.apache.dolphinscheduler.dao.entity.PluginDefine;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 插件定义 Mapper 接口，封装对 t_ds_plugin_define 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供 DolphinScheduler 插件定义（如告警插件、任务插件）的查询能力。
 */
public interface PluginDefineMapper extends BaseMapper<PluginDefine> {

    /**
     * 检查插件定义表是否存在。
     * 用于数据库升级/初始化时的表结构校验。
     *
     * @return 大于0表示表存在
     */
    int checkTableExist();

    /**
     * 查询所有插件定义列表。
     * SELECT * FROM t_ds_plugin_define
     *
     * @return 全部插件定义列表
     */
    List<PluginDefine> queryAllPluginDefineList();

    /**
     * 根据插件类型查询该类型下的所有插件定义。
     * SELECT * FROM t_ds_plugin_define WHERE plugin_type = #{pluginType}
     *
     * @param pluginType 插件类型（如 ALERT、TASK）
     * @return 该类型的插件定义列表
     */
    List<PluginDefine> queryByPluginType(@Param("pluginType") String pluginType);

    /**
     * 根据ID查询插件定义详情。
     * SELECT * FROM t_ds_plugin_define WHERE id = #{id}
     *
     * @param id 插件定义ID
     * @return 插件定义实体
     */
    PluginDefine queryDetailById(@Param("id") int id);

    /**
     * 根据插件名称和插件类型精确查询插件定义。
     * SELECT * FROM t_ds_plugin_define WHERE plugin_name = #{pluginName} AND plugin_type = #{pluginType}
     *
     * @param pluginName 插件名称
     * @param pluginType 插件类型
     * @return 插件定义实体，若不存在则返回 null
     */
    PluginDefine queryByNameAndType(@Param("pluginName") String pluginName, @Param("pluginType") String pluginType);
}
