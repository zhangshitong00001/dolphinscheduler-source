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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用工具类，提供DolphinScheduler服务层常用的工具方法。包括系统环境路径获取、开发模式判断、
 * Kerberos认证配置加载、密码加密解密、资源存储类型判断等功能。
 */
public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    private static final Base64 BASE64 = new Base64();

    protected CommonUtils() {
        throw new UnsupportedOperationException("Construct CommonUtils");
    }

    /**
     * 获取系统环境变量配置文件的路径。
     *
     * @return the path of the system environment variables file
     */
    public static String getSystemEnvPath() {
        String envPath = PropertyUtils.getString(Constants.DOLPHINSCHEDULER_ENV_PATH);
        if (StringUtils.isEmpty(envPath)) {
            URL envDefaultPath = CommonUtils.class.getClassLoader().getResource(Constants.ENV_PATH);

            if (envDefaultPath != null) {
                envPath = envDefaultPath.getPath();
                logger.debug("env path :{}", envPath);
            } else {
                envPath = "/etc/profile";
            }
        }

        return envPath;
    }

    /**
     * 判断当前是否为开发模式。
     *
     * @return true if running in development mode
     */
    public static boolean isDevelopMode() {
        return PropertyUtils.getBoolean(Constants.DEVELOPMENT_STATE, true);
    }

    public static boolean isSetTaskDirToTenantEnable() {
        return PropertyUtils.getBoolean(Constants.SET_TASK_DIR_TO_TENANT_ENABLE, false);
    }

    /**
     * 判断是否启用了Kerberos认证。当资源存储类型为HDFS且Kerberos启动状态为true时返回true。
     *
     * @return true if Kerberos is enabled
     */
    public static boolean getKerberosStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState =
                PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        return resUploadType == ResUploadType.HDFS && kerberosStartupState;
    }

    /**
     * 加载Kerberos配置，从系统属性读取相关参数并进行认证登录。
     *
     * @param configuration the Hadoop Configuration to configure
     * @return true if Kerberos config was loaded successfully
     * @throws IOException if I/O errors occur during Kerberos authentication
     */
    public static boolean loadKerberosConf(Configuration configuration) throws IOException {
        return loadKerberosConf(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH),
                PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH), configuration);
    }

    /**
     * 使用指定的Kerberos参数加载配置并认证。
     *
     * @param javaSecurityKrb5Conf the krb5.conf file path
     * @param loginUserKeytabUsername the Kerberos principal username
     * @param loginUserKeytabPath the keytab file path
     * @throws IOException if I/O errors occur
     */
    public static void loadKerberosConf(String javaSecurityKrb5Conf, String loginUserKeytabUsername,
                                        String loginUserKeytabPath) throws IOException {
        loadKerberosConf(javaSecurityKrb5Conf, loginUserKeytabUsername, loginUserKeytabPath, new Configuration());
    }

    /**
     * 使用指定参数和配置对象加载Kerberos认证。
     *
     * @param javaSecurityKrb5Conf the krb5.conf file path
     * @param loginUserKeytabUsername the Kerberos principal username
     * @param loginUserKeytabPath the keytab file path
     * @param configuration the Hadoop Configuration to configure
     * @return true if Kerberos config was loaded successfully
     * @throws IOException if I/O errors occur
     */
    public static boolean loadKerberosConf(String javaSecurityKrb5Conf, String loginUserKeytabUsername,
                                           String loginUserKeytabPath, Configuration configuration) throws IOException {
        if (CommonUtils.getKerberosStartupState()) {
            System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF, StringUtils.defaultIfBlank(javaSecurityKrb5Conf,
                    PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH)));
            configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);
            UserGroupInformation.setConfiguration(configuration);
            UserGroupInformation.loginUserFromKeytab(
                    StringUtils.defaultIfBlank(loginUserKeytabUsername,
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME)),
                    StringUtils.defaultIfBlank(loginUserKeytabPath,
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH)));
            return true;
        }
        return false;
    }

    /**
     * 使用Base64加盐编码加密密码。如果加密功能未启用则直接返回原密码。
     *
     * @param password the plain text password
     * @return the encoded password
     */
    public static String encodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return StringUtils.EMPTY;
        }
        // if encryption is not turned on, return directly
        boolean encryptionEnable = PropertyUtils.getBoolean(DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE, false);
        if (!encryptionEnable) {
            return password;
        }

        // Using Base64 + salt to process password
        String salt = PropertyUtils.getString(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT,
                DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        String passwordWithSalt = salt + new String(BASE64.encode(password.getBytes(StandardCharsets.UTF_8)));
        return new String(BASE64.encode(passwordWithSalt.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 使用Base64解码并去除盐值解密密码。如果加密功能未启用则直接返回原密码。
     *
     * @param password the encoded password
     * @return the decoded plain text password
     */
    public static String decodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return StringUtils.EMPTY;
        }

        // if encryption is not turned on, return directly
        boolean encryptionEnable = PropertyUtils.getBoolean(DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE, false);
        if (!encryptionEnable) {
            return password;
        }

        // Using Base64 + salt to process password
        String salt = PropertyUtils.getString(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT,
                DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        String passwordWithSalt = new String(BASE64.decode(password), StandardCharsets.UTF_8);
        if (!passwordWithSalt.startsWith(salt)) {
            logger.warn("There is a password and salt mismatch: {} ", password);
            return password;
        }
        return new String(BASE64.decode(passwordWithSalt.substring(salt.length())), StandardCharsets.UTF_8);
    }

}
