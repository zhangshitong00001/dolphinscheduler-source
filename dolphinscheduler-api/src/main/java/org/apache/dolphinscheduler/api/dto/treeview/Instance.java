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

package org.apache.dolphinscheduler.api.dto.treeview;

import java.util.Date;

import lombok.Data;

/**
 * 树形视图节点实例DTO。表示工作流树形图中的一个节点实例，包含节点的运行状态、时间和主机信息。
 */
@Data
public class Instance {

    /** 节点ID */
    private Integer id;

    /** 节点名称 */
    private String name;

    /** 节点编码 */
    private long code;

    /** 节点类型 */
    private String type;

    /** 节点状态 */
    private String state;

    /** 节点开始时间 */
    private Date startTime;

    /** 节点结束时间 */
    private Date endTime;

    /** 节点运行所在的主机 */
    private String host;

    /** 节点运行持续时间 */
    private String duration;

    /** 子流程编码 */
    private long subflowCode;

    public Instance() {
    }

    public Instance(int id, String name, long code, String type) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public Instance(int id, String name, long code, String type, String state, Date startTime, Date endTime,
                    String host, String duration, long subflowCode) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.host = host;
        this.duration = duration;
        this.subflowCode = subflowCode;
    }

    public Instance(int id, String name, long code, String type, String state, Date startTime, Date endTime,
                    String host, String duration) {
        this(id, name, code, type, state, startTime, endTime, host, duration, 0);
    }
}
