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

import org.apache.dolphinscheduler.dao.entity.Resource;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 资源 Mapper 接口，封装对 t_ds_resources 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供文件资源的增删改查、用户权限过滤、分页查询及批量更新等能力。
 */
public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     * 根据资源全名、用户ID和资源类型查询资源列表。
     * SQL: SELECT * FROM t_ds_resources WHERE full_name = #{fullName} AND user_id = #{userId} AND type = #{type}
     *
     * @param fullName full name
     * @param userId   userId
     * @param type     type
     * @return resource list
     */
    List<Resource> queryResourceList(@Param("fullName") String fullName,
                                     @Param("userId") int userId,
                                     @Param("type") int type);

    /**
     * 查询指定用户有权限的资源列表。
     * SQL: SELECT * FROM t_ds_resources WHERE user_id = #{userId} AND type = #{type}
     *
     * @param userId userId
     * @param type   type
     * @return resource list
     */
    List<Resource> queryResourceListAuthored(@Param("userId") int userId,
                                             @Param("type") int type);

    /**
     * 分页查询资源列表，支持按资源ID、类型、名称模糊搜索和资源ID集合过滤。
     * SQL: SELECT * FROM t_ds_resources WHERE id = #{id} AND type = #{type}
     *      AND full_name LIKE #{searchVal} AND id IN #{resIds} ORDER BY id DESC
     *
     * @param page      page
     * @param id        id
     * @param type      type
     * @param searchVal searchVal
     * @param resIds    resIds
     * @return resource page 分页结果
     */
    IPage<Resource> queryResourcePaging(IPage<Resource> page,
                                        @Param("id") int id,
                                        @Param("type") int type,
                                        @Param("searchVal") String searchVal,
                                        @Param("resIds") List<Integer> resIds);

    /**
     * 查询除指定用户自有资源外的其他资源列表。
     * SQL: SELECT * FROM t_ds_resources WHERE user_id != #{userId}
     *
     * @param userId userId
     * @return resource list
     */
    List<Resource> queryResourceExceptUserId(@Param("userId") int userId);

    /**
     * 查询指定用户被授权的指定资源名称列表中的资源。
     * SQL: SELECT * FROM t_ds_resources r INNER JOIN t_ds_relation_resources_user ru
     *      ON r.id = ru.resources_id WHERE ru.user_id = #{userId} AND r.full_name IN #{resNames}
     *
     * @param userId   userId
     * @param resNames resNames
     * @param <T>      资源名称类型
     * @return resource list
     */
    <T> List<Resource> listAuthorizedResource(@Param("userId") int userId, @Param("resNames") T[] resNames);

    /**
     * 根据资源ID集合批量查询资源列表。
     * SQL: SELECT * FROM t_ds_resources WHERE id IN #{resIds}
     *
     * @param resIds resIds
     * @return resource list
     */
    List<Resource> queryResourceListById(@Param("resIds") List<Integer> resIds);

    /**
     * 查询指定用户被授权的指定资源ID集合中的资源。
     * SQL: SELECT * FROM t_ds_resources r INNER JOIN t_ds_relation_resources_user ru
     *      ON r.id = ru.resources_id WHERE ru.user_id = #{userId} AND r.id IN #{resIds}
     *
     * @param userId userId
     * @param resIds resIds
     * @param <T>    资源ID类型
     * @return resource list
     */
    <T> List<Resource> listAuthorizedResourceById(@Param("userId") int userId,@Param("resIds")T[] resIds);

    /**
     * 根据资源ID数组批量删除资源。
     * SQL: DELETE FROM t_ds_resources WHERE id IN #{resIds}
     *
     * @param resIds resource id array
     * @return 删除的记录数
     */
    int deleteIds(@Param("resIds")Integer[] resIds);

    /**
     * 查询指定目录下的子资源ID列表。
     * SQL: SELECT id FROM t_ds_resources WHERE pid = #{direcotyId}
     *
     * @param direcotyId directory id
     * @return resource id array
     */
    List<Integer> listChildren(@Param("direcotyId") int direcotyId);

    /**
     * 根据资源全名和类型查询资源列表。
     * SQL: SELECT * FROM t_ds_resources WHERE full_name = #{fullName} AND type = #{type}
     *
     * @param fullName full name
     * @param type     resource type
     * @return resource list
     */
    List<Resource> queryResource(@Param("fullName") String fullName,@Param("type") int type);

    /**
     * 根据资源ID数组批量查询资源列表。
     * SQL: SELECT * FROM t_ds_resources WHERE id IN #{resIds}
     *
     * @param resIds resource id array
     * @return resource list
     */
    List<Resource> listResourceByIds(@Param("resIds")Integer[] resIds);

    /**
     * 批量更新资源信息（如移动资源时更新目录结构）。
     * SQL: UPDATE t_ds_resources SET ... WHERE id = #{item.id}（批量执行）
     *
     * @param resourceList resource list
     * @return 更新的记录数
     */
    int batchUpdateResource(@Param("resourceList") List<Resource> resourceList);

    /**
     * 检查指定用户在指定路径下是否存在指定类型的资源。
     * SQL: SELECT COUNT(*) > 0 FROM t_ds_resources WHERE full_name = #{fullName} AND user_id = #{userId} AND type = #{type}
     *
     * @param fullName full name
     * @param userId   userId
     * @param type     type
     * @return true if exist, else false
     */
    Boolean existResourceByUser(@Param("fullName") String fullName,
                              @Param("userId") int userId,
                              @Param("type") int type);

    /**
     * 检查指定路径下是否存在指定类型的资源（不限制用户）。
     * SQL: SELECT COUNT(*) > 0 FROM t_ds_resources WHERE full_name = #{fullName} AND type = #{type}
     *
     * @param fullName full name
     * @param type     type
     * @return true if exist, else false
     */
    Boolean existResource(@Param("fullName") String fullName,
                          @Param("type") int type);

}
