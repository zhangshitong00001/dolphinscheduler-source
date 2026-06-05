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

package org.apache.dolphinscheduler.api.security.impl.pwd;

import org.apache.dolphinscheduler.api.security.impl.AbstractAuthenticator;
import org.apache.dolphinscheduler.dao.entity.User;

/**
 * 密码认证器。通过数据库中的用户名和密码直接验证用户身份，是最基础的认证方式。
 *
 * 认证流程：
 * 1. 调用UserService根据用户名和密码查询数据库
 * 2. 返回查询到的用户对象，如果未找到则返回null
 */
public class PasswordAuthenticator extends AbstractAuthenticator {

    /**
     * 通过用户名和密码在数据库中查询用户。
     *
     * @param userId   用户身份标识（用户名）
     * @param password 用户登录密码
     * @param extra    额外登录信息
     * @return 数据库中匹配的用户对象，未找到返回null
     */
    @Override
    public User login(String userId, String password, String extra) {
        return userService.queryUser(userId, password);
    }
}
