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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Strings;

/**
 * 流程定义实体，映射到 t_ds_process_definition 表，表示工作流的一次版本定义。
 * 包含流程名称、版本号、全局参数、位置信息、超时时间、租户、执行类型等配置信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_process_definition")
public class ProcessDefinition {

    /** 流程定义主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 流程定义编码，全局唯一 */
    private long code;

    /** 流程定义名称 */
    private String name;

    /** 流程定义版本号 */
    private int version;

    /** 发布状态：上线/下线 */
    private ReleaseState releaseState;

    /** 所属项目编码 */
    private long projectCode;

    /** 流程描述 */
    private String description;

    /** 用户自定义全局参数（JSON 格式） */
    private String globalParams;

    /** 非数据库字段：用户自定义全局参数列表 */
    @TableField(exist = false)
    private List<Property> globalParamList;

    /** 非数据库字段：用户自定义全局参数映射 */
    @TableField(exist = false)
    private Map<String, String> globalParamMap;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 流程是否有效：是/否 */
    private Flag flag;

    /** 流程创建用户 ID */
    private int userId;

    /** 非数据库字段：创建用户名 */
    @TableField(exist = false)
    private String userName;

    /** 非数据库字段：项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 节点位置信息（JSON 格式，用于前端展示） */
    private String locations;

    /** 非数据库字段：定时调度发布状态：上线/下线 */
    @TableField(exist = false)
    private ReleaseState scheduleReleaseState;

    /** 流程超时告警时间，单位：分钟 */
    private int timeout;

    /** 租户 ID */
    private int tenantId;

    /** 非数据库字段：租户编码 */
    @TableField(exist = false)
    private String tenantCode;

    /** 非数据库字段：修改用户名 */
    @TableField(exist = false)
    private String modifyBy;

    /** 非数据库字段：告警组 ID */
    @TableField(exist = false)
    private int warningGroupId;

    /** 执行类型 */
    private ProcessExecutionTypeEnum executionType;

    public ProcessDefinition(long projectCode,
                             String name,
                             long code,
                             String description,
                             String globalParams,
                             String locations,
                             int timeout,
                             int userId,
                             int tenantId) {
        set(projectCode, name, description, globalParams, locations, timeout, tenantId);
        this.code = code;
        this.userId = userId;
        Date date = new Date();
        this.createTime = date;
        this.updateTime = date;
    }

    public void set(long projectCode,
                    String name,
                    String description,
                    String globalParams,
                    String locations,
                    int timeout,
                    int tenantId) {
        this.projectCode = projectCode;
        this.name = name;
        this.description = description;
        this.globalParams = globalParams;
        this.locations = locations;
        this.timeout = timeout;
        this.tenantId = tenantId;
        this.flag = Flag.YES;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParamList = JSONUtils.toList(globalParams, Property.class);
        if (this.globalParamList == null) {
            this.globalParamList = new ArrayList<>();
        }
        this.globalParams = globalParams;
    }

    public Map<String, String> getGlobalParamMap() {
        if (globalParamMap == null && !Strings.isNullOrEmpty(globalParams)) {
            List<Property> propList = JSONUtils.toList(globalParams, Property.class);
            globalParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }

        return globalParamMap;
    }

}
