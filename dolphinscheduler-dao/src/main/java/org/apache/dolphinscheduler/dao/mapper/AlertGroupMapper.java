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

import org.apache.dolphinscheduler.dao.entity.AlertGroup;

import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 告警组 Mapper 接口，封装对 t_ds_alertgroup 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供告警组的分页查询、按名称或用户查询、授权管理等功能。
 */
public interface AlertGroupMapper extends BaseMapper<AlertGroup> {


    /**
     * 分页查询告警组列表，支持按组名称进行 LIKE 模糊搜索。
     *
     * @param page 分页对象
     * @param groupName 告警组名称，用于模糊匹配
     * @return 告警组分页结果
     */
    IPage<AlertGroup> queryAlertGroupPage(Page page,
                                          @Param("groupName") String groupName);


    /**
     * 根据告警组名称精确查询告警组列表。
     * SELECT * FROM t_ds_alertgroup WHERE group_name = #{groupName}
     *
     * @param groupName 告警组名称
     * @return 告警组列表
     */
    List<AlertGroup> queryByGroupName(@Param("groupName") String groupName);

    /**
     * 判断指定名称的告警组是否已存在。
     * SELECT COUNT(*) > 0 FROM t_ds_alertgroup WHERE group_name = #{groupName}
     *
     * @param groupName 告警组名称
     * @return 存在返回 true，否则返回 false
     */
    Boolean existGroupName(@Param("groupName") String groupName);

    /**
     * 根据用户ID查询该用户关联的告警组列表。
     * 通过 t_ds_relation_user_alertgroup 关联表进行 JOIN 查询。
     *
     * @param userId 用户ID
     * @return 该用户关联的告警组列表
     */
    List<AlertGroup> queryByUserId(@Param("userId") int userId);

    /**
     * 查询所有告警组列表。
     * SELECT * FROM t_ds_alertgroup
     *
     * @return 全部告警组列表
     */
    List<AlertGroup> queryAllGroupList();

    /**
     * 查询所有告警组中包含的告警实例ID列表（逗号分隔的字符串）。
     * SELECT group_concat(alert_instance_ids) FROM t_ds_alertgroup
     *
     * @return 告警实例ID字符串列表
     */
    List<String> queryInstanceIdsList();

    /**
     * 根据告警组ID查询该组关联的告警插件实例ID串。
     * SELECT alert_instance_ids FROM t_ds_alertgroup WHERE id = #{alertGroupId}
     *
     * @param alertGroupId 告警组ID
     * @return 逗号分隔的告警实例ID字符串
     */
    String queryAlertGroupInstanceIdsById(@Param("alertGroupId") int alertGroupId);

    /**
     * 查询用户在指定告警组ID列表中有权访问的告警组。
     * SELECT * FROM t_ds_alertgroup WHERE id IN (#{alertGroupsIds})
     * 并根据用户绑定的告警组进行权限过滤。
     *
     * @param userId 用户ID
     * @param alertGroupsIds 告警组ID列表
     * @param <T> ID类型参数
     * @return 用户有权访问的告警组列表
     */
    <T> List<AlertGroup> listAuthorizedAlertGroupList (@Param("userId") int userId, @Param("alertGroupsIds")List<Integer> alertGroupsIds);

    /**
     * 根据指定的告警组ID列表分页查询告警组，支持按搜索值进行过滤。
     * SELECT * FROM t_ds_alertgroup WHERE id IN (#{ids}) AND group_name LIKE CONCAT('%', #{searchVal}, '%')
     *
     * @param page 分页对象
     * @param ids 告警组ID列表
     * @param searchVal 搜索关键字
     * @return 告警组分页结果
     */
    IPage<AlertGroup> queryAlertGroupPageByIds(Page<AlertGroup> page, @Param("ids") List<Integer> ids, @Param("searchVal") String searchVal);
}
