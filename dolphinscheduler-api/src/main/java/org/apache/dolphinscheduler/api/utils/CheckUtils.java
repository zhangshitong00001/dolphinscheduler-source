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

import static org.apache.dolphinscheduler.common.constants.Constants.USER_PASSWORD_MAX_LENGTH;
import static org.apache.dolphinscheduler.common.constants.Constants.USER_PASSWORD_MIN_LENGTH;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;

/**
 * 校验工具类。提供用户名、邮箱、密码、电话号码、时区等参数的格式校验功能。
 * 该类为工具类。
 */
public class CheckUtils {

    private CheckUtils() {
        throw new IllegalStateException("CheckUtils class");
    }

    /**
     * 校验用户名格式是否合法。
     *
     * @param userName 用户名
     * @return 格式合法返回true，否则返回false
     */
    public static boolean checkUserName(String userName) {
        return regexChecks(userName, Constants.REGEX_USER_NAME);
    }

    /**
     * 校验邮箱格式是否合法。要求至少包含二级域名。
     *
     * @param email 邮箱地址
     * @return 格式合法返回true，否则返回false
     */
    public static boolean checkEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        EmailValidator emailValidator = new EmailValidator();
        if (!emailValidator.isValid(email, null)) {
            return false;
        }
        //Email is at least a second-level domain name
        int indexDomain = email.lastIndexOf("@");
        String domainString = email.substring(indexDomain);
        return domainString.contains(".");
    }

    /**
     * 校验项目描述长度。描述长度不能超过200字符。
     *
     * @param desc 描述文本
     * @return 校验结果Map，包含状态码
     */
    public static Map<String, Object> checkDesc(String desc) {
        Map<String, Object> result = new HashMap<>();
        if (!StringUtils.isEmpty(desc) && desc.length() > 200) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG,
                    MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "desc length"));
        } else {
            result.put(Constants.STATUS, Status.SUCCESS);
        }
        return result;
    }

    /**
     * 校验其他参数是否为有效JSON格式。
     *
     * @param otherParams 其他参数JSON字符串
     * @return 参数无效返回true，有效返回false
     */
    public static boolean checkOtherParams(String otherParams) {
        return !StringUtils.isEmpty(otherParams) && !JSONUtils.checkJsonValid(otherParams);
    }

    /**
     * 校验密码是否为空且长度合法。
     *
     * @param password 密码
     * @return 密码合法返回true，否则返回false
     */
    public static boolean checkPassword(String password) {
        return !StringUtils.isEmpty(password) && checkPasswordLength(password);
    }

    /**
     * 校验密码长度是否在允许的范围内。
     *
     * @param password 密码
     * @return 长度合法返回true，否则返回false
     */
    public static boolean checkPasswordLength(String password) {
        return password.length() >= USER_PASSWORD_MIN_LENGTH && password.length() <= USER_PASSWORD_MAX_LENGTH;
    }

    /**
     * 校验电话号码格式。电话可以为空，非空时长度必须为11位。
     *
     * @param phone 电话号码
     * @return 格式合法返回true，否则返回false
     */
    public static boolean checkPhone(String phone) {
        return StringUtils.isEmpty(phone) || phone.length() == 11;
    }

    /**
     * 校验时区参数是否为有效的ZoneId。
     *
     * @param timeZone 时区字符串
     * @return 时区合法返回true，否则返回false
     */
    public static boolean checkTimeZone(String timeZone) {
        try {
            ZoneId.of(timeZone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * regex check
     *
     * @param str     input string
     * @param pattern regex pattern
     * @return true if regex pattern is right, otherwise return false
     */
    private static boolean regexChecks(String str, Pattern pattern) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        return pattern.matcher(str).matches();
    }
}
