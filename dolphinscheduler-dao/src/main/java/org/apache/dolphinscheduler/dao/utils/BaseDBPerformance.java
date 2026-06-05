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

package org.apache.dolphinscheduler.dao.utils;

import org.apache.dolphinscheduler.dao.entity.MonitorRecord;

import java.sql.Connection;

/**
 * 数据库性能采集抽象类，定义获取当前数据库性能指标的模板方法。
 * 各数据库类型（MySQL、PostgreSQL、H2）需实现各自的性能采集逻辑。
 */
public abstract class BaseDBPerformance {

    /**
     * 获取当前数据库的性能指标记录。
     *
     * @param conn connection
     * @return MonitorRecord
     */
    protected abstract MonitorRecord getMonitorRecord(Connection conn);

}
