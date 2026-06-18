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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证器接口。定义用户身份认证的统一规范，支持用户名密码验证和从HTTP请求中获取已认证用户。
 * 所有认证方式（PASSWORD、LDAP等）均需实现此接口。
 */
public interface Authenticator {
    /**
     * 通过用户名和密码验证用户身份的合法性。
     *
     * @param username 用户名
     * @param password 用户密码
     * @param extra    额外信息（如客户端IP等）
     * @return 认证结果，包含sessionId和安全配置类型
     */
    Result<Map<String, String>> authenticate(String username, String password, String extra);

    /**
     * 从HTTP请求中获取已认证的用户信息。
     *
     * @param request HTTP请求对象
     * @return 已认证的用户实体，未认证则返回null
     */
    User getAuthUser(HttpServletRequest request);
}
