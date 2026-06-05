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

import org.apache.dolphinscheduler.dao.entity.Session;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话 Mapper 接口，封装对 t_ds_session 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供用户会话的按用户和IP查询能力。
 */
public interface SessionMapper extends BaseMapper<Session> {

    /**
     * 根据用户ID查询该用户的所有会话列表。
     * SQL: SELECT * FROM t_ds_session WHERE user_id = #{userId}
     *
     * @param userId userId
     * @return session list
     */
    List<Session> queryByUserId(@Param("userId") int userId);

    /**
     * 根据用户ID和IP地址查询对应的会话记录。
     * SQL: SELECT * FROM t_ds_session WHERE user_id = #{userId} AND ip = #{ip} LIMIT 1
     *
     * @param userId userId
     * @param ip     ip
     * @return session 会话实体，未找到时返回 null
     */
    Session queryByUserIdAndIp(@Param("userId") int userId,@Param("ip") String ip);

}
