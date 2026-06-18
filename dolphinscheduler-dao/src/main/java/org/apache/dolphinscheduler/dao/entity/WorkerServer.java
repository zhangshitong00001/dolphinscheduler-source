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

/**
 * Worker 服务器信息，表示一台 Worker 节点的运行时状态。
 */
@Data
public class WorkerServer {

    /** Worker 服务器 ID */
    private int id;

    /** Worker 主机地址 */
    private String host;

    /** Worker 端口 */
    private int port;

    /** ZooKeeper 目录 */
    private String zkDirectory;

    /** 资源信息 */
    private String resInfo;

    /** 创建时间 */
    private Date createTime;

    /** 最后心跳时间 */
    private Date lastHeartbeatTime;
}
