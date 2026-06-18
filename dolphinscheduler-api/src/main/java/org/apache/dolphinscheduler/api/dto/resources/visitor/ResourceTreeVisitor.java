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
package org.apache.dolphinscheduler.api.dto.resources.visitor;


import org.apache.dolphinscheduler.api.dto.resources.Directory;
import org.apache.dolphinscheduler.api.dto.resources.FileLeaf;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.dao.entity.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源树访问者。实现Visitor接口，将扁平的资源列表构建为树形结构，递归处理父子节点关系。
 */
public class ResourceTreeVisitor implements Visitor{

    /** 资源列表 */
    private List<Resource> resourceList;

    public ResourceTreeVisitor() {
    }

    /**
     * 构造资源树访问者
     * @param resourceList 资源列表
     */
    public ResourceTreeVisitor(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    /**
     * 访问并构建资源树
     * @return 根资源组件（包含完整树形结构）
     */
    @Override
    public ResourceComponent visit() {
        ResourceComponent rootDirectory = new Directory();
        for (Resource resource : resourceList) {
            // judge whether is root node
            if (rootNode(resource)){
                ResourceComponent tempResourceComponent = getResourceComponent(resource);
                rootDirectory.add(tempResourceComponent);
                tempResourceComponent.setChildren(setChildren(tempResourceComponent.getId(),resourceList));
            }
        }
        return rootDirectory;
    }

    /**
     * 递归设置子节点
     * @param id    父节点ID
     * @param list  资源列表
     * @return 子资源组件列表
     */
    public static List<ResourceComponent> setChildren(int id, List<Resource> list ){
        List<ResourceComponent> childList = new ArrayList<>();
        for (Resource resource : list) {
            if (id == resource.getPid()){
                ResourceComponent tempResourceComponent = getResourceComponent(resource);
                childList.add(tempResourceComponent);
            }
        }
        for (ResourceComponent resourceComponent : childList) {
            resourceComponent.setChildren(setChildren(resourceComponent.getId(),list));
        }
        if (childList.size()==0){
            return new ArrayList<>();
        }
        return childList;
    }

    /**
     * 判断是否为根节点
     * @param resource 资源
     * @return 如果是根节点则返回true
     */
    public boolean rootNode(Resource resource) {

        boolean isRootNode = true;
        if(resource.getPid() != -1 ){
            for (Resource parent : resourceList) {
                if (resource.getPid() == parent.getId()) {
                    isRootNode = false;
                    break;
                }
            }
        }
        return isRootNode;
    }

    /**
     * 根据资源实体获取对应的资源组件
     * @param resource 资源实体
     * @return 资源组件（目录或文件叶子节点）
     */
    private static ResourceComponent getResourceComponent(Resource resource) {
        ResourceComponent tempResourceComponent;
        if(resource.isDirectory()){
            tempResourceComponent = new Directory();
        }else{
            tempResourceComponent = new FileLeaf();
        }

        tempResourceComponent.setName(resource.getAlias());
        tempResourceComponent.setFullName(resource.getFullName().replaceFirst("/",""));
        tempResourceComponent.setId(resource.getId());
        tempResourceComponent.setPid(resource.getPid());
        tempResourceComponent.setIdValue(resource.getId(),resource.isDirectory());
        tempResourceComponent.setDescription(resource.getDescription());
        tempResourceComponent.setType(resource.getType());
        return tempResourceComponent;
    }

}
