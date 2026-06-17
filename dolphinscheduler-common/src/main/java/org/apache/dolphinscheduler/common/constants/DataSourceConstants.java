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

package org.apache.dolphinscheduler.common.constants;

/**
 * 数据源常量类。
 * 定义各种数据库的JDBC驱动类名、连接验证SQL及JDBC URL前缀，
 * 以及数据源加密和连接池配置键名。
 */
public class DataSourceConstants {

    /** 数据源通用标识 */
    public static final String DATASOURCE = "datasource";

    // ==================== 数据库JDBC驱动类名 ====================

    /** PostgreSQL驱动 */
    public static final String ORG_POSTGRESQL_DRIVER = "org.postgresql.Driver";
    /** MySQL CJ驱动（新版） */
    public static final String COM_MYSQL_CJ_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    /** MySQL JDBC驱动（旧版） */
    public static final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    /** Hive JDBC驱动 */
    public static final String ORG_APACHE_HIVE_JDBC_HIVE_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    /** ClickHouse JDBC驱动 */
    public static final String COM_CLICKHOUSE_JDBC_DRIVER = "com.clickhouse.jdbc.ClickHouseDriver";
    /** Oracle JDBC驱动 */
    public static final String COM_ORACLE_JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    /** SQL Server JDBC驱动 */
    public static final String COM_SQLSERVER_JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    /** DB2 JDBC驱动 */
    public static final String COM_DB2_JDBC_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    /** Presto JDBC驱动 */
    public static final String COM_PRESTO_JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
    /** Redshift JDBC驱动 */
    public static final String COM_REDSHIFT_JDBC_DRIVER = "com.amazon.redshift.jdbc42.Driver";
    /** Athena JDBC驱动 */
    public static final String COM_ATHENA_JDBC_DRIVER = "com.simba.athena.jdbc.Driver";
    /** Apache Doris JDBC驱动 (兼容MySQL协议) */
    public static final String COM_DORIS_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // ==================== 数据库连接验证SQL ====================

    /** PostgreSQL连接验证SQL */
    public static final String POSTGRESQL_VALIDATION_QUERY = "select version()";
    /** MySQL连接验证SQL */
    public static final String MYSQL_VALIDATION_QUERY = "select 1";
    /** Hive连接验证SQL */
    public static final String HIVE_VALIDATION_QUERY = "select 1";
    /** ClickHouse连接验证SQL */
    public static final String CLICKHOUSE_VALIDATION_QUERY = "select 1";
    /** Oracle连接验证SQL（必须包含dual表） */
    public static final String ORACLE_VALIDATION_QUERY = "select 1 from dual";
    /** SQL Server连接验证SQL */
    public static final String SQLSERVER_VALIDATION_QUERY = "select 1";
    /** DB2连接验证SQL（必须包含sysdummy1表） */
    public static final String DB2_VALIDATION_QUERY = "select 1 from sysibm.sysdummy1";
    /** Presto连接验证SQL */
    public static final String PRESTO_VALIDATION_QUERY = "select 1";
    /** Redshift连接验证SQL */
    public static final String REDHIFT_VALIDATION_QUERY = "select 1";
    /** Athena连接验证SQL */
    public static final String ATHENA_VALIDATION_QUERY = "select 1";
    /** Apache Doris连接验证SQL */
    public static final String DORIS_VALIDATION_QUERY = "select 1";

    // ==================== JDBC URL前缀 ====================

    /** MySQL JDBC URL前缀 */
    public static final String JDBC_MYSQL = "jdbc:mysql://";
    /** PostgreSQL JDBC URL前缀 */
    public static final String JDBC_POSTGRESQL = "jdbc:postgresql://";
    /** Hive JDBC URL前缀 */
    public static final String JDBC_HIVE_2 = "jdbc:hive2://";
    /** ClickHouse JDBC URL前缀 */
    public static final String JDBC_CLICKHOUSE = "jdbc:clickhouse://";
    /** Oracle SID方式JDBC URL前缀 */
    public static final String JDBC_ORACLE_SID = "jdbc:oracle:thin:@";
    /** Oracle Service Name方式JDBC URL前缀 */
    public static final String JDBC_ORACLE_SERVICE_NAME = "jdbc:oracle:thin:@//";
    /** SQL Server JDBC URL前缀 */
    public static final String JDBC_SQLSERVER = "jdbc:sqlserver://";
    /** DB2 JDBC URL前缀 */
    public static final String JDBC_DB2 = "jdbc:db2://";
    /** Presto JDBC URL前缀 */
    public static final String JDBC_PRESTO = "jdbc:presto://";
    /** Redshift JDBC URL前缀 */
    public static final String JDBC_REDSHIFT = "jdbc:redshift://";
    /** Athena JDBC URL前缀 */
    public static final String JDBC_ATHENA = "jdbc:awsathena://";
    /** Apache Doris JDBC URL前缀 (使用MySQL协议) */
    public static final String JDBC_DORIS = "jdbc:mysql://";

    // ==================== 数据库类型标识 ====================

    /** MySQL数据库类型 */
    public static final String MYSQL = "MYSQL";
    /** Hive数据库类型 */
    public static final String HIVE = "HIVE";

    // ==================== 数据源敏感参数 ====================

    /** 数据源密码正则表达式，用于脱敏匹配JSON中的password字段 */
    public static final String DATASOURCE_PASSWORD_REGEX =
            "(?<=((?i)password((\":\")|(=')))).*?(?=((\")|(')))";

    /** 数据源加密默认盐值 */
    public static final String DATASOURCE_ENCRYPTION_SALT_DEFAULT = "!@#$%^&*";
    /** 是否启用数据源加密的配置键 */
    public static final String DATASOURCE_ENCRYPTION_ENABLE = "datasource.encryption.enable";
    /** 数据源加密盐值的配置键 */
    public static final String DATASOURCE_ENCRYPTION_SALT = "datasource.encryption.salt";

    // ==================== 数据源连接池配置 ====================

    /** 连接池最小空闲连接数配置键 */
    public static final String SPRING_DATASOURCE_MIN_IDLE = "spring.datasource.minIdle";
    /** 连接池最大活跃连接数配置键 */
    public static final String SPRING_DATASOURCE_MAX_ACTIVE = "spring.datasource.maxActive";
    /** 连接借出时是否验证配置键 */
    public static final String SPRING_DATASOURCE_TEST_ON_BORROW = "spring.datasource.testOnBorrow";
}
