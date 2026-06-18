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

import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 环境-工作组关联 Mapper 接口，封装对 t_ds_environment_worker_group_relation 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，管理环境配置与 Worker 工作组之间的多对多关联关系。
 */
public interface EnvironmentWorkerGroupRelationMapper extends BaseMapper<EnvironmentWorkerGroupRelation> {

    /**
     * 根据环境编码查询该环境绑定的所有工作组关联记录。
     * SELECT * FROM t_ds_environment_worker_group_relation WHERE environment_code = #{environmentCode}
     *
     * @param environmentCode 环境编码
     * @return 环境-工作组关联列表
     */
    List<EnvironmentWorkerGroupRelation> queryByEnvironmentCode(@Param("environmentCode") Long environmentCode);

    /**
     * 根据工作组名称查询该工作组关联的所有环境记录。
     * SELECT * FROM t_ds_environment_worker_group_relation WHERE worker_group = #{workerGroupName}
     *
     * @param workerGroupName Worker 工作组名称
     * @return 环境-工作组关联列表
     */
    List<EnvironmentWorkerGroupRelation> queryByWorkerGroupName(@Param("workerGroupName") String workerGroupName);

    /**
     * 根据环境编码和工作组名称删除关联记录。
     * DELETE FROM t_ds_environment_worker_group_relation WHERE environment_code = #{environmentCode} AND worker_group = #{workerGroupName}
     *
     * @param environmentCode 环境编码
     * @param workerGroupName Worker 工作组名称
     * @return 删除的记录数
     */
    int deleteByCode(@Param("environmentCode") Long environmentCode, @Param("workerGroupName") String workerGroupName);
}
