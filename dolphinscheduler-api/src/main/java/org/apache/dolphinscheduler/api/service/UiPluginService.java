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

import org.apache.dolphinscheduler.common.enums.PluginType;

import java.util.Map;

/**
 * UI插件服务接口。提供前端插件的查询功能，支持按插件类型查询和按ID查询插件详情。
 */
public interface UiPluginService {

    /**
     * 根据插件类型查询UI插件列表。
     *
     * @param pluginType 插件类型
     * @return 插件列表Map
     */
    Map<String, Object> queryUiPluginsByType(PluginType pluginType);

    /**
     * 根据插件ID查询UI插件详情。
     *
     * @param id 插件ID
     * @return 插件详情Map
     */
    Map<String, Object> queryUiPluginDetailById(int id);

}
