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

import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * 数据源通道接口，定义了根据连接参数和数据库类型创建数据源客户端的方法。
 * <p>
 * 每种数据库类型（如MySQL、PostgreSQL）都有对应的通道实现，
 * 负责创建该类型数据库的专用连接客户端，用于获取JDBC连接。
 */
public interface DataSourceChannel {

    /**
     * 根据给定的连接参数和数据库类型创建对应的数据源客户端
     *
     * @param baseConnectionParam 数据库连接参数（地址、用户名、密码、JDBC URL等）
     * @param dbType              数据库类型
     * @return 该数据库类型对应的数据源客户端实例
     */
    DataSourceClient createDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType);
}
