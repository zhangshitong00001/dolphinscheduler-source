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

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYY_MM_DD_HH_MM_SS;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.base.Strings;

/**
 * JSON工具类，基于Jackson提供JSON序列化、反序列化和节点操作功能。
 * 支持LocalDateTime的自动序列化与反序列化，配置了宽松的解析策略。
 * 该类为工具类，不可实例化。
 */
public class JSONUtils {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

    static {
        logger.info("init timezone: {}", TimeZone.getDefault());
    }

    private static final SimpleModule LOCAL_DATE_TIME_MODULE = new SimpleModule()
            .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer())
            .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

    /**
     * can use static singleton, inject: just make sure to reuse!
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
            .registerModule(LOCAL_DATE_TIME_MODULE)
            .setTimeZone(TimeZone.getDefault())
            .setDateFormat(new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS));

    private JSONUtils() {
        throw new UnsupportedOperationException("Construct JSONUtils");
    }

    public static synchronized void setTimeZone(TimeZone timeZone) {
        objectMapper.setTimeZone(timeZone);
    }

    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static JsonNode toJsonNode(Object obj) {
        return objectMapper.valueToTree(obj);
    }

    /**
     * 将对象转换为JSON字符串，支持指定序列化特性。
     *
     * @param object 要序列化的对象
     * @param feature Jackson序列化特性
     * @return JSON字符串，序列化失败返回null
     */
    public static String toJsonString(Object object, SerializationFeature feature) {
        try {
            ObjectWriter writer = objectMapper.writer(feature);
            return writer.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("object to json exception!", e);
        }

        return null;
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象。
     * 注意：由于Java类型擦除，该方法不适用于泛型类型。如需反序列化泛型类型，请使用{@link #parseObject(String, TypeReference)}。
     *
     * @param json JSON字符串
     * @param clazz 目标类型的Class对象
     * @param <T> 目标类型
     * @return 反序列化后的对象，如果json为空或解析失败则返回null
     */
    public static @Nullable <T> T parseObject(String json, Class<T> clazz) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Parse object exception, jsonStr: {}, class: {}", json, clazz, e);
        }
        return null;
    }

    /**
     * 将字节数组反序列化为指定类型的对象。
     *
     * @param src 字节数组
     * @param clazz 目标类型的Class对象
     * @param <T> 目标类型
     * @return 反序列化后的对象，如果src为null则返回null
     */
    public static <T> T parseObject(byte[] src, Class<T> clazz) {
        if (src == null) {
            return null;
        }
        String json = new String(src, UTF_8);
        return parseObject(json, clazz);
    }

    /**
     * 将JSON数组字符串反序列化为指定类型的List。
     *
     * @param json JSON数组字符串
     * @param clazz 列表元素类型
     * @param <T> 列表元素类型
     * @return 反序列化后的List，如果json为空或解析失败则返回空列表
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (Strings.isNullOrEmpty(json)) {
            return Collections.emptyList();
        }

        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return objectMapper.readValue(json, listType);
        } catch (Exception e) {
            logger.error("parse list exception!", e);
        }

        return Collections.emptyList();
    }

    /**
     * 检查JSON字符串是否合法。
     *
     * @param json JSON字符串
     * @return 如果是合法的JSON返回true，否则返回false
     */
    public static boolean checkJsonValid(String json) {

        if (Strings.isNullOrEmpty(json)) {
            return false;
        }

        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            logger.error("check json object valid exception!", e);
        }

        return false;
    }

    /**
     * 在JsonNode及其子节点中查找指定字段的值。
     * 如果当前节点或其后代节点中没有找到匹配的字段，返回null。
     *
     * @param jsonNode JSON节点
     * @param fieldName 要查找的字段名
     * @return 第一个匹配节点的文本值，未找到则返回null
     */
    public static String findValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.findValue(fieldName);

        if (node == null) {
            return null;
        }

        return node.asText();
    }

    /**
     * 将JSON字符串转换为Map&lt;String, String&gt;。
     *
     * @param json JSON字符串
     * @return 转换后的Map，如果json为空则返回空Map
     */
    public static Map<String, String> toMap(String json) {
        return parseObject(json, new TypeReference<Map<String, String>>() {
        });
    }

    /**
     * 将JSON字符串转换为指定键值类型的Map。
     *
     * @param json JSON字符串
     * @param classK 键的类型
     * @param classV 值的类型
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 转换后的Map，如果json为空或解析失败则返回空Map
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> classK, Class<V> classV) {
        if (Strings.isNullOrEmpty(json)) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<K, V>>() {
            });
        } catch (Exception e) {
            logger.error("json to map exception!", e);
        }

        return Collections.emptyMap();
    }

    /**
     * 从JSON字符串中获取指定节点的字符串值，不管节点的实际类型。
     *
     * @param json JSON字符串
     * @param nodeName 节点名称
     * @return 节点的字符串值，未找到或解析失败返回空字符串
     */
    public static String getNodeString(String json, String nodeName) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode jsonNode = rootNode.findValue(nodeName);
            if (Objects.isNull(jsonNode)) {
                return "";
            }
            return jsonNode.isTextual() ? jsonNode.asText() : jsonNode.toString();
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * 将JSON字符串反序列化为指定TypeReference类型的对象，支持泛型类型。
     *
     * @param json JSON字符串
     * @param type TypeReference类型引用
     * @param <T> 目标类型
     * @return 反序列化后的对象，如果json为空或解析失败则返回null
     */
    public static <T> T parseObject(String json, TypeReference<T> type) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            logger.error("json to map exception!", e);
        }

        return null;
    }

    /**
     * 将对象序列化为JSON字符串。
     *
     * @param object 要序列化的对象
     * @return JSON字符串
     * @throws RuntimeException 序列化失败时抛出
     */
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Object json deserialization exception.", e);
        }
    }

    public static String toPrettyJsonString(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Object json deserialization exception.", e);
        }
    }

    /**
     * 将对象序列化为JSON字节数组。
     *
     * @param obj 要序列化的对象
     * @param <T> 对象类型
     * @return JSON字节数组，如果obj为null则返回null
     */
    public static <T> byte[] toJsonByteArray(T obj) {
        if (obj == null) {
            return null;
        }
        String json = "";
        try {
            json = toJsonString(obj);
        } catch (Exception e) {
            logger.error("json serialize exception.", e);
        }

        return json.getBytes(UTF_8);
    }

    public static ObjectNode parseObject(String text) {
        try {
            if (StringUtils.isEmpty(text)) {
                return parseObject(text, ObjectNode.class);
            } else {
                return (ObjectNode) objectMapper.readTree(text);
            }
        } catch (Exception e) {
            throw new RuntimeException("String json deserialization exception.", e);
        }
    }

    public static ArrayNode parseArray(String text) {
        try {
            return (ArrayNode) objectMapper.readTree(text);
        } catch (Exception e) {
            throw new RuntimeException("Json deserialization exception.", e);
        }
    }

    /**
     * JSON原始数据序列化器，将字符串作为原始JSON值写入。
     */
    public static class JsonDataSerializer extends JsonSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeRawValue(value);
        }

    }

    /**
     * JSON原始数据反序列化器，将JSON节点转换为字符串形式。
     */
    public static class JsonDataDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node instanceof TextNode) {
                return node.asText();
            } else {
                return node.toString();
            }
        }

    }

    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

        @Override
        public void serialize(LocalDateTime value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(formatter));
        }
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
            return LocalDateTime.parse(p.getValueAsString(), formatter);
        }
    }
}
