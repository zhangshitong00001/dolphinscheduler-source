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

import java.sql.Connection;

/**
 * 数据源客户端接口，封装了与数据库的实际连接管理。
 * <p>
 * 实现类负责创建和管理底层JDBC连接，提供连接健康检查和资源释放能力。
 * 继承自 {@link AutoCloseable}，支持try-with-resources模式。
 */
public interface DataSourceClient extends AutoCloseable {

    /**
     * 检查客户端连接是否有效（通常通过执行一条验证查询实现）
     */
    void checkClient();

    @Override
    void close();

    /**
     * 获取底层JDBC数据库连接
     *
     * @return JDBC连接对象
     */
    Connection getConnection();
}
