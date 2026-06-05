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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 数据源-用户关联实体，映射到 t_ds_relation_datasource_user 表，表示数据源与用户之间的授权关系。
 * 通过此关联表实现数据源级别的权限控制，指定哪些用户可以访问特定的数据源。
 */
@Data
@TableName("t_ds_relation_datasource_user")
public class DatasourceUser {

    /** 关联关系主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户 ID，对应 t_ds_user 表的 id */
    private int userId;

    /** 数据源 ID，对应 t_ds_datasource 表的 id */
    private int datasourceId;

    /** 权限编码，控制用户对数据源的访问级别（如只读、读写等） */
    private int perm;
    /** 授权创建时间 */
    private Date createTime;

    /** 最后更新时间 */
    private Date updateTime;
}
