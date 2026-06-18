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
 * 流程血缘关系 DTO，表示工作流中上游任务与下游任务之间的依赖关系。
 * 用于追踪数据在任务节点之间的流转路径。
 */
public class ProcessLineage {

    /** 项目编码 */
    private long projectCode;

    /** 下游任务编码 */
    private long postTaskCode;

    /** 下游任务版本号 */
    private int postTaskVersion;

    /** 上游任务编码 */
    private long preTaskCode;

    /** 上游任务版本号 */
    private int preTaskVersion;

    /** 流程定义编码 */
    private long processDefinitionCode;

    /** 流程定义版本号 */
    private int processDefinitionVersion;

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public long getPostTaskCode() {
        return postTaskCode;
    }

    public void setPostTaskCode(long postTaskCode) {
        this.postTaskCode = postTaskCode;
    }

    public int getPostTaskVersion() {
        return postTaskVersion;
    }

    public void setPostTaskVersion(int postTaskVersion) {
        this.postTaskVersion = postTaskVersion;
    }

    public long getPreTaskCode() {
        return preTaskCode;
    }

    public void setPreTaskCode(long preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public int getPreTaskVersion() {
        return preTaskVersion;
    }

    public void setPreTaskVersion(int preTaskVersion) {
        this.preTaskVersion = preTaskVersion;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }
}
