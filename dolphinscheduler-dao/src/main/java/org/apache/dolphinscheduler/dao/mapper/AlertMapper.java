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

import org.apache.dolphinscheduler.dao.entity.Alert;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 告警 Mapper 接口，封装对 t_ds_alert 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供告警记录的基本 CRUD 及服务崩溃告警插入能力。
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    /**
     * 插入服务崩溃告警记录。
     * 该方法会确保数据库中最多只有一条相同内容且未发送的告警记录，
     * 通过先删除同内容未发送的旧记录再插入新记录来实现告警抑制。
     *
     * @param alert 告警实体
     * @param crashAlarmSuppressionStartTime 崩溃告警抑制开始时间，此时间之前的同内容告警将被抑制
     */
    void insertAlertWhenServerCrash(@Param("alert") Alert alert, @Param("crashAlarmSuppressionStartTime") Date crashAlarmSuppressionStartTime);

}
