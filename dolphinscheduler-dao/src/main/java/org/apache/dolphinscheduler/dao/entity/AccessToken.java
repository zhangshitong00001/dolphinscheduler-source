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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 访问令牌实体，映射到 t_ds_access_token 表，表示用户的 API 访问令牌。
 * 用户可以通过创建访问令牌来使用 REST API 进行认证和授权，令牌有过期时间限制。
 */
@Data
@TableName("t_ds_access_token")
public class AccessToken {

    /** 访问令牌主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 关联的用户 ID，对应 t_ds_user 表的 id */
    @TableField(value = "user_id")
    private int userId;
    /** 访问令牌字符串，用于 API 认证 */
    @TableField(value = "token")
    private String token;
    /** 令牌过期时间，过期后将无法用于认证 */
    @TableField(value = "expire_time")
    private Date expireTime;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;
    /** 非数据库字段：关联的用户名，通过 userId 关联 t_ds_user 表查询填充 */
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
        AccessToken that = (AccessToken) o;

        if (id != that.id) {
            return false;
        }
        if (userId != that.userId) {
            return false;
        }
        if (userName != null && !userName.equals(that.userName)) {
            return false;
        }
        if (token != null && !token.equals(that.token)) {
            return false;
        }
        if (expireTime != null && !expireTime.equals(that.expireTime)) {
            return false;
        }
        if (createTime != null && !createTime.equals(that.createTime)) {
            return false;
        }
        if (updateTime != null && !updateTime.equals(that.updateTime)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + userId;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (expireTime != null ? expireTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        return result;
    }
}
