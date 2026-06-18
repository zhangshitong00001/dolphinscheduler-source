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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 升级用项目数据访问对象，负责在版本升级时查询项目数据并将旧版 ID 映射为新编码 Code。
 */
public class ProjectDao {

    public static final Logger logger = LoggerFactory.getLogger(ProjectDao.class);

    /**
     * 查询所有项目，返回旧版 ID 到新编码 Code 的映射。
     *
     * @param conn jdbc connection
     * @return Project List
     */
    public Map<Integer, Long> queryAllProject(Connection conn) {
        Map<Integer, Long> projectMap = new HashMap<>();
        String sql = "SELECT id,code FROM t_ds_project";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(1);
                long code = rs.getLong(2);
                if (code == 0L) {
                    code = CodeGenerateUtils.getInstance().genCode();
                }
                projectMap.put(id, code);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
        return projectMap;
    }

    /**
     * 批量更新项目的编码（Code），将旧版 ID 替换为新编码。
     *
     * @param conn jdbc connection
     * @param projectMap projectMap
     */
    public void updateProjectCode(Connection conn, Map<Integer, Long> projectMap) {
        String sql = "UPDATE t_ds_project SET code=? where id=?";
        try {
            for (Map.Entry<Integer, Long> entry : projectMap.entrySet()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, entry.getValue());
                    pstmt.setInt(2, entry.getKey());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(conn);
        }
    }
}
