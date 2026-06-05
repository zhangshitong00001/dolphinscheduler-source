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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * 执行状态统计实体，非数据库表映射，用于按执行状态统计任务或工作流实例的数量。
 * 通常用于 Dashboard 展示各状态（成功、失败、运行中等）的实例分布情况。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteStatusCount {

    /** 任务执行状态枚举值 */
    private TaskExecutionStatus state;

    /** 该状态的实例数量 */
    private int count;
}
