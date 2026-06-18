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

import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 数据源实体，映射到 t_ds_datasource 表，表示一个外部数据源连接配置（如 MySQL、Hive、PostgreSQL 等）。
 * 数据源被任务插件引用，用于在任务执行过程中连接和操作外部数据库。
 */
@Data
@TableName("t_ds_datasource")
public class DataSource {

    /** 数据源主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 数据源所属用户 ID，对应 t_ds_user 表的 id */
    private int userId;

    /** 非数据库字段：所属用户名，通过 userId 关联 t_ds_user 表查询填充 */
    @TableField(exist = false)
    private String userName;

    /** 数据源名称，用户自定义的标识名 */
    private String name;

    /** 备注说明 */
    private String note;

    /** 数据源类型枚举，如 MYSQL、POSTGRESQL、HIVE、SPARK、CLICKHOUSE、ORACLE、SQLSERVER 等 */
    private DbType type;

    /** 连接参数字符串，JSON 格式，存储 JDBC 连接地址、用户名、密码等敏感信息 */
    private String connectionParams;

    /** 创建时间 */
    private Date createTime;

    /** 最后更新时间 */
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataSource that = (DataSource) o;

        if (id != that.id) {
            return false;
        }
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }
}
