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

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PasswordUtils.class})
public class DorisDataSourceProcessorTest {

    private final DorisDataSourceProcessor processor = new DorisDataSourceProcessor();

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(DataSourceConstants.COM_DORIS_JDBC_DRIVER, processor.getDatasourceDriver());
    }

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(DataSourceConstants.DORIS_VALIDATION_QUERY, processor.getValidationQuery());
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.DORIS, processor.getDbType());
    }

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        DorisDataSourceParamDTO dto = new DorisDataSourceParamDTO();
        dto.setHost("127.0.0.1");
        dto.setPort(9030);
        dto.setDatabase("test_db");
        dto.setUserName("root");
        dto.setPassword("password");
        dto.setOther(props);

        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");

        DorisConnectionParam result = (DorisConnectionParam) processor.createConnectionParams(dto);

        Assert.assertNotNull(result);
        Assert.assertEquals("jdbc:mysql://127.0.0.1:9030", result.getAddress());
        Assert.assertEquals("jdbc:mysql://127.0.0.1:9030/test_db", result.getJdbcUrl());
        Assert.assertEquals("test_db", result.getDatabase());
        Assert.assertEquals("root", result.getUser());
        Assert.assertEquals(DataSourceConstants.COM_DORIS_JDBC_DRIVER, result.getDriverClassName());
        Assert.assertEquals(DataSourceConstants.DORIS_VALIDATION_QUERY, result.getValidationQuery());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:9030\""
                + ",\"database\":\"test_db\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:9030/test_db\"}";
        DorisConnectionParam connectionParams = (DorisConnectionParam) processor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetJdbcUrlWithoutOtherParams() {
        DorisConnectionParam connectionParam = new DorisConnectionParam();
        connectionParam.setJdbcUrl("jdbc:mysql://127.0.0.1:9030/test_db");

        String jdbcUrl = processor.getJdbcUrl(connectionParam);

        Assert.assertEquals(
                "jdbc:mysql://127.0.0.1:9030/test_db?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                jdbcUrl);
    }

    @Test
    public void testGetJdbcUrlWithOtherParams() {
        DorisConnectionParam connectionParam = new DorisConnectionParam();
        connectionParam.setJdbcUrl("jdbc:mysql://127.0.0.1:9030/test_db");
        connectionParam.setOther("useSSL=false");

        String jdbcUrl = processor.getJdbcUrl(connectionParam);

        Assert.assertTrue(jdbcUrl.startsWith("jdbc:mysql://127.0.0.1:9030/test_db?"));
        Assert.assertTrue(jdbcUrl.contains("useSSL=false"));
        Assert.assertTrue(jdbcUrl.contains("allowLoadLocalInfile=false"));
        Assert.assertTrue(jdbcUrl.contains("autoDeserialize=false"));
    }
}
