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

import org.apache.dolphinscheduler.dao.entity.ResourcesUser;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 资源-用户关系 Mapper 接口，封装对 t_ds_relation_resources_user 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供资源与用户授权关系的查询和删除能力。
 */
public interface ResourceUserMapper extends BaseMapper<ResourcesUser> {

    /**
     * 根据用户ID和权限级别查询该用户被授权的资源ID列表。
     * SQL: SELECT resources_id FROM t_ds_relation_resources_user WHERE user_id = #{userId} AND perm = #{perm}
     *
     * @param userId userId
     * @param perm   perm
     * @return resourcesId list
     */
    List<Integer> queryResourcesIdListByUserIdAndPerm(@Param("userId") int userId,
                                                      @Param("perm") int perm);

    /**
     * 删除指定用户对指定资源的授权关系。
     * SQL: DELETE FROM t_ds_relation_resources_user WHERE user_id = #{userId} AND resources_id = #{resourceId}
     *
     * @param userId     userId
     * @param resourceId resourceId
     * @return 删除的记录数
     */
    int deleteResourceUser(@Param("userId") int userId,
                           @Param("resourceId") int resourceId);

    /**
     * 批量删除指定用户对指定资源ID数组的授权关系。
     * SQL: DELETE FROM t_ds_relation_resources_user WHERE user_id = #{userId} AND resources_id IN #{resIds}
     *
     * @param userId userId
     * @param resIds resource Ids
     * @return 删除的记录数
     */
    int deleteResourceUserArray(@Param("userId") int userId,
                           @Param("resIds") Integer[] resIds);

}
