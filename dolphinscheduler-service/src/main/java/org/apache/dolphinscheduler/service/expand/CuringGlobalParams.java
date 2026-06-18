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

package org.apache.dolphinscheduler.service.expand;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_EXECUTE_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 全局参数固化服务实现，负责处理流程实例和任务实例中的全局参数与本地参数。
 * <p>支持时间占位符扩展、业务时间计算和参数合并等功能。</p>
 */
@Component
public class CuringGlobalParams implements CuringParamsService {

    @Autowired
    private TimePlaceholderResolverExpandService timePlaceholderResolverExpandService;

    @Override
    public String convertParameterPlaceholders(String val, Map<String, String> allParamMap) {
        return ParameterUtils.convertParameterPlaceholders(val, allParamMap);
    }

    @Override
    public boolean timeFunctionNeedExpand(String placeholderName) {
        return timePlaceholderResolverExpandService.timeFunctionNeedExpand(placeholderName);
    }

    @Override
    public String timeFunctionExtension(Integer processInstanceId, String timezone, String placeholderName) {
        return timePlaceholderResolverExpandService.timeFunctionExtension(processInstanceId, timezone, placeholderName);
    }

    /**
     * 固化全局参数，判断是否需要进行外部扩展计算并获取计算结果。
     *
     * @param processInstanceId 流程实例ID
     * @param globalParamMap 全局参数映射表
     * @param globalParamList 全局参数列表
     * @param commandType 命令类型
     * @param scheduleTime 调度时间
     * @param timezone 时区
     * @return 固化后的全局参数JSON字符串
     */
    @Override
    public String curingGlobalParams(Integer processInstanceId, Map<String, String> globalParamMap,
                                     List<Property> globalParamList, CommandType commandType, Date scheduleTime,
                                     String timezone) {
        if (globalParamList == null || globalParamList.isEmpty()) {
            return null;
        }
        Map<String, String> globalMap = new HashMap<>();
        if (globalParamMap != null) {
            globalMap.putAll(globalParamMap);
        }
        Map<String, String> allParamMap = new HashMap<>();
        // If it is a complement, a complement time needs to be passed in, according to the task type
        Map<String, String> timeParams = BusinessTimeUtils.getBusinessTime(commandType, scheduleTime, timezone);

        if (timeParams != null) {
            allParamMap.putAll(timeParams);
        }
        allParamMap.putAll(globalMap);
        Set<Map.Entry<String, String>> entries = allParamMap.entrySet();
        Map<String, String> resolveMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String val = entry.getValue();
            if (val.startsWith(Constants.FUNCTION_START_WITH)) {
                String str = "";
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    str = timeFunctionExtension(processInstanceId, timezone, val);
                } else {
                    str = convertParameterPlaceholders(val, allParamMap);
                }
                resolveMap.put(entry.getKey(), str);
            }
        }
        globalMap.putAll(resolveMap);
        for (Property property : globalParamList) {
            String val = globalMap.get(property.getProp());
            if (val != null) {
                property.setValue(val);
            }
        }
        return JSONUtils.toJsonString(globalParamList);
    }

    /**
     * 参数解析准备，在Worker端合并全局参数和本地参数。
     * <p>将流程实例的全局参数与任务本地参数进行合并，处理时间占位符替换和业务时间计算。</p>
     *
     * @param taskInstance 任务实例
     * @param parameters 任务参数
     * @param processInstance 流程实例
     * @return 合并后的参数映射表
     */
    @Override
    public Map<String, Property> paramParsingPreparation(@NonNull TaskInstance taskInstance,
                                                         @NonNull AbstractParameters parameters,
                                                         @NonNull ProcessInstance processInstance) {
        // assign value to definedParams here
        Map<String, String> globalParamsMap = setGlobalParamsMap(processInstance);
        Map<String, Property> globalParams = ParamUtils.getUserDefParamsMap(globalParamsMap);
        CommandType commandType = processInstance.getCmdTypeIfComplement();
        Date scheduleTime = processInstance.getScheduleTime();

        // combining local and global parameters
        Map<String, Property> localParams = parameters.getInputLocalParametersMap();

        // stream pass params
        parameters.setVarPool(taskInstance.getVarPool());
        Map<String, Property> varParams = parameters.getVarPoolMap();

        if (MapUtils.isEmpty(globalParams) && MapUtils.isEmpty(localParams) && MapUtils.isEmpty(varParams)) {
            return null;
        }
        // if it is a complement,
        // you need to pass in the task instance id to locate the time
        // of the process instance complement
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        String timeZone = cmdParam.get(Constants.SCHEDULE_TIMEZONE);
        Map<String, String> params = BusinessTimeUtils.getBusinessTime(commandType, scheduleTime, timeZone);

        if (globalParamsMap != null) {
            params.putAll(globalParamsMap);
        }

        if (StringUtils.isNotBlank(taskInstance.getExecutePath())) {
            params.put(PARAMETER_TASK_EXECUTE_PATH, taskInstance.getExecutePath());
        }
        params.put(PARAMETER_TASK_INSTANCE_ID, Integer.toString(taskInstance.getId()));

        if (MapUtils.isNotEmpty(varParams)) {
            globalParams.putAll(varParams);
        }
        if (MapUtils.isNotEmpty(localParams)) {
            globalParams.putAll(localParams);
        }

        Iterator<Map.Entry<String, Property>> iter = globalParams.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();

            if (StringUtils.isNotEmpty(property.getValue())
                    && property.getValue().startsWith(Constants.FUNCTION_START_WITH)) {
                /**
                 *  local parameter refers to global parameter with the same name
                 *  note: the global parameters of the process instance here are solidified parameters,
                 *  and there are no variables in them.
                 */
                String val = property.getValue();
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    val = timeFunctionExtension(taskInstance.getProcessInstanceId(), timeZone, val);
                } else {
                    val = convertParameterPlaceholders(val, params);
                }
                property.setValue(val);
            }
        }
        if (MapUtils.isEmpty(globalParams)) {
            globalParams = new HashMap<>();
        }
        // put schedule time param to params map
        Map<String, Property> paramsMap = preBuildBusinessParams(processInstance);
        if (MapUtils.isNotEmpty(paramsMap)) {
            globalParams.putAll(paramsMap);
        }
        return globalParams;
    }

    private Map<String, String> setGlobalParamsMap(ProcessInstance processInstance) {
        Map<String, String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = processInstance.getGlobalParams();
        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONUtils.toList(globalParamsStr, Property.class);
            globalParamsMap
                    .putAll(globalParamsList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue)));
        }
        return globalParamsMap;
    }

    @Override
    public Map<String, Property> preBuildBusinessParams(ProcessInstance processInstance) {
        Map<String, Property> paramsMap = new HashMap<>();
        // replace variable TIME with $[YYYYmmddd...] in shell file when history run job and batch complement job
        if (processInstance.getScheduleTime() != null) {
            Date date = processInstance.getScheduleTime();
            String dateTime = DateUtils.format(date, DateConstants.PARAMETER_FORMAT_TIME, null);
            Property p = new Property();
            p.setValue(dateTime);
            p.setProp(DateConstants.PARAMETER_DATETIME);
            paramsMap.put(DateConstants.PARAMETER_DATETIME, p);
        }
        return paramsMap;
    }
}
