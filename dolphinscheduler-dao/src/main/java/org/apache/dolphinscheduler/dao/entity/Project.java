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

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 项目实体，映射到 t_ds_project 表，表示 DolphinScheduler 中的项目管理单元。
 * 一个项目下可包含多个流程定义，项目是权限管理的基本粒度。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_project")
public class Project {

    /** 项目主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 创建用户 ID */
    @TableField("user_id")
    private Integer userId;

    /** 非数据库字段：用户名 */
    @TableField(exist = false)
    private String userName;

    /** 项目编码，全局唯一 */
    private long code;

    /** 项目名称 */
    private String name;

    /** 项目描述 */
    private String description;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 非数据库字段：当前用户对该项目的权限值 */
    @TableField(exist = false)
    private int perm;

    /** 非数据库字段：项目下的流程定义数量 */
    @TableField(exist = false)
    private int defCount;

    /** 非数据库字段：项目下正在运行的流程实例数量 */
    @TableField(exist = false)
    private int instRunningCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Project project = (Project) o;

        if (id != project.id) {
            return false;
        }
        return name.equals(project.name);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }
}
