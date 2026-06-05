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

package org.apache.dolphinscheduler.spi.datasource;

import org.apache.dolphinscheduler.spi.plugin.PrioritySPI;
import org.apache.dolphinscheduler.spi.plugin.SPIIdentify;

/**
 * 数据源通道工厂接口，继承自 {@link PrioritySPI}，用于通过SPI机制加载各数据库类型的通道工厂。
 * <p>
 * 每种数据库类型需要实现此接口并在 {@code META-INF/services} 中注册，
 * 工厂负责创建对应的 {@link DataSourceChannel} 实例。
 * 当存在多个同名工厂时，优先级高的会被加载。
 */
public interface DataSourceChannelFactory extends PrioritySPI {
    /**
     * 创建数据源通道实例
     *
     * @return 该数据库类型对应的数据源通道
     */
    DataSourceChannel create();

    /**
     * 获取数据源通道工厂的名称（通常对应数据库类型名称）
     *
     * @return 工厂名称
     */
    String getName();

    @Override
    default SPIIdentify getIdentify() {
        return SPIIdentify.builder().name(getName()).build();
    }
}
