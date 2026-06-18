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

import org.apache.dolphinscheduler.dao.entity.DatasourceUser;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 数据源-用户关联 Mapper 接口，封装对 t_ds_datasource_user 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，管理数据源与用户之间的授权关联关系。
 */
public interface DataSourceUserMapper extends BaseMapper<DatasourceUser> {


    /**
     * 根据用户ID删除该用户的所有数据源授权关联记录。
     * DELETE FROM t_ds_datasource_user WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") int userId);

    /**
     * 根据数据源ID删除该数据源的所有用户授权关联记录。
     * DELETE FROM t_ds_datasource_user WHERE datasource_id = #{datasourceId}
     *
     * @param datasourceId 数据源ID
     * @return 删除的记录数
     */
    int deleteByDatasourceId(@Param("datasourceId") int datasourceId);

}
