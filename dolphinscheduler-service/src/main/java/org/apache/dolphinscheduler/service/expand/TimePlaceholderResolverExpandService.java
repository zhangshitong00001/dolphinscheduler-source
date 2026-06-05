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

/**
 * 时间占位符解析扩展服务接口，允许用户自定义时间函数的扩展计算逻辑。
 */
public interface TimePlaceholderResolverExpandService {

    /**
     * 检查当前时间占位符是否需要执行扩展计算。
     *
     * @param placeholderName 占位符名称
     * @return 需要扩展返回true，否则返回false
     */
    boolean timeFunctionNeedExpand(String placeholderName);

    /**
     * 执行时间函数扩展计算，根据流程实例上下文返回计算结果。
     *
     * @param processInstanceId 流程实例ID
     * @param timeZone 时区
     * @param placeholderName 占位符名称
     * @return 扩展计算后的结果字符串
     */
    String timeFunctionExtension(Integer processInstanceId, String timeZone, String placeholderName);
}
