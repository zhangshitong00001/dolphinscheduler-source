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

import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 错误命令 Mapper 接口，封装对 t_ds_error_command 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，存储执行失败的命令记录，并提供按状态统计的能力。
 */
public interface ErrorCommandMapper extends BaseMapper<ErrorCommand> {

    /**
     * 按时间范围和项目编码统计错误命令各状态的数量。
     * SELECT command_type, COUNT(*) FROM t_ds_error_command WHERE start_time BETWEEN #{startTime} AND #{endTime}
     * AND project_code IN (#{projectCodeArray}) GROUP BY command_type
     *
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param projectCodeArray 项目编码数组
     * @return 命令统计结果列表
     */
    List<CommandCount> countCommandState(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectCodeArray") Long[] projectCodeArray);
}
