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
 * K8s 命名空间与用户关联实体，映射到 t_ds_relation_namespace_user 表，表示用户与 Kubernetes 命名空间之间的权限关系。
 */
@Data
@TableName("t_ds_relation_namespace_user")
public class K8sNamespaceUser {

    /** 主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户 ID */
    @TableField("user_id")
    private int userId;

    /** 命名空间 ID */
    @TableField("namespace_id")
    private int namespaceId;

    /** 非数据库字段：K8s 集群标识 */
    @TableField(exist = false)
    private String k8s;

    /** 非数据库字段：命名空间名称 */
    @TableField(exist = false)
    private String namespaceName;

    /** 非数据库字段：用户名 */
    @TableField(exist = false)
    private String userName;

    /** 权限值 */
    private int perm;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
