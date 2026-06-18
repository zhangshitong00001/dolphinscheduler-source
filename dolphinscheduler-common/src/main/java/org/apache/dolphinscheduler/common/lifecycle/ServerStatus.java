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

package org.apache.dolphinscheduler.common.lifecycle;

/**
 * 服务状态枚举，表示DolphinScheduler Master/Worker服务的运行状态。
 * 包含运行中、等待中、已停止三种状态，每种状态有对应的编码和描述。
 */
public enum ServerStatus {

    /** 运行中：服务正常工作 */
    RUNNING(0, "The current server is running"),
    /** 等待中：服务暂时无法处理任务 */
    WAITING(1, "The current server is waiting, this means it cannot work"),
    /** 已停止：服务已终止 */
    STOPPED(2, "The current server is stopped"),
    ;

    private final int code;
    private final String desc;

    ServerStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取状态编码。
     *
     * @return 状态编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态描述。
     *
     * @return 状态描述字符串
     */
    public String getDesc() {
        return desc;
    }
}
