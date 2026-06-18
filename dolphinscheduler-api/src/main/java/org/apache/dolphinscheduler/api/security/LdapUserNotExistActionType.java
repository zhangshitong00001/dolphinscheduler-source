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

package org.apache.dolphinscheduler.api.security;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * LDAP用户不存在时的处理动作枚举。定义当LDAP认证成功但系统中不存在对应用户时的处理策略。
 * 支持自动创建用户（CREATE）和拒绝登录（DENY）两种策略。
 */
public enum LdapUserNotExistActionType {

    CREATE(0, "automatically create user when user not exist"),
    DENY(1, "deny log-in when user not exist"),
    ;

    LdapUserNotExistActionType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @EnumValue
    private final int code;
    private final String desc;
}
