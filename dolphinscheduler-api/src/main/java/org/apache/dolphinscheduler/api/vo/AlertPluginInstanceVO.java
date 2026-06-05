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

package org.apache.dolphinscheduler.api.vo;

import java.util.Date;

import lombok.Data;

/**
 * 告警插件实例视图对象。用于返回告警插件实例的前端展示数据。
 */
@Data
public class AlertPluginInstanceVO {

    /**
     * 告警插件实例ID
     */
    private int id;

    /**
     * 插件定义ID
     */
    private int pluginDefineId;

    /**
     * 告警插件实例名称
     */
    private String instanceName;

    /**
     * 插件实例参数
     */
    private String pluginInstanceParams;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 告警插件名称
     */
    private String alertPluginName;
}
