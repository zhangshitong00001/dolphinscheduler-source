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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.K8sNamespace;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * K8s 命名空间 Mapper 接口，封装对 t_ds_k8s_namespace 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供 Kubernetes 命名空间的分页查询、存在性校验及授权管理等功能。
 */
public interface K8sNamespaceMapper extends BaseMapper<K8sNamespace> {

    /**
     * 分页查询 K8s 命名空间列表，支持按命名空间名称进行 LIKE 模糊搜索。
     *
     * @param page 分页对象
     * @param searchVal 搜索关键字
     * @return K8s 命名空间分页结果
     */
    IPage<K8sNamespace> queryK8sNamespacePaging(IPage<K8sNamespace> page,
                                                @Param("searchVal") String searchVal);

    /**
     * 判断指定命名空间名称和集群编码的组合是否已存在。
     * SELECT COUNT(*) > 0 FROM t_ds_k8s_namespace WHERE namespace = #{namespace} AND cluster_code = #{clusterCode}
     *
     * @param namespace 命名空间名称
     * @param clusterCode 集群编码
     * @return 存在返回 true，否则返回 false
     */
    Boolean existNamespace(@Param("namespace") String namespace, @Param("clusterCode") Long clusterCode);

    /**
     * 查询除指定用户之外的所有命名空间列表。
     * 用于查找可以授权给当前用户的其他用户命名空间。
     *
     * @param userId 要排除的用户ID
     * @return 命名空间列表
     */
    List<K8sNamespace> queryNamespaceExceptUserId(@Param("userId") int userId);

    /**
     * 查询指定用户已授权访问的命名空间列表。
     * 通过 JOIN t_ds_relation_namespace_user 表查找用户有权限的命名空间。
     *
     * @param userId 用户ID
     * @return 用户授权命名空间列表
     */
    List<K8sNamespace> queryAuthedNamespaceListByUserId(@Param("userId") Integer userId);

    /**
     * 根据命名空间编码查询 K8s 命名空间信息。
     * SELECT * FROM t_ds_k8s_namespace WHERE code = #{clusterCode}
     *
     * @param namespaceCode 命名空间编码
     * @return 命名空间实体，若不存在则返回 null
     */
    K8sNamespace queryByNamespaceCode(@Param("clusterCode") Long namespaceCode);
}
