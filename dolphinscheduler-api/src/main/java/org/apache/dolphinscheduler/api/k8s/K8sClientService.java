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

import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * K8s客户端服务。封装所有与Kubernetes集群交互的客户端操作，不涉及数据库访问。
 * 提供命名空间的创建、删除、查询以及资源配额的更新等操作。
 */
@Component
public class K8sClientService {

    private static Yaml yaml = new Yaml();
    @Autowired
    private K8sManager k8sManager;

    /**
     * 在K8s集群中创建或更新命名空间及其资源配额。
     *
     * @param k8sNamespace K8s命名空间实体，包含命名空间名称和集群编码
     * @param yamlStr      资源配额的YAML配置字符串
     * @return 创建或更新后的ResourceQuota对象
     * @throws RemotingException 与K8s通信异常时抛出
     */
    public ResourceQuota upsertNamespaceAndResourceToK8s(K8sNamespace k8sNamespace, String yamlStr) throws RemotingException {
        upsertNamespaceToK8s(k8sNamespace.getNamespace(), k8sNamespace.getClusterCode());
        return upsertNamespacedResourceToK8s(k8sNamespace, yamlStr);
    }

    /**
     * 从K8s集群中删除指定命名空间。
     *
     * @param name        命名空间名称
     * @param clusterCode 集群编码
     * @return 删除后如果仍然存在该命名空间则返回之，否则返回Optional.empty()
     * @throws RemotingException 与K8s通信异常时抛出
     */
    public Optional<Namespace> deleteNamespaceToK8s(String name, Long clusterCode) throws RemotingException {
        Optional<Namespace> result = getNamespaceFromK8s(name, clusterCode);
        if (result.isPresent()) {
            KubernetesClient client = k8sManager.getK8sClient(clusterCode);
            Namespace body = new Namespace();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(name);
            meta.setName(name);
            body.setMetadata(meta);
            client.namespaces().delete(body);
        }
        return getNamespaceFromK8s(name, clusterCode);
    }

    /**
     * 在K8s命名空间内创建或更新资源配额。
     *
     * @param k8sNamespace K8s命名空间实体
     * @param yamlStr      资源配额的YAML配置字符串
     * @return 创建或更新后的ResourceQuota对象，如果无CPU和内存限制则删除并返回null
     * @throws RemotingException 与K8s通信异常时抛出
     */
    private ResourceQuota upsertNamespacedResourceToK8s(K8sNamespace k8sNamespace, String yamlStr) throws RemotingException {

        KubernetesClient client = k8sManager.getK8sClient(k8sNamespace.getClusterCode());

        //创建资源
        ResourceQuota queryExist = client.resourceQuotas()
            .inNamespace(k8sNamespace.getNamespace())
            .withName(k8sNamespace.getNamespace())
            .get();

        ResourceQuota body = yaml.loadAs(yamlStr, ResourceQuota.class);

        if (queryExist != null) {
            if (k8sNamespace.getLimitsCpu() == null && k8sNamespace.getLimitsMemory() == null) {
                client.resourceQuotas().inNamespace(k8sNamespace.getNamespace())
                    .withName(k8sNamespace.getNamespace())
                    .delete();
                return null;
            }
        }

        return client.resourceQuotas().inNamespace(k8sNamespace.getNamespace())
            .withName(k8sNamespace.getNamespace())
            .createOrReplace(body);
    }

    /**
     * 从K8s集群中查询指定的命名空间。
     *
     * @param name        命名空间名称
     * @param clusterCode 集群编码
     * @return 包含Namespace的Optional对象，不存在则为Optional.empty()
     * @throws RemotingException 与K8s通信异常时抛出
     */
    private Optional<Namespace> getNamespaceFromK8s(String name, Long clusterCode) throws RemotingException {
        NamespaceList listNamespace =
            k8sManager.getK8sClient(clusterCode).namespaces().list();

        Optional<Namespace> list =
            listNamespace.getItems().stream()
                .filter((Namespace namespace) ->
                    namespace.getMetadata().getName().equals(name))
                .findFirst();

        return list;
    }

    /**
     * 在K8s集群中创建或获取命名空间。如果命名空间不存在则创建，如果已存在则返回已有。
     *
     * @param name        命名空间名称
     * @param clusterCode 集群编码
     * @return 创建后或已存在的Namespace对象
     * @throws RemotingException 与K8s通信异常时抛出
     */
    private Namespace upsertNamespaceToK8s(String name, Long clusterCode) throws RemotingException {
        Optional<Namespace> result = getNamespaceFromK8s(name, clusterCode);
        //if not exist create
        if (!result.isPresent()) {
            KubernetesClient client = k8sManager.getK8sClient(clusterCode);
            Namespace body = new Namespace();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(name);
            meta.setName(name);
            body.setMetadata(meta);
            return client.namespaces().create(body);
        }
        return result.get();
    }

}