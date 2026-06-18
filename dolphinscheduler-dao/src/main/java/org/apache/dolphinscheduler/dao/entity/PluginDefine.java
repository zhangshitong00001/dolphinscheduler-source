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

package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 插件定义实体，映射到 t_ds_plugin_define 表，表示系统中注册的一个插件及其参数配置信息。
 */
@Data
@TableName("t_ds_plugin_define")
public class PluginDefine {

    /** 插件主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 插件名称 */
    @TableField("plugin_name")
    private String pluginName;

    /** 插件类型 */
    @TableField("plugin_type")
    private String pluginType;

    /** 插件参数（JSON 格式） */
    @TableField("plugin_params")
    private String pluginParams;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    public PluginDefine(String pluginName, String pluginType, String pluginParams) {
        this.pluginName = pluginName;
        this.pluginType = pluginType;
        this.pluginParams = pluginParams;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}
