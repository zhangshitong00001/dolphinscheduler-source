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

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CommandCount;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 命令 Mapper 接口，封装对 t_ds_command 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供命令记录的分页查询、按状态统计以及基于槽位的分布式分页查询能力。
 */
public interface CommandMapper extends BaseMapper<Command> {

    /**
     * 按时间范围和项目编码统计各状态命令的数量。
     * SELECT command_type, COUNT(*) FROM t_ds_command WHERE start_time BETWEEN #{startTime} AND #{endTime}
     * AND project_code IN (#{projectCodeArray}) GROUP BY command_type
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param projectCodeArray 项目编码数组，用于过滤指定项目
     * @return 命令统计结果列表
     */
    List<CommandCount> countCommandState(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectCodeArray") Long[] projectCodeArray);

    /**
     * 分页查询命令列表。
     * SELECT * FROM t_ds_command LIMIT #{limit} OFFSET #{offset}
     *
     * @param limit 每页记录数
     * @param offset 偏移量
     * @return 命令列表
     */
    List<Command> queryCommandPage(@Param("limit") int limit, @Param("offset") int offset);


    /**
     * 基于槽位（slot）的分布式分页查询命令列表。
     * 用于多 Master 场景下按槽位分片读取命令，避免竞争。
     * 通过 id % #{masterCount} = #{thisMasterSlot} 进行槽位过滤。
     *
     * @param limit 每页记录数
     * @param offset 偏移量
     * @param masterCount Master 节点总数
     * @param thisMasterSlot 当前 Master 的槽位编号
     * @return 当前槽位对应的命令列表
     */
    List<Command> queryCommandPageBySlot(@Param("limit") int limit, @Param("offset") int offset, @Param("masterCount") int masterCount, @Param("thisMasterSlot") int thisMasterSlot);
}
