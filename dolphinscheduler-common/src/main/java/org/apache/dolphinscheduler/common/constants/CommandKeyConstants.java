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

package org.apache.dolphinscheduler.common.constants;

/**
 * Command命令参数键常量类。
 * 定义Command实体中commandParam字段的JSON键名，
 * 用于在流程实例恢复、补数、子流程等场景下传递参数。
 *
 * @see org.apache.dolphinscheduler.dao.entity.Command
 */
public class CommandKeyConstants {

    /** 恢复流程实例时使用的流程实例ID键名 */
    public static final String CMD_PARAM_RECOVER_PROCESS_ID_STRING = "ProcessInstanceId";

    /** 恢复流程时指定起始节点ID列表的键名 */
    public static final String CMD_PARAM_RECOVERY_START_NODE_STRING = "StartNodeIdList";

    /** 恢复流程时等待线程ID的键名 */
    public static final String CMD_PARAM_RECOVERY_WAITING_THREAD = "WaitingThreadInstanceId";

    /** 子流程实例ID的键名 */
    public static final String CMD_PARAM_SUB_PROCESS = "processInstanceId";

    /** 空子流程标识（值为"0"表示无子流程） */
    public static final String CMD_PARAM_EMPTY_SUB_PROCESS = "0";

    /** 子流程父流程实例ID的键名 */
    public static final String CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID = "parentProcessInstanceId";

    /** 子流程定义Code的键名 */
    public static final String CMD_PARAM_SUB_PROCESS_DEFINE_CODE = "processDefinitionCode";

    /** 起始节点列表的键名 */
    public static final String CMD_PARAM_START_NODES = "StartNodeList";

    /** 起始参数的键名 */
    public static final String CMD_PARAM_START_PARAMS = "StartParams";

    /** 父参数（用于传递父流程的参数） */
    public static final String CMD_PARAM_FATHER_PARAMS = "fatherParams";

    /** 补数数据的起始日期 */
    public static final String CMD_PARAM_COMPLEMENT_DATA_START_DATE = "complementStartDate";

    /** 补数数据的结束日期 */
    public static final String CMD_PARAM_COMPLEMENT_DATA_END_DATE = "complementEndDate";

    /** 补数数据的调度日期列表 */
    public static final String CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST = "complementScheduleDateList";
}
