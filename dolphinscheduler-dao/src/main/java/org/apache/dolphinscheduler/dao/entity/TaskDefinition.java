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
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;

/**
 * 任务定义实体，映射到 t_ds_task_definition 表，存储任务的元数据定义信息。
 */
@Data
@TableName("t_ds_task_definition")
public class TaskDefinition {

    /** 任务定义主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 任务编码，用于标识同一任务的唯一编码 */
    private long code;

    /** 任务名称 */
    private String name;

    /** 任务版本号 */
    private int version;

    /** 任务描述 */
    private String description;

    /** 所属项目编码 */
    private long projectCode;

    /** 任务所属用户 ID */
    private int userId;

    /** 任务类型 */
    private String taskType;

    /** 用户自定义任务参数，JSON 格式 */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String taskParams;

    /** 非数据库字段：用户自定义参数列表 */
    @TableField(exist = false)
    private List<Property> taskParamList;

    /** 非数据库字段：用户自定义参数映射表 */
    @TableField(exist = false)
    private Map<String, String> taskParamMap;

    /** 任务是否有效：是/否 */
    private Flag flag;

    /** 任务优先级 */
    private Priority taskPriority;

    /** 非数据库字段：用户名 */
    @TableField(exist = false)
    private String userName;

    /** 非数据库字段：项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** Worker 分组名称 */
    private String workerGroup;

    /** 环境编码 */
    private long environmentCode;

    /** 失败重试次数 */
    private int failRetryTimes;

    /** 失败重试间隔 */
    private int failRetryInterval;

    /** 超时告警标志 */
    private TimeoutFlag timeoutFlag;

    /** 超时通知策略 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private TaskTimeoutStrategy timeoutNotifyStrategy;

    /** 任务超时告警时间，单位：分钟 */
    private int timeout;

    /** 延时执行时间 */
    private int delayTime;

    /** 资源 ID 列表 */
    private String resourceIds;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 非数据库字段：修改人用户名 */
    @TableField(exist = false)
    private String modifyBy;

    /** 任务组 ID */
    private int taskGroupId;
    /** 任务组优先级 */
    private int taskGroupPriority;

    /** CPU 配额 */
    private Integer cpuQuota;

    /** 最大内存 */
    private Integer memoryMax;

    /** 任务执行类型 */
    private TaskExecuteType taskExecuteType;

    public TaskDefinition() {
    }

    public TaskDefinition(long code, int version) {
        this.code = code;
        this.version = version;
    }

    public List<Property> getTaskParamList() {
        JsonNode localParams = JSONUtils.parseObject(taskParams).findValue("localParams");
        if (localParams != null) {
            taskParamList = JSONUtils.toList(localParams.toString(), Property.class);
        }

        return taskParamList;
    }

    public Map<String, String> getTaskParamMap() {
        if (taskParamMap == null && !Strings.isNullOrEmpty(taskParams)) {
            JsonNode localParams = JSONUtils.parseObject(taskParams).findValue("localParams");

            // If a jsonNode is null, not only use !=null, but also it should use the isNull method to be estimated.
            if (localParams != null && !localParams.isNull()) {
                List<Property> propList = JSONUtils.toList(localParams.toString(), Property.class);

                if (CollectionUtils.isNotEmpty(propList)) {
                    taskParamMap = new HashMap<>();
                    for (Property property : propList) {
                        taskParamMap.put(property.getProp(), property.getValue());
                    }
                }
            }
        }
        return taskParamMap;
    }

    public String getDependence() {
        return JSONUtils.getNodeString(this.taskParams, Constants.DEPENDENCE);
    }

    public Integer getCpuQuota() {
        return cpuQuota == null ? -1 : cpuQuota;
    }

    public Integer getMemoryMax() {
        return memoryMax == null ? -1 : memoryMax;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        TaskDefinition that = (TaskDefinition) o;
        return failRetryTimes == that.failRetryTimes
                && failRetryInterval == that.failRetryInterval
                && timeout == that.timeout
                && delayTime == that.delayTime
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(taskType, that.taskType)
                && Objects.equals(taskParams, that.taskParams)
                && flag == that.flag
                && taskPriority == that.taskPriority
                && Objects.equals(workerGroup, that.workerGroup)
                && timeoutFlag == that.timeoutFlag
                && timeoutNotifyStrategy == that.timeoutNotifyStrategy
                && (Objects.equals(resourceIds, that.resourceIds)
                        || ("".equals(resourceIds) && that.resourceIds == null)
                        || ("".equals(that.resourceIds) && resourceIds == null))
                && environmentCode == that.environmentCode
                && taskGroupId == that.taskGroupId
                && taskGroupPriority == that.taskGroupPriority
                && Objects.equals(cpuQuota, that.cpuQuota)
                && Objects.equals(memoryMax, that.memoryMax)
                && Objects.equals(taskExecuteType, that.taskExecuteType);
    }
}
