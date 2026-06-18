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

import lombok.NonNull;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 参数固化服务接口，定义全局参数和本地参数的固化、解析以及时间占位符处理方法。
 */
public interface CuringParamsService {

    /**
     * 判断时间函数是否需要执行外部扩展。
     *
     * @param placeholderName 占位符名称
     * @return 需要扩展返回true，否则返回false
     */
    boolean timeFunctionNeedExpand(String placeholderName);

    /**
     * 执行时间函数的外部扩展计算。
     *
     * @param processInstanceId 流程实例ID
     * @param timezone 时区
     * @param placeholderName 占位符名称
     * @return 扩展计算后的结果字符串
     */
    String timeFunctionExtension(Integer processInstanceId, String timezone, String placeholderName);

    /**
     * 转换参数中的占位符为实际值。
     *
     * @param val 包含占位符的原始值
     * @param allParamMap 所有参数的映射表
     * @return 替换占位符后的字符串
     */
    String convertParameterPlaceholders(String val, Map<String, String> allParamMap);

    /**
     * 固化全局参数，将时间函数和占位符替换为实际值。
     *
     * @param processInstanceId 流程实例ID
     * @param globalParamMap 全局参数映射表
     * @param globalParamList 全局参数列表
     * @param commandType 命令类型
     * @param scheduleTime 调度时间
     * @param timezone 时区
     * @return 固化后的全局参数JSON字符串
     */
    String curingGlobalParams(Integer processInstanceId, Map<String, String> globalParamMap, List<Property> globalParamList, CommandType commandType, Date scheduleTime, String timezone);

    /**
     * 参数解析准备，在Worker端合并全局参数和本地参数并完成占位符替换。
     *
     * @param taskInstance 任务实例
     * @param parameters 任务参数
     * @param processInstance 流程实例
     * @return 合并后的参数映射表
     */
    Map<String, Property> paramParsingPreparation(@NonNull TaskInstance taskInstance, @NonNull AbstractParameters parameters, @NonNull ProcessInstance processInstance);

    /**
     * 预构建业务参数，提取流程实例中的时间相关参数。
     *
     * @param processInstance 流程实例
     * @return 业务参数映射表
     */
    Map<String, Property> preBuildBusinessParams(ProcessInstance processInstance);
}
