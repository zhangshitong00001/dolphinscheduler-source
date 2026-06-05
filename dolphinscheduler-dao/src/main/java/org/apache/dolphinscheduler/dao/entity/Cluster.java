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
 * 集群实体，映射到 t_ds_cluster 表，表示一个大数据集群（如 Hadoop/YARN 集群）的配置信息。
 * 工作流任务可以在指定的集群上运行，集群配置包含集群连接地址、配置文件等 JSON 格式的参数。
 */
@Data
@TableName("t_ds_cluster")
public class Cluster {

    /** 集群主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 集群编码，全局唯一标识符 */
    private Long code;

    /** 集群名称，用户自定义的集群标识名 */
    private String name;

    /** 集群配置内容，JSON 格式，存储集群的连接参数和配置文件信息 */
    private String config;

    /** 集群描述信息 */
    private String description;

    /** 操作者用户 ID，最后修改该集群配置的用户 */
    private Integer operator;

    /** 创建时间 */
    private Date createTime;

    /** 最后更新时间 */
    private Date updateTime;
}
