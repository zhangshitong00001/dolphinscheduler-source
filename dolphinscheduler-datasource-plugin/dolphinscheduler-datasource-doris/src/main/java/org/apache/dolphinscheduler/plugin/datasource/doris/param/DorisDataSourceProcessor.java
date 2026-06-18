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

package org.apache.dolphinscheduler.plugin.datasource.doris.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Apache Doris数据源处理器。
 * 通过AutoService注解自动注册为SPI服务。
 * 处理Doris数据源的参数校验、JDBC URL构建和连接管理。
 * Doris兼容MySQL协议，使用MySQL JDBC驱动通过FE端口9030连接。
 */
@AutoService(DataSourceProcessor.class)
public class DorisDataSourceProcessor extends AbstractDataSourceProcessor {

    private final Logger logger = LoggerFactory.getLogger(DorisDataSourceProcessor.class);

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";
    private static final String AUTO_DESERIALIZE = "autoDeserialize";
    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";
    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";
    private static final String APPEND_PARAMS = "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DorisDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DorisConnectionParam connectionParams = (DorisConnectionParam) createConnectionParams(connectionJson);
        DorisDataSourceParamDTO dorisDatasourceParamDTO = new DorisDataSourceParamDTO();
        dorisDatasourceParamDTO.setUserName(connectionParams.getUser());
        dorisDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        dorisDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));
        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        dorisDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        dorisDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        return dorisDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        DorisDataSourceParamDTO dorisDatasourceParam = (DorisDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_DORIS, dorisDatasourceParam.getHost(),
                dorisDatasourceParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, dorisDatasourceParam.getDatabase());
        DorisConnectionParam dorisConnectionParam = new DorisConnectionParam();
        dorisConnectionParam.setJdbcUrl(jdbcUrl);
        dorisConnectionParam.setDatabase(dorisDatasourceParam.getDatabase());
        dorisConnectionParam.setAddress(address);
        dorisConnectionParam.setUser(dorisDatasourceParam.getUserName());
        dorisConnectionParam.setPassword(PasswordUtils.encodePassword(dorisDatasourceParam.getPassword()));
        dorisConnectionParam.setDriverClassName(getDatasourceDriver());
        dorisConnectionParam.setValidationQuery(getValidationQuery());
        dorisConnectionParam.setOther(transformOther(dorisDatasourceParam.getOther()));
        dorisConnectionParam.setProps(dorisDatasourceParam.getOther());
        return dorisConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DorisConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DORIS_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DORIS_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DorisConnectionParam dorisConnectionParam = (DorisConnectionParam) connectionParam;
        String jdbcUrl = dorisConnectionParam.getJdbcUrl();
        if (!StringUtils.isEmpty(dorisConnectionParam.getOther())) {
            return String.format("%s?%s&%s", jdbcUrl, dorisConnectionParam.getOther(), APPEND_PARAMS);
        }
        return String.format("%s?%s", jdbcUrl, APPEND_PARAMS);
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DorisConnectionParam dorisConnectionParam = (DorisConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = dorisConnectionParam.getUser();
        if (user.contains(AUTO_DESERIALIZE)) {
            logger.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
            user = user.replace(AUTO_DESERIALIZE, "");
        }
        String password = PasswordUtils.decodePassword(dorisConnectionParam.getPassword());
        if (password.contains(AUTO_DESERIALIZE)) {
            logger.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
            password = password.replace(AUTO_DESERIALIZE, "");
        }
        return DriverManager.getConnection(getJdbcUrl(connectionParam), user, password);
    }

    @Override
    public DbType getDbType() {
        return DbType.DORIS;
    }

    @Override
    public DataSourceProcessor create() {
        return new DorisDataSourceProcessor();
    }

    private String transformOther(Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            return null;
        }
        Map<String, String> otherMap = new HashMap<>();
        paramMap.forEach((k, v) -> {
            if (!checkKeyIsLegitimate(k)) {
                return;
            }
            otherMap.put(k, v);
        });
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s&", key, value)));
        return stringBuilder.toString();
    }

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME)
                && !key.contains(AUTO_DESERIALIZE)
                && !key.contains(ALLOW_LOCAL_IN_FILE_NAME)
                && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split("&")) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }
}
