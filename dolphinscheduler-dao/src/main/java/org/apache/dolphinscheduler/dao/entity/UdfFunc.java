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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.IOException;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.google.common.base.Strings;

/**
 * UDF 函数实体，映射到 t_ds_udfs 表，存储用户自定义函数元数据。
 */
@Data
@TableName("t_ds_udfs")
public class UdfFunc {

    /** UDF 函数主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 用户 ID */
    private int userId;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = "UDF";
    }

    /** 非数据库字段：资源类型 */
    @TableField(exist = false)
    private String resourceType = "UDF";
    /** UDF 函数名称 */
    private String funcName;

    /** UDF 类全限定名 */
    private String className;

    /** UDF 参数类型列表 */
    private String argTypes;

    /** UDF 数据库 */
    private String database;

    /** UDF 函数描述 */
    private String description;

    /** 资源 ID */
    private int resourceId;

    /** 资源名称 */
    private String resourceName;

    /** UDF 函数类型：Hive 或 Spark */
    private UdfType type;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 非数据库字段：用户名 */
    @TableField(exist = false)
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UdfFunc udfFunc = (UdfFunc) o;

        if (id != udfFunc.id) {
            return false;
        }
        return !(funcName != null ? !funcName.equals(udfFunc.funcName) : udfFunc.funcName != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (funcName != null ? funcName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return JSONUtils.toJsonString(this);
    }

    public static class UdfFuncDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            if (Strings.isNullOrEmpty(key)) {
                return null;
            }
            return JSONUtils.parseObject(key, UdfFunc.class);
        }
    }
}
