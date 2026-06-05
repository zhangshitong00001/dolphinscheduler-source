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

package org.apache.dolphinscheduler.spi.common;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

/**
 * UI通道工厂接口，定义了前端页面上可配置参数的插件契约。
 * <p>
 * 所有需要在前端UI上展示参数配置的插件（如告警插件、任务插件等）
 * 都需要实现此接口，提供插件名称和参数列表供前端动态渲染表单。
 * 插件名称必须全局唯一。
 */
public interface UiChannelFactory {

    /**
     * 获取插件名称，必须全局唯一。
     * 该名称通常也用于前端页面展示，如 email、message、MR、spark、hive 等。
     *
     * @return 插件名称
     */
    String getName();

    /**
     * 返回该插件需要在前端页面展示的可配置参数列表
     *
     * @return 插件参数列表
     */
    List<PluginParams> getParams();

}
