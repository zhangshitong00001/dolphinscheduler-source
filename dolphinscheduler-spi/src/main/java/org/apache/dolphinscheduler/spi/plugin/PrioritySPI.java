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

/**
 * 带优先级的SPI插件接口，所有通过SPI机制加载的插件均应实现此接口。
 * <p>
 * 插件通过 {@link #getIdentify()} 返回唯一标识（名称+优先级）。
 * 当多个插件具有相同名称时，优先级较高的插件会覆盖优先级较低的插件；
 * 如果名称和优先级都相同，则抛出 {@code IllegalArgumentException}。
 * <p>
 * 实现此接口的插件类需要在 {@code META-INF/services} 中进行注册，
 * 然后通过 {@link PrioritySPIFactory} 进行加载。
 */
public interface PrioritySPI extends Comparable<Integer> {

    /**
     * 获取插件的SPI标识信息（名称和优先级）
     *
     * @return 包含插件名称和优先级的标识对象
     */
    SPIIdentify getIdentify();

    @Override
    default int compareTo(Integer o) {
        return Integer.compare(getIdentify().getPriority(), o);
    }

}
