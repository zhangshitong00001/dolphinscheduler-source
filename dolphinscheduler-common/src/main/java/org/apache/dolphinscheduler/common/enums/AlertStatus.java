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
 * 告警发送（执行）状态枚举。
 * 用于标识告警通知的发送/执行结果。
 */
public enum AlertStatus {
    /** 等待执行（0） */
    WAIT_EXECUTION(0, "waiting executed"),
    /** 执行成功（1） */
    EXECUTION_SUCCESS(1, "execute successfully"),
    /** 执行失败（2） */
    EXECUTION_FAILURE(2, "execute failed"),
    /** 部分执行成功（3） */
    EXECUTION_PARTIAL_SUCCESS(3, "execute partial successfully");

    AlertStatus(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    /** MyBatis-Plus枚举值映射：状态码 */
    @EnumValue
    private final int code;
    /** 状态描述 */
    private final String descp;
}
