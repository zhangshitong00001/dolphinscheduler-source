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

package org.apache.dolphinscheduler.spi.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * SPI插件标识类，封装插件的名称和优先级信息。
 * <p>
 * 在SPI加载过程中，系统根据名称（name）判断是否存在同名插件，
 * 根据优先级（priority）决定加载哪个插件。优先级数值越大，优先级越高。
 * 默认优先级为0。
 */
@Data
@Builder
@AllArgsConstructor
public class SPIIdentify {

    /** 默认优先级 */
    private static final int DEFAULT_PRIORITY = 0;

    /** 插件名称，用于唯一标识插件类型 */
    private String name;

    /** 插件优先级，数值越大优先级越高 */
    @Builder.Default
    private int priority = DEFAULT_PRIORITY;

}
