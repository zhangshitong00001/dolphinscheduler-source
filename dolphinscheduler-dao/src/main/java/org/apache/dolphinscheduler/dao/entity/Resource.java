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

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 资源文件实体，映射到 t_ds_resources 表，表示系统中上传和管理的文件资源。
 * 支持目录结构，可用于存放任务执行所需的脚本、配置文件等。
 */
@Data
@NoArgsConstructor
@TableName("t_ds_resources")
public class Resource {

    /** 资源主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 父资源 ID，用于构建资源目录树 */
    private int pid;

    /** 资源别名 */
    private String alias;

    /** 资源全名（含路径） */
    private String fullName;

    /** 是否为目录 */
    private boolean isDirectory = false;

    /** 资源描述 */
    private String description;

    /** 文件名 */
    private String fileName;

    /** 创建者用户 ID */
    private int userId;

    /** 资源类型 */
    private ResourceType type;

    /** 资源文件大小（字节） */
    private long size;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 非数据库字段：用户名 */
    @TableField(exist = false)
    private String userName;

    public Resource(int id, String alias, String fileName, String description, int userId,
                    ResourceType type, long size,
                    Date createTime, Date updateTime) {
        this.id = id;
        this.alias = alias;
        this.fileName = fileName;
        this.description = description;
        this.userId = userId;
        this.type = type;
        this.size = size;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Resource(int id, int pid, String alias, String fullName, boolean isDirectory) {
        this.id = id;
        this.pid = pid;
        this.alias = alias;
        this.fullName = fullName;
        this.isDirectory = isDirectory;
    }

    public Resource(int pid, String alias, String fullName, boolean isDirectory, String description, String fileName,
                    int userId, ResourceType type, long size, Date createTime, Date updateTime) {
        this.pid = pid;
        this.alias = alias;
        this.fullName = fullName;
        this.isDirectory = isDirectory;
        this.description = description;
        this.fileName = fileName;
        this.userId = userId;
        this.type = type;
        this.size = size;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource resource = (Resource) o;

        if (id != resource.id) {
            return false;
        }
        return alias.equals(resource.alias);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + alias.hashCode();
        return result;
    }
}
