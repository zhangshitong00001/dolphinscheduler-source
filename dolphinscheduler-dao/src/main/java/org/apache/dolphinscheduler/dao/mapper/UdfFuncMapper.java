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

import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UDF 函数 Mapper 接口，封装对 t_ds_udf_func 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供 UDF 函数的查询、分页、批量更新及授权管理等能力。
 */
public interface UdfFuncMapper extends BaseMapper<UdfFunc> {

    /**
     * 根据主键ID查询 UDF 函数详情。
     * SELECT * FROM t_ds_udf_func WHERE id = #{id}
     *
     * @param id UDF 函数ID
     * @return UDF 函数实体，若不存在则返回 null
     */
    UdfFunc selectUdfById(@Param("id") int id);

    /**
     * 根据用户ID数组和函数名称查询 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id IN (#{ids}) AND func_name LIKE CONCAT('%', #{funcNames}, '%')
     *
     * @param ids 用户ID数组
     * @param funcNames 函数名称搜索关键字
     * @return UDF 函数列表
     */
    List<UdfFunc> queryUdfByIdStr(@Param("ids") Integer[] ids,
                                  @Param("funcNames") String funcNames);

    /**
     * 分页查询 UDF 函数列表，支持按用户ID列表和搜索关键字筛选。
     * SELECT * FROM t_ds_udf_func WHERE user_id IN (...) AND func_name LIKE CONCAT('%', #{searchVal}, '%')
     *
     * @param page 分页对象
     * @param ids 用户ID列表
     * @param searchVal 搜索关键字
     * @return UDF 函数分页结果
     */
    IPage<UdfFunc> queryUdfFuncPaging(IPage<UdfFunc> page,
                                      @Param("ids") List<Integer> ids,
                                      @Param("searchVal") String searchVal);

    /**
     * 根据用户ID列表和函数类型查询 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id IN (#{ids}) AND type = #{type}
     *
     * @param ids 用户ID列表
     * @param type UDF 函数类型
     * @return UDF 函数列表
     */
    List<UdfFunc> getUdfFuncByType(@Param("ids") List<Integer> ids,
                                   @Param("type") Integer type);

    /**
     * 查询指定用户以外的所有 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id != #{userId}
     *
     * @param userId 需要排除的用户ID
     * @return UDF 函数列表
     */
    List<UdfFunc> queryUdfFuncExceptUserId(@Param("userId") int userId);

    /**
     * 查询指定用户已授权的 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 已授权的 UDF 函数列表
     */
    List<UdfFunc> queryAuthedUdfFunc(@Param("userId") int userId);

    /**
     * 查询指定用户对给定 UDF 函数ID数组的授权 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id = #{userId} AND id IN (#{udfIds})
     *
     * @param userId 用户ID
     * @param udfIds UDF 函数ID数组
     * @param <T> UDF 函数ID数组的类型
     * @return 已授权的 UDF 函数列表
     */
    <T> List<UdfFunc> listAuthorizedUdfFunc (@Param("userId") int userId,@Param("udfIds")T[] udfIds);

    /**
     * 根据资源ID数组查询关联的 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE resource_id IN (#{resourceIds})
     *
     * @param resourceIds 资源ID数组
     * @return UDF 函数列表
     */
    List<UdfFunc> listUdfByResourceId(@Param("resourceIds") Integer[] resourceIds);

    /**
     * 查询指定用户对给定资源ID数组的授权 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id = #{userId} AND resource_id IN (#{resourceIds})
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID数组
     * @return 已授权的 UDF 函数列表
     */
    List<UdfFunc> listAuthorizedUdfByResourceId(@Param("userId") int userId,@Param("resourceIds") int[] resourceIds);

    /**
     * 批量更新 UDF 函数信息。
     * UPDATE t_ds_udf_func SET ... WHERE id = #{item.id}
     *
     * @param udfFuncList 待更新的 UDF 函数列表
     * @return 更新的行数
     */
    int batchUpdateUdfFunc(@Param("udfFuncList") List<UdfFunc> udfFuncList);

    /**
     * 根据用户ID查询该用户已授权的 UDF 函数列表。
     * SELECT * FROM t_ds_udf_func WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 已授权的 UDF 函数列表
     */
    List<UdfFunc> listAuthorizedUdfByUserId(@Param("userId") int userId);
}
