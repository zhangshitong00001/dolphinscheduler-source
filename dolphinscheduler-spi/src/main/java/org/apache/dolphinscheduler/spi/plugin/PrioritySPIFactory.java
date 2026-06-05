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

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 基于优先级机制的SPI插件工厂，通过JDK {@link ServiceLoader} 加载并管理SPI插件。
 * <p>
 * 加载规则：
 * <ul>
 *   <li>通过 {@code META-INF/services} 机制发现所有实现类</li>
 *   <li>同名插件按优先级竞争：优先级高的覆盖优先级低的</li>
 *   <li>若名称和优先级完全相同，抛出 {@link IllegalArgumentException}</li>
 * </ul>
 *
 * @param <T> 插件类型，必须实现 {@link PrioritySPI} 接口
 */
@Slf4j
public class PrioritySPIFactory<T extends PrioritySPI> {

    /** 存储插件名称到插件实例的映射 */
    private final Map<String, T> map = new HashMap<>();

    /**
     * 构造工厂并自动加载指定SPI类型的所有插件实现
     *
     * @param spiClass SPI接口的Class对象
     */
    public PrioritySPIFactory(Class<T> spiClass) {
        for (T t : ServiceLoader.load(spiClass)) {
            if (map.containsKey(t.getIdentify().getName())) {
                resolveConflict(t);
            } else {
                map.put(t.getIdentify().getName(), t);
            }
        }
    }

    /**
     * 获取所有已加载的SPI插件映射（只读）
     *
     * @return 插件名称到实例的不可变映射
     */
    public Map<String, T> getSPIMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * 处理同名插件的优先级冲突：高优先级覆盖低优先级，相同优先级则抛异常
     *
     * @param newSPI 新加载的SPI插件实例
     */
    private void resolveConflict(T newSPI) {
        SPIIdentify identify = newSPI.getIdentify();
        T oldSPI = map.get(identify.getName());

        if (newSPI.compareTo(oldSPI.getIdentify().getPriority()) == 0) {
            throw new IllegalArgumentException(String.format("These two spi plugins has conflict identify name with the same priority: %s, %s",
                    oldSPI.getIdentify(), newSPI.getIdentify()));
        } else if (newSPI.compareTo(oldSPI.getIdentify().getPriority()) > 0) {
            log.info("The {} plugin has high priority, will override {}", newSPI.getIdentify(), oldSPI);
            map.put(identify.getName(), newSPI);
        } else {
            log.info("The low plugin {} will be skipped", newSPI);
        }
    }
}
