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

package org.apache.dolphinscheduler.api.k8s;

import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * K8s集群管理器。支持多集群环境，管理Kubernetes客户端连接的创建、缓存和生命周期。
 * 通过集群编码索引并复用客户端实例，减少重复连接开销。
 */
@Component
public class K8sManager {

    private static final Logger logger = LoggerFactory.getLogger(K8sManager.class);
    /**
     * cache k8s client
     */
    private static Map<Long, KubernetesClient> clientMap = new Hashtable<>();

    @Autowired
    private ClusterMapper clusterMapper;

    /**
     * 获取指定集群编码对应的K8s客户端，首次调用时从数据库读取配置并创建连接。
     *
     * @param clusterCode 集群编码
     * @return KubernetesClient实例，clusterCode为null时返回null
     * @throws RemotingException 与K8s通信异常时抛出
     */
    public synchronized KubernetesClient getK8sClient(Long clusterCode) throws RemotingException {
        if (null == clusterCode) {
            return null;
        }

        return getAndUpdateK8sClient(clusterCode, false);
    }

    /**
     * 获取或更新K8s客户端。当update为true时，先删除现有客户端再重新创建。
     *
     * @param clusterCode 集群编码
     * @param update      是否强制更新，true时删除现有客户端并重新创建
     * @return KubernetesClient实例，clusterCode为null时返回null
     * @throws RemotingException 与K8s通信异常时抛出
     */
    public synchronized KubernetesClient getAndUpdateK8sClient(Long clusterCode, boolean update) throws RemotingException {
        if (null == clusterCode) {
            return null;
        }

        if (update) {
            deleteK8sClientInner(clusterCode);
        }

        if (clientMap.containsKey(clusterCode)) {
            return clientMap.get(clusterCode);
        } else {
            createK8sClientInner(clusterCode);
        }
        return clientMap.get(clusterCode);
    }


    /**
     * 关闭并移除缓存中的K8s客户端连接。
     *
     * @param clusterCode 集群编码
     */
    private void deleteK8sClientInner(Long clusterCode) {
        if (clusterCode == null) {
            return;
        }
        Cluster cluster = clusterMapper.queryByClusterCode(clusterCode);
        if (cluster == null) {
            return;
        }
        KubernetesClient client = clientMap.get(clusterCode);
        if (client != null) {
            client.close();
        }
    }

    /**
     * 从数据库读取集群配置，创建K8s客户端并放入缓存。
     *
     * @param clusterCode 集群编码
     * @throws RemotingException 与K8s通信异常时抛出
     */
    private void createK8sClientInner(Long clusterCode) throws RemotingException {
        Cluster cluster = clusterMapper.queryByClusterCode(clusterCode);
        if (cluster == null) {
            return;
        }

        String k8sConfig = ClusterConfUtils.getK8sConfig(cluster.getConfig());
        if (k8sConfig != null) {
            DefaultKubernetesClient client = null;
            try {
                client = getClient(k8sConfig);
                clientMap.put(clusterCode, client);
            } catch (RemotingException e) {
                logger.error("cluster code ={},fail to get k8s ApiClient:  {}", clusterCode, e.getMessage());
                throw new RemotingException("fail to get k8s ApiClient:" + e.getMessage());
            }
        }
    }

    /**
     * 根据K8s配置YAML字符串创建DefaultKubernetesClient实例。
     *
     * @param configYaml K8s配置的YAML字符串
     * @return DefaultKubernetesClient实例
     * @throws RemotingException 创建客户端失败时抛出
     */
    private DefaultKubernetesClient getClient(String configYaml) throws RemotingException {
        try {
            Config config = Config.fromKubeconfig(configYaml);
            return new DefaultKubernetesClient(config);
        } catch (Exception e) {
            logger.error("fail to get k8s ApiClient", e);
            throw new RemotingException("fail to get k8s ApiClient:" + e.getMessage());
        }
    }

}
