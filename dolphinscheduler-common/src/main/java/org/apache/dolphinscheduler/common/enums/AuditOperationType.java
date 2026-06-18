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

import java.util.HashMap;

/**
 * 审计操作类型枚举。
 * 定义审计日志记录的操作类型（增/读/改/删）。
 */
public enum AuditOperationType {

    /** 创建操作 */
    CREATE(0, "CREATE"),
    /** 读取操作 */
    READ(1, "READ"),
    /** 更新操作 */
    UPDATE(2, "UPDATE"),
    /** 删除操作 */
    DELETE(3, "DELETE");

    /** 操作类型编码 */
    private final int code;
    /** 操作类型英文消息 */
    private final String enMsg;

    /** 编码到枚举实例的映射缓存 */
    private static HashMap<Integer, AuditOperationType> AUDIT_OPERATION_MAP = new HashMap<>();

    static {
        for (AuditOperationType operationType : AuditOperationType.values()) {
            AUDIT_OPERATION_MAP.put(operationType.code, operationType);
        }
    }

    AuditOperationType(int code, String enMsg) {
        this.code = code;
        this.enMsg = enMsg;
    }

    /**
     * 根据编码获取对应的审计操作类型。
     * @param status 操作类型编码
     * @return 对应的审计操作类型枚举
     * @throws IllegalArgumentException 如果编码无效
     */
    public static AuditOperationType of(int status) {
        if (AUDIT_OPERATION_MAP.containsKey(status)) {
            return AUDIT_OPERATION_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid audit operation type code " + status);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return enMsg;
    }
}
