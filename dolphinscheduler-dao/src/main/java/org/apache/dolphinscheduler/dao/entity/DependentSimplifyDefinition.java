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

/**
 * 依赖节点简化定义实体，非数据库表映射，用于前端依赖节点选择器中展示被依赖的工作流定义摘要信息。
 * 只包含工作流定义的编码、名称和版本三个核心字段，便于在依赖配置界面快速查找和选择上游工作流。
 */
public class DependentSimplifyDefinition {

    /** 工作流定义编码，全局唯一标识 */
    private Long code;

    /** 工作流定义名称 */
    private String name;

    /** 工作流定义版本号 */
    private Integer version;

    public Long getCode() {
        return this.code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
