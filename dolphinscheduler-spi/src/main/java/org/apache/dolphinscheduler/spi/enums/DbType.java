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

package org.apache.dolphinscheduler.spi.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.google.common.base.Functions;

/**
 * 数据库类型枚举，定义了DolphinScheduler支持的所有数据源类型。
 * <p>
 * 每种数据库类型对应唯一的整数编码，用于数据库层面的存储和传输。
 * 支持MySQL、PostgreSQL、Hive、Spark、ClickHouse、Oracle、SQL Server、
 * DB2、Presto、H2、Redshift和Athena。
 */
public enum DbType {
    /** MySQL数据库 */
    MYSQL(0, "mysql"),
    /** PostgreSQL数据库 */
    POSTGRESQL(1, "postgresql"),
    /** Hive数据仓库 */
    HIVE(2, "hive"),
    /** Spark计算引擎 */
    SPARK(3, "spark"),
    /** ClickHouse列式数据库 */
    CLICKHOUSE(4, "clickhouse"),
    /** Oracle数据库 */
    ORACLE(5, "oracle"),
    /** SQL Server数据库 */
    SQLSERVER(6, "sqlserver"),
    /** DB2数据库 */
    DB2(7, "db2"),
    /** Presto查询引擎 */
    PRESTO(8, "presto"),
    /** H2内存数据库 */
    H2(9, "h2"),
    /** AWS Redshift数据仓库 */
    REDSHIFT(10,"redshift"),
    /** AWS Athena查询服务 */
    ATHENA(11,"athena"),
    ;

    @EnumValue
    private final int code;
    private final String descp;

    DbType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    private static final Map<Integer, DbType> DB_TYPE_MAP =
            Arrays.stream(DbType.values()).collect(toMap(DbType::getCode, Functions.identity()));

    public static DbType of(int type) {
        if (DB_TYPE_MAP.containsKey(type)) {
            return DB_TYPE_MAP.get(type);
        }
        return null;
    }

    public static DbType ofName(String name) {
        return Arrays.stream(DbType.values()).filter(e -> e.name().equals(name)).findFirst().orElseThrow(() -> new NoSuchElementException("no such db type"));
    }

    public boolean isHive() {
        return this == DbType.HIVE;
    }
}
