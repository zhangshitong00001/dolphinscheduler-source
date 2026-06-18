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

import org.apache.dolphinscheduler.common.enums.UserType;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体，映射到 t_ds_user 表，存储系统用户信息。
 */
@Data
@TableName("t_ds_user")
public class User {

    /** 用户主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户名 */
    private String userName;

    /** 用户密码 */
    private String userPassword;

    /** 邮箱 */
    private String email;

    /** 手机号码 */
    private String phone;

    /** 用户类型：管理员/普通用户 */
    private UserType userType;

    /** 租户 ID */
    private int tenantId;

    /** 用户状态：0 禁用，1 启用 */
    private int state;

    /** 非数据库字段：租户编码 */
    @TableField(exist = false)
    private String tenantCode;

    /** 非数据库字段：队列名称 */
    @TableField(exist = false)
    private String queueName;

    /** 非数据库字段：告警组名称 */
    @TableField(exist = false)
    private String alertGroup;

    /** 队列标识 */
    private String queue;

    /** 时区 */
    private String timeZone;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

}
