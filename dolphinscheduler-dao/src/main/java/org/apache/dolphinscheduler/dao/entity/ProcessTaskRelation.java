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

import org.apache.dolphinscheduler.common.enums.ConditionType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 流程任务关系实体，映射到 t_ds_process_task_relation 表，表示工作流中两个任务节点之间的连线关系。
 * 记录上游节点与下游节点的连接信息以及条件分支参数。
 */
@Data
@NoArgsConstructor
@TableName("t_ds_process_task_relation")
public class ProcessTaskRelation {

    /** 关系主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关系名称 */
    private String name;

    /** 流程定义版本号 */
    private int processDefinitionVersion;

    /** 项目编码 */
    private long projectCode;

    /** 流程定义编码 */
    private long processDefinitionCode;

    /** 上游任务编码 */
    private long preTaskCode;

    /** 上游节点版本号 */
    private int preTaskVersion;

    /** 下游任务编码 */
    private long postTaskCode;

    /** 下游节点版本号 */
    private int postTaskVersion;

    /** 条件类型 */
    private ConditionType conditionType;

    /** 条件参数（JSON 格式） */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String conditionParams;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public ProcessTaskRelation(String name,
                               int processDefinitionVersion,
                               long projectCode,
                               long processDefinitionCode,
                               long preTaskCode,
                               int preTaskVersion,
                               long postTaskCode,
                               int postTaskVersion,
                               ConditionType conditionType,
                               String conditionParams,
                               Date createTime,
                               Date updateTime) {
        this.name = name;
        this.processDefinitionVersion = processDefinitionVersion;
        this.projectCode = projectCode;
        this.processDefinitionCode = processDefinitionCode;
        this.preTaskCode = preTaskCode;
        this.preTaskVersion = preTaskVersion;
        this.postTaskCode = postTaskCode;
        this.postTaskVersion = postTaskVersion;
        this.conditionType = conditionType;
        this.conditionParams = conditionParams;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

}
