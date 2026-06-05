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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 流程实例映射关系实体，映射到 t_ds_relation_process_instance 表，表示父子流程实例之间的依赖关系。
 * 用于记录子流程的父流程实例 ID 和父任务实例 ID。
 */
@Data
@TableName("t_ds_relation_process_instance")
public class ProcessInstanceMap {

    /** 主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 父流程实例 ID */
    private int parentProcessInstanceId;

    /** 父任务实例 ID */
    private int parentTaskInstanceId;

    /** 子流程实例 ID */
    private int processInstanceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcessInstanceMap that = (ProcessInstanceMap) o;

        if (id != that.id) {
            return false;
        }
        if (parentProcessInstanceId != that.parentProcessInstanceId) {
            return false;
        }
        if (parentTaskInstanceId != that.parentTaskInstanceId) {
            return false;
        }
        return processInstanceId == that.processInstanceId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + parentProcessInstanceId;
        result = 31 * result + parentTaskInstanceId;
        result = 31 * result + processInstanceId;
        return result;
    }
}
