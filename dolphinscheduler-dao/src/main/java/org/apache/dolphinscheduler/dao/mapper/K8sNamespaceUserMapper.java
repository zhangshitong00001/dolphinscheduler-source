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

import org.apache.dolphinscheduler.dao.entity.K8sNamespaceUser;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * K8s 命名空间-用户关联 Mapper 接口，封装对 t_ds_relation_namespace_user 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，管理 K8s 命名空间与用户之间的授权关联关系。
 */
public interface K8sNamespaceUserMapper extends BaseMapper<K8sNamespaceUser> {

    /**
     * 删除指定命名空间与用户的关联授权记录。
     * DELETE FROM t_ds_relation_namespace_user WHERE namespace_id = #{namespaceId} AND user_id = #{userId}
     *
     * @param namespaceId K8s 命名空间ID
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteNamespaceRelation(@Param("namespaceId") int namespaceId,
                                @Param("userId") int userId);

    /**
     * 查询指定命名空间与用户的关联授权记录。
     * SELECT * FROM t_ds_relation_namespace_user WHERE namespace_id = #{namespaceId} AND user_id = #{userId}
     *
     * @param namespaceId K8s 命名空间ID
     * @param userId 用户ID
     * @return 命名空间-用户关联实体，若不存在则返回 null
     */
    K8sNamespaceUser queryNamespaceRelation(@Param("namespaceId") int namespaceId,
                                            @Param("userId") int userId);
}
