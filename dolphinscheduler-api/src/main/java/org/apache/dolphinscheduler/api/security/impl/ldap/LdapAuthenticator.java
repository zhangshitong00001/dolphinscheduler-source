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

package org.apache.dolphinscheduler.api.security.impl.ldap;

import org.apache.dolphinscheduler.api.security.impl.AbstractAuthenticator;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * LDAP认证器。通过LDAP服务器验证用户身份，认证成功后自动在系统中创建用户（如果配置允许）。
 *
 * 认证流程：
 * 1. 调用LdapService.ldapLogin()在LDAP服务器上验证用户名密码
 * 2. 验证成功后检查系统中是否存在该用户
 * 3. 如果用户不存在且配置允许自动创建，则在系统中创建新用户
 * 4. 返回系统中的用户对象
 */
public class LdapAuthenticator extends AbstractAuthenticator {

    @Autowired
    LdapService ldapService;

    /**
     * 通过LDAP服务器进行用户登录验证。
     *
     * @param userId   用户身份标识
     * @param password 用户登录密码
     * @param extra    额外登录信息
     * @return 验证成功返回系统中的用户对象，失败返回null
     */
    @Override
    public User login(String userId, String password, String extra) {
        User user = null;
        String ldapEmail = ldapService.ldapLogin(userId, password);
        if (ldapEmail != null) {
            //check if user exist
            user = userService.getUserByUserName(userId);
            if (user == null && ldapService.createIfUserNotExists()) {
                user = userService.createUser(ldapService.getUserType(userId), userId, ldapEmail);
            }
        }
        return user;
    }
}
