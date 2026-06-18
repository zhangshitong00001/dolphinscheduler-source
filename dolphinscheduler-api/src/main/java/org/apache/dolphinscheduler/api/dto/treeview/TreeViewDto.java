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

package org.apache.dolphinscheduler.api.dto.treeview;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形视图DTO。用于展示工作流的树形结构，包含节点名称、类型、编码、实例列表和子节点信息。
 */
public class TreeViewDto {

    /** 节点名称 */
    private String name;

    /** 节点类型 */
    private String type;

    /** 节点编码 */
    private long code;

    /** 实例列表 */
    private List<Instance> instances = new ArrayList<>();

    /** 子节点列表 */
    private List<TreeViewDto> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public List<TreeViewDto> getChildren() {
        return children;
    }

    public void setChildren(List<TreeViewDto> children) {
        this.children = children;
    }
}
