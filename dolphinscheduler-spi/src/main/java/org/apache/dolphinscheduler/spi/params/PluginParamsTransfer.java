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

package org.apache.dolphinscheduler.spi.params;

import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_FIELD;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_VALUE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 插件参数传输工具类，提供 {@link PluginParams} 对象与JSON格式之间的转换方法。
 * <p>
 * 主要功能包括：
 * <ul>
 *   <li>将插件参数列表序列化为JSON字符串</li>
 *   <li>将JSON字符串反序列化为插件参数列表</li>
 *   <li>从JSON字符串提取参数名-值映射</li>
 *   <li>根据模板生成带有实际值的完整参数列表</li>
 * </ul>
 */
public class PluginParamsTransfer {

    /**
     * 将插件参数列表转换为JSON字符串
     *
     * @param list 插件参数列表
     * @return JSON字符串
     */
    public static String transferParamsToJson(List<PluginParams> list) {
        return JSONUtils.toJsonString(list);
    }

    /**
     * 将JSON字符串转换为插件参数列表
     *
     * @param str JSON字符串
     * @return 插件参数列表
     */
    public static List<PluginParams> transferJsonToParamsList(String str) {
        return JSONUtils.toList(str, PluginParams.class);
    }

    /**
     * 将JSON字符串解析为参数名到参数值的映射
     *
     * @param paramsJsonStr 参数JSON字符串
     * @return 参数名到参数值的映射Map
     */
    public static Map<String, String> getPluginParamsMap(String paramsJsonStr) {
        List<PluginParams> pluginParams = transferJsonToParamsList(paramsJsonStr);
        Map<String, String> paramsMap = new HashMap<>();
        for (PluginParams param : pluginParams) {
            paramsMap.put(param.getName(), param.getValue() == null ? null : param.getValue().toString());
        }
        return paramsMap;
    }

    /**
     * 根据参数JSON字符串和模板生成完整的插件参数列表
     *
     * @param paramsJsonStr       参数值的JSON字符串（key-value形式）
     * @param pluginParamsTemplate 插件参数模板JSON字符串
     * @return 填充了实际值的参数列表
     */
    public static List<Map<String, Object>> generatePluginParams(String paramsJsonStr, String pluginParamsTemplate) {
        Map<String, Object> paramsMap = JSONUtils.toMap(paramsJsonStr, String.class, Object.class);
        return generatePluginParams(paramsMap, pluginParamsTemplate);
    }

    /**
     * 根据参数Map和模板生成完整的插件参数列表。
     * 实现原理：将模板中的每个参数项填充上paramsMap中对应的实际值。
     *
     * @param paramsMap            参数名到参数值的映射
     * @param pluginParamsTemplate 插件参数模板JSON字符串
     * @return 填充了实际值的参数列表，如果paramsMap为空则返回null
     */
    public static List<Map<String, Object>> generatePluginParams(Map<String, Object> paramsMap, String pluginParamsTemplate) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> pluginParamsList = JSONUtils.parseObject(pluginParamsTemplate, new TypeReference<List<Map<String, Object>>>() {});
        pluginParamsList.forEach(pluginParams -> pluginParams.put(STRING_PLUGIN_PARAM_VALUE, paramsMap.get(pluginParams.get(STRING_PLUGIN_PARAM_FIELD))));
        return pluginParamsList;
    }
}
