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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;

import java.util.List;

/**
 * 依赖工作流定义实体，非数据库表映射，用于表示 Dependent 类型任务的依赖配置信息。
 * 封装了被依赖的上游工作流定义信息，包括编码、名称、版本、任务参数和工作组等，用于在依赖任务执行时解析和检查上游依赖状态。
 */
public class DependentProcessDefinition {

    /** 工作流定义编码，全局唯一标识 */
    private long processDefinitionCode;

    /** 工作流定义名称 */
    private String processDefinitionName;

    /** 工作流定义版本号 */
    private int processDefinitionVersion;

    /** 任务定义编码，Dependent 任务的任务编码 */
    private long taskDefinitionCode;

    /** 任务参数字符串，JSON 格式，包含依赖配置列表 */
    private String taskParams;

    /** 调度工作组名称 */
    private String workerGroup;

    /**
     * get dependent cycle
     * @return CycleEnum
     */
    public CycleEnum getDependentCycle(long upstreamProcessDefinitionCode) {
        DependentParameters dependentParameters = this.getDependentParameters();
        List<DependentTaskModel> dependentTaskModelList = dependentParameters.getDependTaskList();

        for (DependentTaskModel dependentTaskModel : dependentTaskModelList) {
            List<DependentItem> dependentItemList = dependentTaskModel.getDependItemList();
            for (DependentItem dependentItem : dependentItemList) {
                if (upstreamProcessDefinitionCode == dependentItem.getDefinitionCode()) {
                    return cycle2CycleEnum(dependentItem.getCycle());
                }
            }
        }

        return CycleEnum.DAY;
    }

    public CycleEnum cycle2CycleEnum(String cycle) {
        CycleEnum cycleEnum = null;

        switch (cycle) {
            case "day":
                cycleEnum = CycleEnum.DAY;
                break;
            case "hour":
                cycleEnum =  CycleEnum.HOUR;
                break;
            case "week":
                cycleEnum =  CycleEnum.WEEK;
                break;
            case "month":
                cycleEnum =  CycleEnum.MONTH;
                break;
            default:
                break;
        }
        return cycleEnum;
    }

    public DependentParameters getDependentParameters() {
        return JSONUtils.parseObject(getDependence(), DependentParameters.class);
    }

    public String getDependence() {
        return JSONUtils.getNodeString(this.taskParams, Constants.DEPENDENCE);
    }

    public String getProcessDefinitionName() {
        return this.processDefinitionName;
    }

    public void setProcessDefinitionName(String name) {
        this.processDefinitionName = name;
    }

    public long getProcessDefinitionCode() {
        return this.processDefinitionCode;
    }

    public void setProcessDefinitionCode(long code) {
        this.processDefinitionCode = code;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public long getTaskDefinitionCode() {
        return this.taskDefinitionCode;
    }

    public void setTaskDefinitionCode(long code) {
        this.taskDefinitionCode = code;
    }

    public String getTaskParams() {
        return this.taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public String getWorkerGroup() {
        return this.workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

}
