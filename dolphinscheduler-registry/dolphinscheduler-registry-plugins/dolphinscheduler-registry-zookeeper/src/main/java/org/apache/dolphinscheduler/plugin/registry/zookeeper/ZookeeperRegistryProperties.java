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



package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ZooKeeper 注册中心配置属性类。
 * 当配置 registry.type=zookeeper 时自动生效，绑定 application.yml 中 registry 前缀下的 ZooKeeper 相关配置。
 * 包含连接参数、会话超时、重试策略等配置项。
 */
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "zookeeper")
@ConfigurationProperties(prefix = "registry")
public class ZookeeperRegistryProperties {

    /** ZooKeeper 连接与会话相关配置 */
    private ZookeeperProperties zookeeper = new ZookeeperProperties();

    public ZookeeperProperties getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(ZookeeperProperties zookeeper) {
        this.zookeeper = zookeeper;
    }

    /**
     * ZooKeeper 配置属性内部类，包含连接地址、命名空间、会话超时、重试策略、认证等配置。
     */
    public static final class ZookeeperProperties {

        /** ZooKeeper 命名空间，用于隔离不同应用的数据 */
        private String namespace;

        /** ZooKeeper 集群连接地址，格式: host1:port1,host2:port2 */
        private String connectString;

        /** 重试策略配置 */
        private RetryPolicy retryPolicy = new RetryPolicy();

        /** 认证摘要信息，用于 ZooKeeper ACL 访问控制 */
        private String digest;

        /** 会话超时时间，默认 30 秒 */
        private Duration sessionTimeout = Duration.ofSeconds(30);

        /** 连接超时时间，默认 120 秒 */
        private Duration connectionTimeout = Duration.ofSeconds(120);

        /** 启动时阻塞等待连接建立的最大时间，默认 2 秒 */
        private Duration blockUntilConnected = Duration.ofMillis(2000);

        /** 连接最大等待时间，默认 60 秒 */
        private Duration maxWaitTime = Duration.ofSeconds(60);

        /** 连接重试次数，默认 3 次 */
        private int connectionRetryCount = 3;

        /** 重试间隔时间，默认 2 秒 */
        private Duration retryInterval = Duration.ofSeconds(2);

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getConnectString() {
            return connectString;
        }

        public void setConnectString(String connectString) {
            this.connectString = connectString;
        }

        public RetryPolicy getRetryPolicy() {
            return retryPolicy;
        }

        public void setRetryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public Duration getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(Duration sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public Duration getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Duration getBlockUntilConnected() {
            return blockUntilConnected;
        }

        public void setBlockUntilConnected(Duration blockUntilConnected) {
            this.blockUntilConnected = blockUntilConnected;
        }

        public Duration getMaxWaitTime() {
            return maxWaitTime;
        }

        public void setMaxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
        }

        public int getConnectionRetryCount() {
            return connectionRetryCount;
        }

        public void setConnectionRetryCount(int connectionRetryCount) {
            this.connectionRetryCount = connectionRetryCount;
        }

        public Duration getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(Duration retryInterval) {
            this.retryInterval = retryInterval;
        }

        /**
         * ZooKeeper 重试策略配置，使用指数退避算法。
         */
        public static final class RetryPolicy {

            /** 初始休眠时间，默认 60 毫秒 */
            private Duration baseSleepTime = Duration.ofMillis(60);

            /** 最大重试次数 */
            private int maxRetries;

            /** 最大休眠时间上限，默认 300 毫秒 */
            private Duration maxSleep = Duration.ofMillis(300);

            public Duration getBaseSleepTime() {
                return baseSleepTime;
            }

            public void setBaseSleepTime(Duration baseSleepTime) {
                this.baseSleepTime = baseSleepTime;
            }

            public int getMaxRetries() {
                return maxRetries;
            }

            public void setMaxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
            }

            public Duration getMaxSleep() {
                return maxSleep;
            }

            public void setMaxSleep(Duration maxSleep) {
                this.maxSleep = maxSleep;
            }
        }
    }

}
