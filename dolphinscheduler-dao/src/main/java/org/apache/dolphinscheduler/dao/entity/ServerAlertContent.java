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

import org.apache.dolphinscheduler.common.enums.AlertEvent;
import org.apache.dolphinscheduler.common.enums.AlertWarnLevel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 服务器告警内容，封装 Master 或 Worker 节点的告警信息。
 */
public class ServerAlertContent {

    /** 服务器类型：master 或 worker */
    @JsonProperty("type")
    final String type;
    /** 服务器主机地址 */
    @JsonProperty("host")
    final String host;
    /** 告警事件类型 */
    @JsonProperty("event")
    final AlertEvent event;
    /** 告警警告级别 */
    @JsonProperty("warningLevel")
    final AlertWarnLevel warningLevel;

    private ServerAlertContent(Builder builder) {
        this.type = builder.type;
        this.host = builder.host;
        this.event = builder.event;
        this.warningLevel = builder.warningLevel;

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String type;

        private String host;

        private AlertEvent event;

        private AlertWarnLevel warningLevel;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder event(AlertEvent event) {
            this.event = event;
            return this;
        }

        public Builder warningLevel(AlertWarnLevel warningLevel) {
            this.warningLevel = warningLevel;
            return this;
        }

        public ServerAlertContent build() {
            return new ServerAlertContent(this);
        }
    }

}
