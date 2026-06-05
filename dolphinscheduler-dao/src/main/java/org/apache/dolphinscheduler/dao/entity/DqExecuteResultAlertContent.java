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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据质量执行结果告警内容实体，非数据库表映射，用于封装数据质量检查失败时发送的告警消息内容。
 * 通过 Builder 模式构建，序列化为 JSON 后发送给告警插件实例。包含检查结果、阈值对比等关键信息。
 */
@JsonInclude(Include.NON_NULL)
public class DqExecuteResultAlertContent implements Serializable {

    /** 工作流定义 ID */
    @JsonProperty(value = "processDefinitionId")
    private long processDefinitionId;
    /** 工作流定义名称 */
    @JsonProperty("processDefinitionName")
    private String  processDefinitionName;
    /** 工作流实例 ID */
    @JsonProperty(value = "processInstanceId")
    private long processInstanceId;
    /** 工作流实例名称 */
    @JsonProperty("processInstanceName")
    private String processInstanceName;
    /** 任务实例 ID */
    @JsonProperty(value = "taskInstanceId")
    private long taskInstanceId;
    /** 任务名称 */
    @JsonProperty("taskName")
    private String taskName;
    /** 数据质量规则类型 */
    @JsonProperty(value = "ruleType")
    private int ruleType;
    /** 数据质量规则名称 */
    @JsonProperty(value = "ruleName")
    private String ruleName;
    /** 统计值，数据质量 SQL 执行得到的实际数值 */
    @JsonProperty(value = "statisticsValue")
    private double statisticsValue;
    /** 比较值，用于对比的期望值 */
    @JsonProperty(value = "comparisonValue")
    private double comparisonValue;
    /** 检查类型 */
    @JsonProperty(value = "checkType")
    private int checkType;
    /** 阈值，数据质量检查的临界值 */
    @JsonProperty(value = "threshold")
    private double threshold;
    /** 操作符，定义统计值与阈值的比较逻辑 */
    @JsonProperty(value = "operator")
    private int operator;
    /** 失败策略，质量检查失败时的处理方式 */
    @JsonProperty(value = "failureStrategy")
    private int failureStrategy;
    /** 用户 ID */
    @JsonProperty(value = "userId")
    private int userId;
    /** 用户名 */
    @JsonProperty("userName")
    private String userName;
    /** 执行状态 */
    @JsonProperty(value = "state")
    private int state;

    /** 错误数据输出路径 */
    @JsonProperty(value = "errorDataPath")
    private String errorDataPath;

    public DqExecuteResultAlertContent(Builder builder) {
        this.processDefinitionId = builder.processDefinitionId;
        this.processDefinitionName = builder.processDefinitionName;
        this.processInstanceId = builder.processInstanceId;
        this.processInstanceName = builder.processInstanceName;
        this.taskInstanceId = builder.taskInstanceId;
        this.taskName = builder.taskName;
        this.ruleType = builder.ruleType;
        this.ruleName = builder.ruleName;
        this.statisticsValue = builder.statisticsValue;
        this.comparisonValue = builder.comparisonValue;
        this.checkType = builder.checkType;
        this.threshold = builder.threshold;
        this.operator = builder.operator;
        this.failureStrategy = builder.failureStrategy;
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.state = builder.state;
        this.errorDataPath = builder.errorDataPath;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private long processDefinitionId;
        private String  processDefinitionName;
        private long processInstanceId;
        private String processInstanceName;
        private long taskInstanceId;
        private String taskName;
        private int ruleType;
        private String ruleName;
        private double statisticsValue;
        private double comparisonValue;
        private int checkType;
        private double threshold;
        private int operator;
        private int failureStrategy;
        private int userId;
        private String userName;
        private int state;
        private String errorDataPath;

        public Builder processDefinitionId(long processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public Builder processDefinitionName(String processDefinitionName) {
            this.processDefinitionName = processDefinitionName;
            return this;
        }

        public Builder processInstanceId(long processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder processInstanceName(String processInstanceName) {
            this.processInstanceName = processInstanceName;
            return this;
        }

        public Builder taskInstanceId(long taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder ruleType(int ruleType) {
            this.ruleType = ruleType;
            return this;
        }

        public Builder ruleName(String ruleName) {
            this.ruleName = ruleName;
            return this;
        }

        public Builder statisticsValue(double statisticsValue) {
            this.statisticsValue = statisticsValue;
            return this;
        }

        public Builder comparisonValue(double comparisonValue) {
            this.comparisonValue = comparisonValue;
            return this;
        }

        public Builder checkType(int checkType) {
            this.checkType = checkType;
            return this;
        }

        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder operator(int operator) {
            this.operator = operator;
            return this;
        }

        public Builder failureStrategy(int failureStrategy) {
            this.failureStrategy = failureStrategy;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder state(int state) {
            this.state = state;
            return this;
        }

        public Builder errorDataPath(String errorDataPath) {
            this.errorDataPath = errorDataPath;
            return this;
        }

        public DqExecuteResultAlertContent build() {
            return new DqExecuteResultAlertContent(this);
        }
    }
}
