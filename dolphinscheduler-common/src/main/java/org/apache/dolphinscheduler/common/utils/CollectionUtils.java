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

package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.beanutils.BeanMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 集合操作工具类，提供Collection相关的实用方法。
 * 支持对集合中的对象属性进行过滤和转换操作。
 * 该类为工具类，不可实例化。
 */
public class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("Construct CollectionUtils");
    }

    /**
     * 从对象列表中移除指定属性，返回仅包含保留属性的Map列表。
     *
     * @param originList 原始对象列表
     * @param exclusionSet 需要排除的属性名称集合
     * @param <T> 对象类型
     * @return 过滤后的Map列表，每个Map仅包含未被排除的属性
     */
    public static <T extends Object> List<Map<String, Object>> getListByExclusion(List<T> originList, Set<String> exclusionSet) {
        List<Map<String, Object>> instanceList = new ArrayList<>();
        if (originList == null) {
            return instanceList;
        }
        Map<String, Object> instanceMap;
        for (T instance : originList) {
            BeanMap beanMap = new BeanMap(instance);
            instanceMap = new LinkedHashMap<>(16, 0.75f, true);
            for (Map.Entry<Object, Object> entry : beanMap.entrySet()) {
                if (exclusionSet != null && exclusionSet.contains(entry.getKey())) {
                    continue;
                }
                instanceMap.put((String) entry.getKey(), entry.getValue());
            }
            instanceList.add(instanceMap);
        }
        return instanceList;
    }

}
