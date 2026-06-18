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
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 命令实体，映射到 t_ds_command 表，表示一条待执行的工作流操作命令。
 * 命令由 API 层（Master）创建并写入数据库，由 Master 服务轮询消费执行。支持的命令类型包括：
 * 启动工作流、重跑、暂停、停止、恢复、补数等。命令包含工作流执行所需的所有参数。
 */
@Data
@TableName("t_ds_command")
public class Command {

    /** 命令主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 命令类型，枚举值：START_PROCESS（启动）、RECOVER_PROCESS（恢复）、START_FAILURE_TASK_PROCESS（失败重跑）、COMPLEMENT_DATA（补数）、SCHEDULER（调度）、REPEAT_RUNNING（重复运行）、PAUSE（暂停）、STOP（停止）、RECOVER_WAITING_THREAD（恢复等待线程） */
    @TableField("command_type")
    private CommandType commandType;

    /** 关联的工作流定义编码，对应 t_ds_process_definition 表的 code */
    @TableField("process_definition_code")
    private long processDefinitionCode;

    /** 执行者用户 ID */
    @TableField("executor_id")
    private int executorId;

    /** 命令参数，JSON 格式，包含启动参数、补数日期范围等具体执行参数 */
    @TableField("command_param")
    private String commandParam;

    /** 任务依赖类型，枚举值：TASK_ONLY（仅任务）、TASK_POST（任务后继）、TASK_PRE（任务前置） */
    @TableField("task_depend_type")
    private TaskDependType taskDependType;

    /** 失败策略，枚举值：CONTINUE（继续）、END（结束） */
    @TableField("failure_strategy")
    private FailureStrategy failureStrategy;

    /** 告警通知类型，枚举值：ALL（全部）、SUCCESS（成功时）、FAILURE（失败时）、TIMEOUT（超时时） */
    @TableField("warning_type")
    private WarningType warningType;

    /** 告警组 ID，对应 t_ds_alertgroup 表的 id */
    @TableField("warning_group_id")
    private Integer warningGroupId;

    /** 计划调度时间，命令预期执行的时间 */
    @TableField("schedule_time")
    private Date scheduleTime;

    /** 命令创建时间（开始时间） */
    @TableField("start_time")
    private Date startTime;

    /** 工作流实例优先级，枚举值：HIGHEST、HIGH、MEDIUM、LOW、LOWEST */
    @TableField("process_instance_priority")
    private Priority processInstancePriority;

    /** 最后更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 工作组的名称，用于指定任务在哪个工作组执行 */
    @TableField("worker_group")
    private String workerGroup;

    /** 环境编码，对应 t_ds_environment 表的 code，用于指定任务运行环境 */
    @TableField("environment_code")
    private Long environmentCode;

    /** 试运行标志，0 表示正常执行，1 表示试运行（dry run，不实际执行任务） */
    @TableField("dry_run")
    private int dryRun;

    /** 关联的工作流实例 ID，对应 t_ds_process_instance 表的 id */
    @TableField("process_instance_id")
    private int processInstanceId;

    /** 工作流定义版本号，指定使用的定义版本 */
    @TableField("process_definition_version")
    private int processDefinitionVersion;

    public Command() {
        this.taskDependType = TaskDependType.TASK_POST;
        this.failureStrategy = FailureStrategy.CONTINUE;
        this.startTime = new Date();
        this.updateTime = new Date();
    }

    public Command(
                   CommandType commandType,
                   TaskDependType taskDependType,
                   FailureStrategy failureStrategy,
                   int executorId,
                   long processDefinitionCode,
                   String commandParam,
                   WarningType warningType,
                   int warningGroupId,
                   Date scheduleTime,
                   String workerGroup,
                   Long environmentCode,
                   Priority processInstancePriority,
                   int dryRun,
                   int processInstanceId,
                   int processDefinitionVersion) {
        this.commandType = commandType;
        this.executorId = executorId;
        this.processDefinitionCode = processDefinitionCode;
        this.commandParam = commandParam;
        this.warningType = warningType;
        this.warningGroupId = warningGroupId;
        this.scheduleTime = scheduleTime;
        this.taskDependType = taskDependType;
        this.failureStrategy = failureStrategy;
        this.startTime = new Date();
        this.updateTime = new Date();
        this.workerGroup = workerGroup;
        this.environmentCode = environmentCode;
        this.processInstancePriority = processInstancePriority;
        this.dryRun = dryRun;
        this.processInstanceId = processInstanceId;
        this.processDefinitionVersion = processDefinitionVersion;
    }

}
