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

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 授权类型枚举。
 * 定义系统中各类资源的权限管理类型。
 */
public enum AuthorizationType {
    /** 资源文件ID */
    RESOURCE_FILE_ID(0, "resource file id"),
    /** 资源文件名 */
    RESOURCE_FILE_NAME(1, "resource file name"),
    /** UDF文件 */
    UDF_FILE(2, "udf file"),
    /** 数据源 */
    DATASOURCE(3, "data source"),
    /** UDF函数 */
    UDF(4, "udf function"),
    /** 项目 */
    PROJECTS(5, "projects"),
    /** 工作组 */
    WORKER_GROUP(6, "worker group"),
    /** 告警组 */
    ALERT_GROUP(7, "alert group"),
    /** 环境 */
    ENVIRONMENT(8, "environment"),
    /** 访问令牌 */
    ACCESS_TOKEN(9, "access token"),
    /** 队列 */
    QUEUE(10,"queue"),
    /** 数据分析 */
    DATA_ANALYSIS(11,"data analysis"),
    /** K8s命名空间 */
    K8S_NAMESPACE(12,"k8s namespace"),
    /** 监控 */
    MONITOR(13,"monitor"),
    /** 告警插件实例 */
    ALERT_PLUGIN_INSTANCE(14,"alert plugin instance"),
    /** 租户 */
    TENANT(15,"tenant"),
    /** 数据质量 */
    DATA_QUALITY(16,"data quality"),
    /** 任务组 */
    TASK_GROUP(17,"task group"),
    ;

    AuthorizationType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
