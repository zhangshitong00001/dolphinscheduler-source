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

package org.apache.dolphinscheduler.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 正则表达式工具类。提供Linux用户名校验以及日志安全转义功能。
 * 该类为工具类。
 */
public class RegexUtils {

    private static final String LINUX_USERNAME_PATTERN = "^[a-zA-Z0-9_].{0,30}";

    private RegexUtils() {
    }

    /**
     * 校验输入是否为合法的Linux用户名格式。
     *
     * @param str 输入字符串
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidLinuxUserName(String str) {
        Pattern pattern = Pattern.compile(LINUX_USERNAME_PATTERN);
        return pattern.matcher(str).matches();
    }

    public static String escapeNRT(String str) {
        // Logging should not be vulnerable to injection attacks: Replace pattern-breaking characters
        if (!StringUtils.isEmpty(str)) {
            return str.replaceAll("[\n|\r|\t]", "_");
        }
        return null;
    }

}
