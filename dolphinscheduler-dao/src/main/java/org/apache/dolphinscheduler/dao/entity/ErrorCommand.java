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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 错误命令实体，映射到 t_ds_error_command 表，存储执行失败或出错的命令记录。
 * 当 Master 处理命令过程中发生异常时，会将原始命令信息连同错误信息移入此表，用于故障排查和重试。
 */
@Data
@TableName("t_ds_error_command")
public class ErrorCommand {

    /** 错误命令 ID，直接使用原始 Command 的 id，类型为 INPUT（手动输入，非自增） */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /** 命令类型，与原始命令一致 */
    private CommandType commandType;

    /** 关联的工作流定义编码 */
    private long processDefinitionCode;

    /** 执行者用户 ID */
    private int executorId;

    /** 命令参数，JSON 格式 */
    private String commandParam;

    /** 任务依赖类型 */
    private TaskDependType taskDependType;

    /** 失败策略 */
    private FailureStrategy failureStrategy;

    /** 告警通知类型 */
    private WarningType warningType;

    /** 告警组 ID */
    private Integer warningGroupId;

    /** 计划调度时间 */
    private Date scheduleTime;

    /** 命令开始时间 */
    private Date startTime;

    /** 工作流实例优先级 */
    private Priority processInstancePriority;

    /** 最后更新时间 */
    private Date updateTime;

    /** 错误信息，记录命令执行失败的原因和堆栈信息 */
    private String message;

    /** 工作组名称 */
    private String workerGroup;

    /** 环境编码 */
    private Long environmentCode;

    /** 试运行标志 */
    private int dryRun;

    public ErrorCommand() {
    }

    public ErrorCommand(Command command, String message) {
        this.id = command.getId();
        this.commandType = command.getCommandType();
        this.executorId = command.getExecutorId();
        this.processDefinitionCode = command.getProcessDefinitionCode();
        this.commandParam = command.getCommandParam();
        this.warningType = command.getWarningType();
        this.warningGroupId = command.getWarningGroupId();
        this.scheduleTime = command.getScheduleTime();
        this.taskDependType = command.getTaskDependType();
        this.failureStrategy = command.getFailureStrategy();
        this.startTime = command.getStartTime();
        this.updateTime = command.getUpdateTime();
        this.environmentCode = command.getEnvironmentCode();
        this.processInstancePriority = command.getProcessInstancePriority();
        this.message = message;
        this.dryRun = command.getDryRun();
    }
}
