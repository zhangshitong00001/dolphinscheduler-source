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

package org.apache.dolphinscheduler.common.enums;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 配置文件类型枚举。
 * 定义系统支持的数据源配置环境类型，用于多环境配置切换。
 */
public enum ProfileType {
    ;

    /** H2数据库配置 */
    public static final String H2 = "h2";

    /** MySQL数据库配置 */
    public static final String MYSQL = "mysql";

    /** PostgreSQL数据库配置 */
    public static final String POSTGRESQL = "postgresql";

    public static final List<String> DATASOURCE_PROFILE = Lists.newArrayList(H2, MYSQL, POSTGRESQL);
}
