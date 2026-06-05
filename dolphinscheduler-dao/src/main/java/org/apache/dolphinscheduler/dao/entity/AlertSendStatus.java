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

import org.apache.dolphinscheduler.common.enums.AlertStatus;

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Objects;

/**
 * 告警发送状态实体，映射到 t_ds_alert_send_status 表，记录告警向各个插件实例发送的结果状态。
 * 一条告警可能同时发送给多个插件实例（如邮件和钉钉），每个插件实例的发送结果独立记录，用于跟踪告警送达情况。
 */
@Data
@TableName("t_ds_alert_send_status")
public class AlertSendStatus {

    /** 发送状态记录主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关联的告警 ID，对应 t_ds_alert 表的 id */
    @TableField(value = "alert_id")
    private int alertId;

    /** 关联的告警插件实例 ID，对应 t_ds_alert_plugin_instance 表的 id */
    @TableField(value = "alert_plugin_instance_id")
    private int alertPluginInstanceId;

    /** 告警发送状态，枚举值：WAIT_EXECUTION（等待发送）、EXECUTION_SUCCESS（发送成功）、EXECUTION_FAILURE（发送失败） */
    @TableField(value = "send_status")
    private AlertStatus sendStatus;

    /** 发送日志，记录发送过程中的详细信息和错误原因 */
    @TableField(value = "log")
    private String log;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlertSendStatus that = (AlertSendStatus) o;
        return alertId == that.alertId && alertPluginInstanceId == that.alertPluginInstanceId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(alertId, alertPluginInstanceId);
    }
}
