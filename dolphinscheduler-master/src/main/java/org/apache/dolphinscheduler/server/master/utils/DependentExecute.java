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

package org.apache.dolphinscheduler.server.master.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 依赖执行器，负责评估依赖任务列表中的各个依赖项是否满足条件。
 * 支持按日期范围查询依赖的工作流实例和任务状态，根据依赖关系（AND/OR）计算最终的依赖结果。
 * 被 DependentTaskProcessor 使用，是依赖任务判断的核心组件。
 */
public class DependentExecute {

    /**
     * 流程服务。
     */
    private final ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);

    /**
     * 依赖项列表。
     */
    private List<DependentItem> dependItemList;

    /**
     * 依赖关系（AND 或 OR）。
     */
    private DependentRelation relation;

    /**
     * 模型的依赖结果。
     */
    private DependResult modelDependResult = DependResult.WAITING;

    /**
     * 依赖结果缓存。
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(DependentExecute.class);

    /**
     * 构造依赖执行器。
     *
     * @param itemList 依赖项列表
     * @param relation 依赖关系
     */
    public DependentExecute(List<DependentItem> itemList, DependentRelation relation) {
        this.dependItemList = itemList;
        this.relation = relation;
    }

    /**
     * 计算单个依赖项的依赖结果，根据时间范围查询对应的任务实例状态。
     *
     * @param dependentItem 依赖项
     * @param currentTime   当前时间
     * @return 依赖结果
     */
    private DependResult getDependentResultForItem(DependentItem dependentItem, Date currentTime) {
        List<DateInterval> dateIntervals =
                DependentUtils.getDateIntervalList(currentTime, dependentItem.getDateValue());
        return calculateResultForTasks(dependentItem, dateIntervals);
    }

    /**
     * 按时间间隔列表计算单个依赖项的依赖结果。
     *
     * @param dependentItem 依赖项
     * @param dateIntervals 日期间隔列表
     * @return 依赖结果
     */
    private DependResult calculateResultForTasks(DependentItem dependentItem,
                                                 List<DateInterval> dateIntervals) {

        DependResult result = DependResult.FAILED;
        for (DateInterval dateInterval : dateIntervals) {
            ProcessInstance processInstance = findLastProcessInterval(dependentItem.getDefinitionCode(),
                    dateInterval);
            if (processInstance == null) {
                return DependResult.WAITING;
            }
            // need to check workflow for updates, so get all task and check the task state
            if (dependentItem.getDepTaskCode() == Constants.DEPENDENT_ALL_TASK_CODE) {
                result = dependResultByProcessInstance(processInstance);
            } else {
                result = getDependTaskResult(dependentItem.getDepTaskCode(), processInstance);
            }
            if (result != DependResult.SUCCESS) {
                break;
            }
        }
        return result;
    }

    /**
     * 依赖类型为 DEPENDENT_ALL 时，根据工作流实例的状态判断依赖结果。
     *
     * @param processInstance 工作流实例
     * @return 依赖结果
     */
    private DependResult dependResultByProcessInstance(ProcessInstance processInstance) {
        if (!processInstance.getState().isFinished()) {
            return DependResult.WAITING;
        }
        if (processInstance.getState().isSuccess()) {
            return DependResult.SUCCESS;
        }
        return DependResult.FAILED;
    }

    /**
     * 根据任务编码获取依赖任务的执行结果。
     *
     * @param taskCode         任务编码
     * @param processInstance  工作流实例
     * @return 依赖结果
     */
    private DependResult getDependTaskResult(long taskCode, ProcessInstance processInstance) {
        DependResult result;
        TaskInstance taskInstance = null;
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());

        for (TaskInstance task : taskInstanceList) {
            if (task.getTaskCode() == taskCode) {
                taskInstance = task;
                break;
            }
        }

        if (taskInstance == null) {
            // cannot find task in the process instance
            // maybe because process instance is running or failed.
            if (processInstance.getState().isFinished()) {
                result = DependResult.FAILED;
            } else {
                return DependResult.WAITING;
            }
        } else {
            result = getDependResultByState(taskInstance.getState());
        }

        return result;
    }

    /**
     * 查找在指定时间范围内最近的工作流实例（包括调度运行和手动运行）。
     * 1. 手动运行且在时间区间内完成
     * 2. 调度运行且调度时间在时间区间内
     *
     * @param definitionCode 工作流定义编码
     * @param dateInterval   日期区间
     * @return 最近的工作流实例
     */
    private ProcessInstance findLastProcessInterval(Long definitionCode, DateInterval dateInterval) {

        ProcessInstance lastSchedulerProcess =
                processService.findLastSchedulerProcessInterval(definitionCode, dateInterval);

        ProcessInstance lastManualProcess = processService.findLastManualProcessInterval(definitionCode, dateInterval);

        if (lastManualProcess == null) {
            return lastSchedulerProcess;
        }
        if (lastSchedulerProcess == null) {
            return lastManualProcess;
        }

        // In the time range, there are both manual and scheduled workflow instances, return the last workflow instance
        return lastManualProcess.getId() > lastSchedulerProcess.getId() ? lastManualProcess : lastSchedulerProcess;
    }

    /**
     * 根据任务/工作流实例的运行状态获取依赖结果。
     *
     * @param state 任务执行状态
     * @return 依赖结果
     */
    private DependResult getDependResultByState(TaskExecutionStatus state) {

        if (!state.isFinished()) {
            return DependResult.WAITING;
        } else if (state.isSuccess()) {
            return DependResult.SUCCESS;
        } else {
            return DependResult.FAILED;
        }
    }

    /**
     * 判断所有依赖项是否已完成评估。
     *
     * @param currentTime 当前时间
     * @return 是否已完成
     */
    public boolean finish(Date currentTime) {
        if (modelDependResult == DependResult.WAITING) {
            modelDependResult = getModelDependResult(currentTime);
            return false;
        }
        return true;
    }

    /**
     * 计算所有依赖项的综合依赖结果。
     *
     * @param currentTime 当前时间
     * @return 综合依赖结果
     */
    public DependResult getModelDependResult(Date currentTime) {

        List<DependResult> dependResultList = new ArrayList<>();

        for (DependentItem dependentItem : dependItemList) {
            DependResult dependResult = getDependResultForItem(dependentItem, currentTime);
            if (dependResult != DependResult.WAITING) {
                dependResultMap.put(dependentItem.getKey(), dependResult);
            }
            dependResultList.add(dependResult);
        }
        modelDependResult = DependentUtils.getDependResultForRelation(this.relation, dependResultList);
        return modelDependResult;
    }

    /**
     * 获取单个依赖项的依赖结果（优先从缓存中获取）。
     *
     * @param item        依赖项
     * @param currentTime 当前时间
     * @return 依赖结果
     */
    private DependResult getDependResultForItem(DependentItem item, Date currentTime) {
        String key = item.getKey();
        if (dependResultMap.containsKey(key)) {
            return dependResultMap.get(key);
        }
        return getDependentResultForItem(item, currentTime);
    }

    public Map<String, DependResult> getDependResultMap() {
        return dependResultMap;
    }

}
