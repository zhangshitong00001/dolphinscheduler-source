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

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 告警插件实例实体，映射到 t_ds_alert_plugin_instance 表，表示一个告警通道的具体配置实例。
 * 例如一个邮件告警实例会包含 SMTP 服务器地址、邮箱账号等配置参数。告警插件实例被告警组引用，用于实际发送告警通知。
 */
@Data
@TableName("t_ds_alert_plugin_instance")
public class AlertPluginInstance {

    /** 告警插件实例主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关联的插件定义 ID，对应 t_ds_plugin_define 表的 id，标识使用哪个告警插件类型（如邮件、短信、钉钉等），更新策略为 NEVER（不允许修改） */
    @TableField(value = "plugin_define_id", updateStrategy = FieldStrategy.NEVER)
    private int pluginDefineId;

    /** 告警插件实例名称，用户自定义，如 "生产环境邮件告警" */
    @TableField("instance_name")
    private String instanceName;

    /** 插件实例参数字符串，JSON 格式，存储该告警实例的具体配置参数 */
    @TableField("plugin_instance_params")
    private String pluginInstanceParams;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 最后更新时间 */
    @TableField("update_time")
    private Date updateTime;

    public AlertPluginInstance() {
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public AlertPluginInstance(int pluginDefineId, String pluginInstanceParams, String instanceName) {
        this.pluginDefineId = pluginDefineId;
        this.pluginInstanceParams = pluginInstanceParams;
        this.createTime = new Date();
        this.updateTime = new Date();
        this.instanceName = instanceName;
    }

    public AlertPluginInstance(int id, String pluginInstanceParams, String instanceName, Date updateDate) {
        this.id = id;
        this.pluginInstanceParams = pluginInstanceParams;
        this.updateTime = updateDate;
        this.instanceName = instanceName;
    }
}
