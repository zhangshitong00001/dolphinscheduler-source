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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.AlertSendStatus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 告警发送状态 Mapper 接口，封装对 t_ds_alert_send_status 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供告警发送状态记录的基本 CRUD 能力。
 * 当前仅使用 MyBatis-Plus 内置方法，无自定义 SQL。
 */
public interface AlertSendStatusMapper extends BaseMapper<AlertSendStatus> {
}
