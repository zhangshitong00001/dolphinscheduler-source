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
package org.apache.dolphinscheduler.api.dto.resources.filter;

import org.apache.dolphinscheduler.dao.entity.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源过滤器。根据文件后缀对资源列表进行过滤，返回匹配文件及其所有父级目录。
 */
public class ResourceFilter implements IFilter {
    /** 资源文件后缀 */
    private String suffix;
    /** 资源列表 */
    private List<Resource> resourceList;

    /**
     * 构造资源过滤器
     * @param suffix        资源文件后缀
     * @param resourceList  资源列表
     */
    public ResourceFilter(String suffix, List<Resource> resourceList) {
        this.suffix = suffix;
        this.resourceList = resourceList;
    }

    /**
     * 根据后缀过滤文件
     * @return 按后缀过滤后的文件集合
     */
    public Set<Resource> fileFilter(){
        return resourceList.stream().filter(t -> {
            String alias = t.getAlias();
            return alias.endsWith(suffix);
        }).collect(Collectors.toSet());
    }

    /**
     * 列出所有父级目录
     * @return 父级资源目录集合
     */
    Set<Resource> listAllParent(){
        Set<Resource> parentList =  new HashSet<>();
        Set<Resource> filterFileList = fileFilter();
        for(Resource file:filterFileList){
            parentList.add(file);
            setAllParent(file,parentList);
        }
        return parentList;

    }

    /**
     * 递归设置所有父级目录
     * @param resource   资源
     * @param parentList 父级目录集合
     */
    private void setAllParent(Resource resource,Set<Resource> parentList){
        for (Resource resourceTemp : resourceList) {
            if (resourceTemp.getId() == resource.getPid()) {
                parentList.add(resourceTemp);
                setAllParent(resourceTemp,parentList);
            }
        }
    }

    @Override
    public List<Resource> filter() {
        return new ArrayList<>(listAllParent());
    }
}
