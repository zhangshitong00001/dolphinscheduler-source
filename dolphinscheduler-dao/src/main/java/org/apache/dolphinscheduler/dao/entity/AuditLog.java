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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 审计日志实体，映射到 t_ds_audit_log 表，记录用户对系统资源的操作历史。
 * 用于安全审计和追溯，记录谁在什么时间对哪个资源执行了什么操作。
 */
@TableName("t_ds_audit_log")
public class AuditLog {

    /** 审计日志主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 操作用户 ID，对应 t_ds_user 表的 id */
    private Integer userId;

    /** 资源类型，标识被操作的是哪种资源（如项目、工作流定义、任务定义、数据源等） */
    private Integer resourceType;

    /** 操作类型，标识执行了什么操作（如创建、修改、删除、授权等） */
    private Integer operation;

    /** 资源 ID，被操作的资源主键 */
    private Integer resourceId;

    /** 非数据库字段：操作者用户名，通过 userId 关联 t_ds_user 表查询填充 */
    @TableField(exist = false)
    private String userName;

    /** 操作时间 */
    private Date time;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getResourceType() {
        return resourceType;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
