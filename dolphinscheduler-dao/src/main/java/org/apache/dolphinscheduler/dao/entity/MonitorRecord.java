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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.Date;

/**
 * 数据库监控记录，用于记录数据库连接池的运行状态指标，包括连接数、线程数等关键性能数据。
 */
public class MonitorRecord {

    /** 数据库类型 */
    private DbType dbType;

    /** 数据库状态是否正常，1 表示正常 */
    private Flag state;

    /** 最大连接数 */
    private long maxConnections;

    /** 最大已使用连接数 */
    private long maxUsedConnections;

    /** 线程连接数 */
    private long threadsConnections;

    /** 正在运行的线程连接数 */
    private long threadsRunningConnections;

    /** 采集日期 */
    private Date date;

    public Flag getState() {
        return state;
    }

    public void setState(Flag state) {
        this.state = state;
    }

    public long getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(long maxConnections) {
        this.maxConnections = maxConnections;
    }

    public long getMaxUsedConnections() {
        return maxUsedConnections;
    }

    public void setMaxUsedConnections(long maxUsedConnections) {
        this.maxUsedConnections = maxUsedConnections;
    }

    public long getThreadsConnections() {
        return threadsConnections;
    }

    public void setThreadsConnections(long threadsConnections) {
        this.threadsConnections = threadsConnections;
    }

    public long getThreadsRunningConnections() {
        return threadsRunningConnections;
    }

    public void setThreadsRunningConnections(long threadsRunningConnections) {
        this.threadsRunningConnections = threadsRunningConnections;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "MonitorRecord{"
                + "state=" + state
                + ", dbType=" + dbType
                + ", maxConnections=" + maxConnections
                + ", maxUsedConnections=" + maxUsedConnections
                + ", threadsConnections=" + threadsConnections
                + ", threadsRunningConnections=" + threadsRunningConnections
                + ", date=" + date
                + '}';
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }
}
