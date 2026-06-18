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

import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 告警组实体，映射到 t_ds_alertgroup 表，表示一个告警通知分组。
 * 告警组将多个告警插件实例组合在一起，当工作流或任务触发告警时，会向告警组中的所有插件实例发送通知。
 */
@Data
@TableName("t_ds_alertgroup")
public class AlertGroup {

    /** 告警组主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 告警组名称 */
    @TableField(value = "group_name")
    private String groupName;

    /** 关联的告警插件实例 ID 列表，以逗号分隔，如 "1,2,3" */
    @TableField(value = "alert_instance_ids")
    private String alertInstanceIds;

    /** 告警组描述信息 */
    @TableField(value = "description")
    private String description;
    /** 创建时间 */
    @TableField(value = "create_time")
    private Date createTime;
    /** 最后更新时间 */
    @TableField(value = "update_time")
    private Date updateTime;

    /** 创建者用户 ID，对应 t_ds_user 表的 id */
    @TableField(value = "create_user_id")
    private int createUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AlertGroup that = (AlertGroup) o;

        if (id != that.id) {
            return false;
        }
        if (createUserId != that.createUserId) {
            return false;
        }
        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) {
            return false;
        }
        if (alertInstanceIds != null ? !alertInstanceIds.equals(that.alertInstanceIds)
                : that.alertInstanceIds != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        return !(createTime != null ? !createTime.equals(that.createTime) : that.createTime != null)
                && !(updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + createUserId;
        result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
        result = 31 * result + (alertInstanceIds != null ? alertInstanceIds.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        return result;
    }
}
