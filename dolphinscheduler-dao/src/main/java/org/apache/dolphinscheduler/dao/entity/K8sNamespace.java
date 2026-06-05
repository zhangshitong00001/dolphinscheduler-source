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

package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * K8s 命名空间实体，映射到 t_ds_k8s_namespace 表，表示 Kubernetes 集群中的一个命名空间及其资源配置。
 * 用于限制在该命名空间中运行任务时 Pod 的资源使用量（CPU、内存）和 Pod 副本数。
 */
@Data
@TableName("t_ds_k8s_namespace")
public class K8sNamespace {

    /** K8s 命名空间主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 命名空间编码，全局唯一标识 */
    private Long code;

    /** K8s 命名空间名称 */
    @TableField(value = "namespace")
    private String namespace;

    /** 总 CPU 资源上限，单位为核（Core） */
    @TableField(value = "limits_cpu")
    private Double limitsCpu;

    /** 总内存资源上限，单位为 MiB */
    private Integer limitsMemory;

    /** 命名空间所有者用户 ID，对应 t_ds_user 表的 id */
    @TableField(value = "user_id")
    private int userId;

    /** 非数据库字段：所有者用户名，通过 userId 关联 t_ds_user 表查询填充 */
    @TableField(exist = false)
    private String userName;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 最后更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 单个 Pod 请求的 CPU 资源，1.00 = 1 核，默认为 0 */
    @TableField("pod_request_cpu")
    private Double podRequestCpu = 0.0;

    /** 单个 Pod 请求的内存资源，单位为 MiB，默认为 0 */
    @TableField("pod_request_memory")
    private Integer podRequestMemory = 0;

    /** Pod 副本数，默认为 0 */
    @TableField("pod_replicas")
    private Integer podReplicas = 0;

    /** 关联的集群编码，对应 t_ds_cluster 表的 code */
    @TableField("cluster_code")
    private Long clusterCode;

    /** 非数据库字段：集群名称，通过 clusterCode 关联 t_ds_cluster 表查询填充 */
    @TableField(exist = false)
    private String clusterName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        K8sNamespace k8sNamespace = (K8sNamespace) o;

        if (id.equals(k8sNamespace.id)) {
            return true;
        }

        return namespace.equals(k8sNamespace.namespace) && clusterName.equals(k8sNamespace.clusterName);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (clusterName + namespace).hashCode();
        return result;
    }
}
