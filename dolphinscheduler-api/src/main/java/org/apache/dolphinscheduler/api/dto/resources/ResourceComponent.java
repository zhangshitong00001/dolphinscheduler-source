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

package org.apache.dolphinscheduler.api.dto.resources;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * 资源组件抽象类。作为资源树节点的基类，包含资源的通用属性，支持子节点管理。可被目录（Directory）或文件（FileLeaf）继承。
 */
@Data
@NoArgsConstructor
@JsonPropertyOrder({"id", "pid", "name", "fullName", "description", "isDirctory", "children", "type"})
public abstract class ResourceComponent {

    public ResourceComponent(int id, int pid, String name, String fullName, String description, boolean isDirctory) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isDirctory = isDirctory;
        int directoryFlag = isDirctory ? 1 : 0;
        this.idValue = String.format("%s_%s", id, directoryFlag);
    }

    /** 资源ID */
    protected int id;
    /** 父级资源ID */
    protected int pid;
    /** 资源名称 */
    protected String name;
    /** 当前目录 */
    protected String currentDir;
    /** 资源全名 */
    protected String fullName;
    /** 资源描述 */
    protected String description;
    /** 是否为目录 */
    protected boolean isDirctory;
    /** ID值（格式：id_是否目录标记） */
    protected String idValue;
    /** 资源类型 */
    protected ResourceType type;
    /** 子资源组件列表 */
    protected List<ResourceComponent> children = new ArrayList<>();

    /**
     * 添加子资源组件
     * @param resourceComponent 资源组件
     */
    public void add(ResourceComponent resourceComponent) {
        children.add(resourceComponent);
    }

    /**
     * 设置ID值
     * @param id         资源ID
     * @param isDirctory 是否为目录
     */
    public void setIdValue(int id, boolean isDirctory) {
        int directoryFlag = isDirctory ? 1 : 0;
        this.idValue = String.format("%s_%s", id, directoryFlag);
    }
}
