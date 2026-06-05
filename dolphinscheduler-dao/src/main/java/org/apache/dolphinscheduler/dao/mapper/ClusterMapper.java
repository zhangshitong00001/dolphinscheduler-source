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

import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 集群 Mapper 接口，封装对 t_ds_cluster 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供集群信息的查询、分页及删除等能力。
 */
public interface ClusterMapper extends BaseMapper<Cluster> {

    /**
     * 根据集群名称精确查询集群信息。
     * SELECT * FROM t_ds_cluster WHERE name = #{clusterName}
     *
     * @param name 集群名称
     * @return 集群实体，若不存在则返回 null
     */
    Cluster queryByClusterName(@Param("clusterName") String name);

    /**
     * 根据集群编码（唯一标识）查询集群信息。
     * SELECT * FROM t_ds_cluster WHERE code = #{clusterCode}
     *
     * @param clusterCode 集群编码
     * @return 集群实体，若不存在则返回 null
     */
    Cluster queryByClusterCode(@Param("clusterCode") Long clusterCode);

    /**
     * 查询所有集群列表。
     * SELECT * FROM t_ds_cluster
     *
     * @return 全部集群列表
     */
    List<Cluster> queryAllClusterList();

    /**
     * 分页查询集群列表，支持按名称搜索（LIKE 模糊匹配）。
     *
     * @param page 分页对象
     * @param searchName 搜索名称关键字，用于 LIKE 模糊匹配
     * @return 集群分页结果
     */
    IPage<Cluster> queryClusterListPaging(IPage<Cluster> page, @Param("searchName") String searchName);

    /**
     * 根据集群编码删除集群记录。
     * DELETE FROM t_ds_cluster WHERE code = #{code}
     *
     * @param code 集群编码
     * @return 删除的记录数
     */
    int deleteByCode(@Param("code") Long code);
}
