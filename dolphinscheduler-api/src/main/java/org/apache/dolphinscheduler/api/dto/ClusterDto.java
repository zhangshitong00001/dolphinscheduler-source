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

package org.apache.dolphinscheduler.api.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 集群 DTO，用于传输集群配置信息，包含集群名称、配置内容和关联的流程定义列表。
 */
@Data
public class ClusterDto {

    private int id;

    /**
     * 集群编码
     */
    private Long code;

    /**
     * 集群名称
     */
    private String name;

    /**
     * 集群配置内容
     */
    private String config;

    private String description;

    private List<String> processDefinitions;

    /**
     * 操作者用户 ID
     */
    private Integer operator;

    private Date createTime;

    private Date updateTime;
}
