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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 集群配置工具类，用于解析数据库中存储的集群配置信息。当前仅支持Kubernetes（K8s）配置的解析，
 * 后续可扩展支持其他集群环境的配置。
 */
public class ClusterConfUtils {

    private static final String K8S_CONFIG = "k8s";

    /**
     * 从数据库存储的集群配置JSON中提取K8s配置。
     *
     * @param config the cluster config JSON string from database
     * @return the K8s configuration string, or null if not found
     */
    public static String getK8sConfig(String config) {
        if (StringUtils.isEmpty(config)) {
            return null;
        }
        ObjectNode conf = JSONUtils.parseObject(config);
        if (conf == null) {
            return null;
        }
        return conf.get(K8S_CONFIG).asText();
    }

}
