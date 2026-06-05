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

import org.apache.dolphinscheduler.dao.entity.ProjectUser;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 项目-用户关系 Mapper 接口，封装对 t_ds_relation_project_user 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供项目与用户关联关系的查询和删除能力。
 */
public interface ProjectUserMapper extends BaseMapper<ProjectUser> {

    /**
     * 删除指定项目与用户的关联关系。
     * SQL: DELETE FROM t_ds_relation_project_user WHERE project_id = #{projectId} AND user_id = #{userId}
     *
     * @param projectId projectId
     * @param userId    userId
     * @return 删除的记录数
     */
    int deleteProjectRelation(@Param("projectId") int projectId,
                              @Param("userId") int userId);

    /**
     * 查询指定项目与用户的关联关系。
     * SQL: SELECT * FROM t_ds_relation_project_user WHERE project_id = #{projectId} AND user_id = #{userId}
     *
     * @param projectId projectId
     * @param userId    userId
     * @return project user relation 项目用户关系实体，未找到时返回 null
     */
    ProjectUser queryProjectRelation(@Param("projectId") int projectId,
                                     @Param("userId") int userId);
}
