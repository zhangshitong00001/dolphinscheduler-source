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

package org.apache.dolphinscheduler.spi.datasource;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 数据库连接参数的抽象基类，定义了所有数据库类型通用的连接参数字段。
 * <p>
 * 包含用户名、密码、地址、数据库名、JDBC URL、驱动信息等核心连接属性。
 * 具体数据库类型（MySQL、PostgreSQL等）应继承此类并添加各自的特有参数。
 * JSON序列化时忽略null字段。
 */
@JsonInclude(Include.NON_NULL)
public abstract class BaseConnectionParam implements ConnectionParam {

    /** 数据库用户名 */
    protected String user;

    /** 数据库密码 */
    protected String password;

    /** 数据库地址（IP:端口） */
    protected String address;

    /** 数据库名称 */
    protected String database;

    /** JDBC连接URL */
    protected String jdbcUrl;

    /** JDBC驱动jar包路径 */
    protected String driverLocation;

    /** JDBC驱动类全限定名 */
    protected String driverClassName;

    /** 用于校验连接有效性的SQL查询语句 */
    protected String validationQuery;

    /** 其他额外JDBC连接参数 */
    protected String other;

    /** 额外的连接属性键值对 */
    private Map<String, String> props = new HashMap<>();

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(String driverLocation) {
        this.driverLocation = driverLocation;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }
}
