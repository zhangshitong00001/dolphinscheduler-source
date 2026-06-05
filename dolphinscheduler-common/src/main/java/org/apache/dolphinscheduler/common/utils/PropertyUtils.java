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

package org.apache.dolphinscheduler.common.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.COMMON_PROPERTIES_PATH;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * 属性配置工具类，提供配置文件属性的读取和类型转换功能。
 * 支持String、Integer、Long、Boolean、Double、Enum等类型的属性获取，
 * 以及前缀匹配的属性集检索。在类加载时自动加载common.properties。
 * 该类为工具类，不可实例化。
 */
public class PropertyUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static final Properties properties = new Properties();

    private PropertyUtils() {
        throw new UnsupportedOperationException("Construct PropertyUtils");
    }

    static {
        loadPropertyFile(COMMON_PROPERTIES_PATH);
    }

    public static synchronized void loadPropertyFile(String... propertyFiles) {
        for (String fileName : propertyFiles) {
            try (InputStream fis = PropertyUtils.class.getResourceAsStream(fileName);) {
                Properties subProperties = new Properties();
                subProperties.load(fis);
                subProperties.forEach((k, v) -> {
                    logger.debug("Get property {} -> {}", k, v);
                });
                properties.putAll(subProperties);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                System.exit(1);
            }
        }

        // Override from system properties
        System.getProperties().forEach((k, v) -> {
            final String key = String.valueOf(k);
            logger.info("Overriding property from system property: {}", key);
            PropertyUtils.setValue(key, String.valueOf(v));
        });
    }

    /**
     * 判断资源上传功能是否启用。
     *
     * @return 如果配置了非NONE的资源存储类型则返回true
     */
    public static boolean getResUploadStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(
                Strings.isNullOrEmpty(resUploadStartupType) ? ResUploadType.NONE.name() : resUploadStartupType);
        return resUploadType != ResUploadType.NONE;
    }

    /**
     * 获取指定key的属性值。
     *
     * @param key 属性名称
     * @return 属性值，如果不存在则返回null
     */
    public static String getString(String key) {
        return properties.getProperty(key.trim());
    }

    /**
     * 获取指定key的属性值并转换为大写。
     *
     * @param key 属性名称
     * @return 大写的属性值，如果不存在则返回null
     */
    public static String getUpperCaseString(String key) {
        String val = getString(key);
        return Strings.isNullOrEmpty(val) ? val : val.toUpperCase();
    }

    /**
     * 获取指定key的属性值，如果不存在则返回默认值。
     *
     * @param key 属性名称
     * @param defaultVal 默认值
     * @return 属性值或默认值
     */
    public static String getString(String key, String defaultVal) {
        String val = getString(key);
        return Strings.isNullOrEmpty(val) ? defaultVal : val;
    }

    /**
     * 获取指定key的int类型属性值，不存在则返回-1。
     *
     * @param key 属性名称
     * @return int类型属性值，不存在返回-1
     */
    public static int getInt(String key) {
        return getInt(key, -1);
    }

    /**
     * 获取指定key的int类型属性值，不存在或格式错误则返回默认值。
     *
     * @param key 属性名称
     * @param defaultValue 默认值
     * @return int类型属性值
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 获取指定key的boolean类型属性值，不存在则返回false。
     *
     * @param key 属性名称
     * @return boolean类型属性值
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * 获取指定key的boolean类型属性值，不存在则返回默认值。
     *
     * @param key 属性名称
     * @param defaultValue 默认值
     * @return boolean类型属性值
     */
    public static Boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        return Strings.isNullOrEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * 获取指定key的long类型属性值，不存在或格式错误则返回默认值。
     *
     * @param key 属性名称
     * @param defaultValue 默认值
     * @return long类型属性值
     */
    public static long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 获取指定key的long类型属性值，不存在则返回-1。
     *
     * @param key 属性名称
     * @return long类型属性值
     */
    public static long getLong(String key) {
        return getLong(key, -1);
    }

    /**
     * 获取指定key的double类型属性值，不存在或格式错误则返回默认值。
     *
     * @param key 属性名称
     * @param defaultValue 默认值
     * @return double类型属性值
     */
    public static double getDouble(String key, double defaultValue) {
        String value = getString(key);
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 获取指定key的属性值并按分隔符拆分为数组。
     *
     * @param key 属性名称
     * @param splitStr 分隔符
     * @return 拆分后的字符串数组，key不存在则返回空数组
     */
    public static String[] getArray(String key, String splitStr) {
        String value = getString(key);
        if (Strings.isNullOrEmpty(value)) {
            return new String[0];
        }
        return value.split(splitStr);
    }

    /**
     * 获取指定key的枚举类型属性值，不存在则返回默认值。
     *
     * @param key 属性名称
     * @param type 枚举类型
     * @param defaultValue 默认值
     * @param <T> 枚举类型参数
     * @return 枚举值
     */
    public static <T extends Enum<T>> T getEnum(String key, Class<T> type,
                                                T defaultValue) {
        String value = getString(key);
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 获取所有以指定前缀开头的属性，返回完整key-value的Map。
     *
     * @param prefix 要搜索的前缀，如 "fs."
     * @return 匹配的属性和值的Map
     */
    public static Map<String, String> getPrefixedProperties(String prefix) {
        Map<String, String> matchedProperties = new HashMap<>();
        for (String propName : properties.stringPropertyNames()) {
            if (propName.startsWith(prefix)) {
                matchedProperties.put(propName, properties.getProperty(propName));
            }
        }
        return matchedProperties;
    }

    /**
     * 设置属性值。
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public static void setValue(String key, String value) {
        properties.setProperty(key, value);
    }

    public static Map<String, String> getPropertiesByPrefix(String prefix) {
        if (Strings.isNullOrEmpty(prefix)) {
            return null;
        }
        Set<Object> keys = properties.keySet();
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        Map<String, String> propertiesMap = new HashMap<>();
        keys.forEach(k -> {
            if (k.toString().contains(prefix)) {
                propertiesMap.put(k.toString().replaceFirst(prefix + ".", ""), properties.getProperty((String) k));
            }
        });
        return propertiesMap;
    }
}
