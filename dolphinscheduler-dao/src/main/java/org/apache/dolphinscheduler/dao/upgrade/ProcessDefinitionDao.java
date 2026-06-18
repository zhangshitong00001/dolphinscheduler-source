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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.ConnectionUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 升级用流程定义数据访问对象，用于在版本升级时读取旧版流程定义 JSON 数据并更新为新的编码结构。
 */
public class ProcessDefinitionDao {


    public static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDao.class);

    /**
     * 查询所有流程定义的原始 JSON 数据，返回 ID 到 JSON 字符串的映射。
     *
     * @param conn jdbc connection
     * @return ProcessDefinition Json List
     */
    public Map<Integer, String> queryAllProcessDefinition(Connection conn) {

        Map<Integer, String> processDefinitionJsonMap = new HashMap<>();

        String sql = "SELECT id,process_definition_json FROM t_ds_process_definition";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Integer id = rs.getInt(1);
                String processDefinitionJson = rs.getString(2);
                processDefinitionJsonMap.put(id, processDefinitionJson);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }

        return processDefinitionJsonMap;
    }

    /**
     * 批量更新流程定义的 JSON 数据，将处理后的 JSON 回写到数据库。
     *
     * @param conn jdbc connection
     * @param processDefinitionJsonMap processDefinitionJsonMap
     */
    public void updateProcessDefinitionJson(Connection conn, Map<Integer, String> processDefinitionJsonMap) {
        String sql = "UPDATE t_ds_process_definition SET process_definition_json=? where id=?";
        try {
            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, entry.getValue());
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

    /**
     * 查询流程定义实体列表，包含旧版 ID 到新编码 code 的转换逻辑。
     */
    public List<ProcessDefinition> queryProcessDefinition(Connection conn) {
        List<ProcessDefinition> processDefinitions = new ArrayList<>();
        String sql = "SELECT id,code,project_code,user_id,locations,name,description,release_state,flag,create_time FROM t_ds_process_definition";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProcessDefinition processDefinition = new ProcessDefinition();
                processDefinition.setId(rs.getInt(1));
                long code = rs.getLong(2);
                if (code == 0L) {
                    code = CodeGenerateUtils.getInstance().genCode();
                }
                processDefinition.setCode(code);
                processDefinition.setVersion(Constants.VERSION_FIRST);
                processDefinition.setProjectCode(rs.getLong(3));
                processDefinition.setUserId(rs.getInt(4));
                processDefinition.setLocations(rs.getString(5));
                processDefinition.setName(rs.getString(6));
                processDefinition.setDescription(rs.getString(7));
                processDefinition.setReleaseState(ReleaseState.getEnum(rs.getInt(8)));
                processDefinition.setFlag(rs.getInt(9) == 1 ? Flag.YES : Flag.NO);
                processDefinition.setCreateTime(rs.getDate(10));
                processDefinitions.add(processDefinition);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
        return processDefinitions;
    }

    /**
     * 更新流程定义的编码（code），将旧版 ID 替换为新编码，同时映射旧项目 ID 到新项目编码。
     *
     * @param conn jdbc connection
     * @param processDefinitions processDefinitions
     * @param projectIdCodeMap projectIdCodeMap
     */
    public void updateProcessDefinitionCode(Connection conn, List<ProcessDefinition> processDefinitions, Map<Integer, Long> projectIdCodeMap) {
        String sql = "UPDATE t_ds_process_definition SET code=?, project_code=?, version=? where id=?";
        try {
            for (ProcessDefinition processDefinition : processDefinitions) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, processDefinition.getCode());
                    long projectCode = processDefinition.getProjectCode();
                    if (String.valueOf(projectCode).length() <= 10) {
                        Integer projectId = Integer.parseInt(String.valueOf(projectCode));
                        if (projectIdCodeMap.containsKey(projectId)) {
                            projectCode = projectIdCodeMap.get(projectId);
                            processDefinition.setProjectCode(projectCode);
                        }
                    }
                    pstmt.setLong(2, projectCode);
                    pstmt.setInt(3, processDefinition.getVersion());
                    pstmt.setInt(4, processDefinition.getId());
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
