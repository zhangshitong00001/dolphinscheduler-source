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
package org.apache.dolphinscheduler.dao.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源与流程定义关联工具类，负责解析资源 ID 与流程定义编码的映射关系。
 */
public class ResourceProcessDefinitionUtils {
    /**
     * 构建资源 ID 到流程定义编码集合的映射。从资源列表中解析每个流程定义关联的资源 ID 并反转为资源视角的索引。
     *
     * @param resourceList the map key is process definition code and value is resource_ids
     * @return resource process definition map (resourceId -> processDefinitionCodes)
     */
    public static Map<Integer, Set<Long>> getResourceProcessDefinitionMap(List<Map<String, Object>> resourceList) {

        // resourceId -> processDefinitionCodes
        Map<Integer, Set<Long>> resourceResult = new HashMap<>();

        if (CollectionUtils.isNotEmpty(resourceList)) {
            for (Map<String, Object> resourceMap : resourceList) {
                Long code = (Long) resourceMap.get("code");
                String[] resourceIds = ((String) resourceMap.get("resource_ids"))
                        .split(",");

                Set<Integer> resourceIdSet = Arrays.stream(resourceIds).map(Integer::parseInt).collect(Collectors.toSet());
                for (Integer resourceId : resourceIdSet) {
                    Set<Long> codeSet;
                    if (resourceResult.containsKey(resourceId)) {
                        codeSet = resourceResult.get(resourceId);
                    } else {
                        codeSet = new HashSet<>();
                    }
                    codeSet.add(code);
                    resourceResult.put(resourceId, codeSet);
                }

            }
        }

        return resourceResult;
    }
}
