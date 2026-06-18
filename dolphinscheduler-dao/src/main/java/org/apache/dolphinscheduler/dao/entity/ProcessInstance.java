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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Strings;

/**
 * 流程实例实体，映射到 t_ds_process_instance 表，表示工作流的一次具体执行。
 * 记录流程的运行状态、开始结束时间、执行主机、命令参数、全局参数、告警配置等运行上下文信息。
 */
@NoArgsConstructor
@Data
@TableName("t_ds_process_instance")
public class ProcessInstance {

    /** 流程实例主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 流程定义编码 */
    private Long processDefinitionCode;

    /** 流程定义版本号 */
    private int processDefinitionVersion;

    /** 流程执行状态 */
    private WorkflowExecutionStatus state;

    /** 状态变更历史（JSON 格式） */
    private String stateHistory;

    /** 非数据库字段：从状态历史解析出的状态描述列表 */
    @TableField(exist = false)
    private List<StateDesc> stateDescList;

    /** 容错标识，用于故障转移恢复 */
    private Flag recovery;
    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date endTime;

    /** 已运行次数 */
    private int runTimes;

    /** 流程实例名称 */
    private String name;

    /** 执行主机 */
    private String host;

    /** 非数据库字段：关联的流程定义结构 */
    @TableField(exist = false)
    private ProcessDefinition processDefinition;
    /** 流程命令类型 */
    private CommandType commandType;

    /** 命令参数（JSON 格式） */
    private String commandParam;

    /** 节点依赖类型 */
    private TaskDependType taskDependType;

    /** 任务最大重试次数 */
    private int maxTryTimes;

    /** 任务失败时的失败策略 */
    private FailureStrategy failureStrategy;

    /** 告警类型 */
    private WarningType warningType;

    /** 告警组 ID */
    private Integer warningGroupId;

    /** 调度时间 */
    private Date scheduleTime;

    /** 命令开始时间 */
    private Date commandStartTime;

    /** 用户自定义全局参数字符串（JSON 格式） */
    private String globalParams;

    /** 非数据库字段：DAG 数据 */
    @TableField(exist = false)
    private DagData dagData;

    /** 执行者用户 ID */
    private int executorId;

    /** 非数据库字段：执行者用户名 */
    @TableField(exist = false)
    private String executorName;

    /** 非数据库字段：租户编码 */
    @TableField(exist = false)
    private String tenantCode;

    /** 非数据库字段：队列名称 */
    @TableField(exist = false)
    private String queue;

    /** 是否为子流程 */
    private Flag isSubProcess;

    /** 非数据库字段：任务节点位置信息（JSON 格式，用于前端展示） */
    @TableField(exist = false)
    private String locations;

    /** 历史命令记录 */
    private String historyCmd;

    /** 非数据库字段：依赖流程的调度时间 */
    @TableField(exist = false)
    private String dependenceScheduleTimes;

    /** 非数据库字段：流程运行时长 */
    @TableField(exist = false)
    private String duration;

    /** 流程实例优先级 */
    private Priority processInstancePriority;

    /** Worker 分组 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;

    /** 流程超时告警时间，单位：分钟 */
    private int timeout;

    /** 租户 ID */
    private int tenantId;

    /** 变量池（JSON 格式） */
    private String varPool;
    /** 串行队列中的下一个流程实例 ID */
    private int nextProcessInstanceId;

    /** 空跑标识 */
    private int dryRun;

    /** 重启时间 */
    private Date restartTime;

    /** 非数据库字段：流程是否被阻塞 */
    @TableField(exist = false)
    private boolean isBlocked;

    /**
     * set the process name with process define version and timestamp
     *
     * @param processDefinition processDefinition
     */
    public ProcessInstance(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        // todo: the name is not unique
        this.name = String.join("-",
                processDefinition.getName(),
                String.valueOf(processDefinition.getVersion()),
                DateUtils.getCurrentTimeStamp());
    }

    /**
     * add command to history
     *
     * @param cmd cmd
     */
    public void addHistoryCmd(CommandType cmd) {
        if (!Strings.isNullOrEmpty(this.historyCmd)) {
            this.historyCmd = String.format("%s,%s", this.historyCmd, cmd.toString());
        } else {
            this.historyCmd = cmd.toString();
        }
    }

    /**
     * check this process is start complement data
     *
     * @return whether complement data
     */
    public boolean isComplementData() {
        if (Strings.isNullOrEmpty(this.historyCmd)) {
            return false;
        }
        return historyCmd.startsWith(CommandType.COMPLEMENT_DATA.toString());
    }

    /**
     * get current command type,
     * if start with complement data,return complement
     *
     * @return CommandType
     */
    public CommandType getCmdTypeIfComplement() {
        if (isComplementData()) {
            return CommandType.COMPLEMENT_DATA;
        }
        return commandType;
    }

    /**
     * set state with desc
     * @param state
     * @param stateDesc
     */
    public void setStateWithDesc(WorkflowExecutionStatus state, String stateDesc) {
        this.setState(state);
        if (StringUtils.isEmpty(this.getStateHistory())) {
            stateDescList = new ArrayList<>();
        } else if (stateDescList == null) {
            stateDescList = JSONUtils.toList(this.getStateHistory(), StateDesc.class);
        }
        stateDescList.add(new StateDesc(new Date(), state, stateDesc));
        this.setStateHistory(JSONUtils.toJsonString(stateDescList));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateDesc {

        Date time;
        WorkflowExecutionStatus state;
        String desc;
    }
}
