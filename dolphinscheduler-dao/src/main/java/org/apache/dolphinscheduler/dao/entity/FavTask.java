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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 收藏任务实体，映射到 t_ds_fav_task 表，记录用户收藏的任务类型。
 * 用于前端快速新建任务时展示用户常用的任务类型列表，提升操作效率。
 */
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@TableName("t_ds_fav_task")
public class FavTask {

    /** 收藏记录主键 ID，自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 任务类型名称，如 "SQL"、"SHELL"、"SPARK" 等 */
    private String taskName;
    /** 用户 ID，对应 t_ds_user 表的 id */
    private int userId;

}
