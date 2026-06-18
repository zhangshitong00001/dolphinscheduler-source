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

package org.apache.dolphinscheduler.server.master.utils;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Maps;

/**
 * Switch 任务工具类，提供条件表达式的参数替换和 JavaScript 求值功能。
 * 用于 Switch 任务处理器中将条件表达式中的占位符替换为实际的全局参数和变量参数，
 * 然后通过 JavaScript 脚本引擎对表达式求值以获得布尔结果。
 */
@Slf4j
public class SwitchTaskUtils {

    private static ScriptEngineManager manager;
    private static ScriptEngine engine;
    private static final String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";

    static {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
    }

    /**
     * 使用 JavaScript 脚本引擎对表达式进行求值。
     *
     * @param expression 条件表达式
     * @return 求值结果（布尔值）
     * @throws ScriptException 脚本执行异常
     */
    public static boolean evaluate(String expression) throws ScriptException {
        Object result = engine.eval(expression);
        return (Boolean) result;
    }

    /**
     * 将条件表达式中的 ${paramName} 占位符替换为实际的参数值，
     * 支持全局参数和变量参数，自动处理数值/布尔类型和字符串类型的引用格式。
     *
     * @param condition    原始条件表达式
     * @param globalParams 全局参数映射
     * @param varParams    变量参数映射
     * @return 替换参数后的条件表达式
     * @throws IllegalArgumentException 当参数为空或没有替换任何参数时抛出
     */
    public static String generateContentWithTaskParams(String condition, Map<String, Property> globalParams,
                                                       Map<String, Property> varParams) {
        String content = condition.replaceAll("'", "\"");
        if (MapUtils.isEmpty(globalParams) && MapUtils.isEmpty(varParams)) {
            throw new IllegalArgumentException("globalParams and varParams are both empty, please check it.");
        }
        Map<String, Property> params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(globalParams)) {
            params.putAll(globalParams);
        }
        if (MapUtils.isNotEmpty(varParams)) {
            params.putAll(varParams);
        }
        String originContent = content;
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            String paramName = m.group(1);
            Property property = params.get(paramName);
            if (property == null) {
                continue;
            }
            String value;
            if (ParameterUtils.isNumber(property) || ParameterUtils.isBoolean(property)) {
                value = "" + ParameterUtils.getParameterValue(property);
            } else {
                value = "\"" + ParameterUtils.getParameterValue(property) + "\"";
            }
            log.info("paramName:{}，paramValue:{}", paramName, value);
            content = content.replace("${" + paramName + "}", value);
        }

        // if not replace any params, throw exception to avoid illegal condition
        if (originContent.equals(content)) {
            throw new IllegalArgumentException("condition is not valid, please check it. condition: " + condition);
        }
        return content;
    }
}
