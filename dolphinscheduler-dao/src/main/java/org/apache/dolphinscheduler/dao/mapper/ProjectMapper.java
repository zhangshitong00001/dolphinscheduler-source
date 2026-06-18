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

import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 项目 Mapper 接口，封装对 t_ds_project 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供项目的增删改查、权限过滤、分页查询及依赖节点项目查询等能力。
 */
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 根据项目编码查询项目详情。
     * SQL: SELECT * FROM t_ds_project WHERE code = #{projectCode}
     *
     * @param projectCode projectCode
     * @return project 项目实体，未找到时返回 null
     */
    Project queryByCode(@Param("projectCode") long projectCode);

    /**
     * 根据项目编码集合批量查询项目列表。
     * SQL: SELECT * FROM t_ds_project WHERE code IN #{codes}
     *
     * @param codes codes
     * @return project list
     */
    List<Project> queryByCodes(@Param("codes") Collection<Long> codes);

    /**
     * TODO: 待删除 —— 根据项目ID查询项目详情（已废弃，请使用 queryByCode 替代）。
     * SQL: SELECT * FROM t_ds_project WHERE id = #{projectId}
     *
     * @param projectId projectId
     * @return project 项目实体
     * @deprecated 使用 queryDetailByCode 替代
     */
    Project queryDetailById(@Param("projectId") int projectId);

    /**
     * 根据项目编码查询项目详细信息。
     * SQL: SELECT * FROM t_ds_project WHERE code = #{projectCode}
     *
     * @param projectCode projectCode
     * @return project 项目实体
     */
    Project queryDetailByCode(@Param("projectCode") long projectCode);

    /**
     * 根据项目名称查询项目实体。
     * SQL: SELECT * FROM t_ds_project WHERE name = #{projectName}
     *
     * @param projectName projectName
     * @return project 项目实体，未找到时返回 null
     */
    Project queryByName(@Param("projectName") String projectName);

    /**
     * 分页查询项目列表，支持按项目ID集合和名称关键词过滤。
     * SQL: SELECT * FROM t_ds_project WHERE id IN #{projectsIds} AND name LIKE #{searchName} ORDER BY create_time DESC
     *
     * @param page        page
     * @param projectsIds projectsIds
     * @param searchName  searchName
     * @return project IPage 分页结果
     */
    IPage<Project> queryProjectListPaging(IPage<Project> page,
                                          @Param("projectsIds") List<Integer> projectsIds,
                                          @Param("searchName") String searchName);

    /**
     * 查询指定用户创建的项目列表。
     * SQL: SELECT * FROM t_ds_project WHERE user_id = #{userId}
     *
     * @param userId userId
     * @return project list
     */
    List<Project> queryProjectCreatedByUser(@Param("userId") int userId);

    /**
     * 查询指定用户被授权的项目列表。
     * SQL: SELECT p.* FROM t_ds_project p INNER JOIN t_ds_relation_project_user r
     *      ON p.id = r.project_id WHERE r.user_id = #{userId}
     *
     * @param userId userId
     * @return project list
     */
    List<Project> queryAuthedProjectListByUserId(@Param("userId") int userId);

    /**
     * 查询指定用户关联的所有项目列表（包括自有和共享项目）。
     * SQL: SELECT p.* FROM t_ds_project p LEFT JOIN t_ds_relation_project_user r
     *      ON p.id = r.project_id WHERE p.user_id = #{userId} OR r.user_id = #{userId}
     *
     * @param userId userId
     * @return project list
     */
    List<Project> queryRelationProjectListByUserId(@Param("userId") int userId);

    /**
     * 查询除指定用户自有项目外的其他项目列表。
     * SQL: SELECT * FROM t_ds_project WHERE user_id != #{userId}
     *
     * @param userId userId
     * @return project list
     */
    List<Project> queryProjectExceptUserId(@Param("userId") int userId);

    /**
     * 查询指定用户创建的和被授权的项目列表（合并结果）。
     * SQL: (SELECT * FROM t_ds_project WHERE user_id = #{userId})
     *      UNION (SELECT p.* FROM t_ds_project p INNER JOIN t_ds_relation_project_user r ON p.id = r.project_id WHERE r.user_id = #{userId})
     *
     * @param userId userId
     * @return project list
     */
    List<Project> queryProjectCreatedAndAuthorizedByUserId(@Param("userId") int userId);

    /**
     * 根据流程实例ID查询项目名和用户名，用于流程实例详情展示。
     * SQL: SELECT p.name AS projectName, u.user_name AS userName FROM t_ds_project p
     *      INNER JOIN t_ds_process_instance pi ON p.id = pi.project_id
     *      INNER JOIN t_ds_user u ON pi.user_id = u.id WHERE pi.id = #{processInstanceId}
     *
     * @param processInstanceId processInstanceId
     * @return ProjectUser（包含 projectName 和 userName）
     */
    ProjectUser queryProjectWithUserByProcessInstanceId(@Param("processInstanceId") int processInstanceId);

    /**
     * 查询指定用户有权限的所有项目列表（含创建和授权的项目）。
     * SQL: SELECT DISTINCT p.* FROM t_ds_project p
     *      LEFT JOIN t_ds_relation_project_user r ON p.id = r.project_id
     *      WHERE p.user_id = #{userId} OR r.user_id = #{userId}
     *
     * @param userId userId
     * @return projectList
     */
    List<Project> queryAllProject(@Param("userId") int userId);

    /**
     * 查询指定用户在指定项目ID集合范围内有权限的项目列表。
     * SQL: SELECT DISTINCT p.* FROM t_ds_project p
     *      LEFT JOIN t_ds_relation_project_user r ON p.id = r.project_id
     *      WHERE p.id IN #{projectsIds} AND (p.user_id = #{userId} OR r.user_id = #{userId})
     *
     * @param userId      userId
     * @param projectsIds projectsIds
     * @param <T>         项目ID类型
     * @return project list
     */
    List<Project> listAuthorizedProjects(@Param("userId") int userId, @Param("projectsIds") List<Integer> projectsIds);

    /**
     * 查询所有用于依赖节点的项目列表（不按用户过滤，用于跨项目依赖解析）。
     * SQL: SELECT * FROM t_ds_project
     *
     * @return projectList
     */
    List<Project> queryAllProjectForDependent();
}
