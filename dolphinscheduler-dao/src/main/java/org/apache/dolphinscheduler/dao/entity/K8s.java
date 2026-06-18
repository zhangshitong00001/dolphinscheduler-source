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
 * Kubernetes 集群配置实体，映射到 t_ds_k8s 表，表示一个 Kubernetes 集群的连接配置。
 * 存储 K8s 客户端配置文件（kubeconfig），用于创建和管理 K8s 命名空间、Pod 等资源。
 */
@Data
@TableName("t_ds_k8s")
public class K8s {

    /** K8s 集群配置主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** K8s 集群名称，用户自定义的标识名 */
    @TableField(value = "k8s_name")
    private String k8sName;
    /** K8s 客户端配置内容，YAML 或 JSON 格式的 kubeconfig */
    @TableField(value = "k8s_config")
    private String k8sConfig;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
