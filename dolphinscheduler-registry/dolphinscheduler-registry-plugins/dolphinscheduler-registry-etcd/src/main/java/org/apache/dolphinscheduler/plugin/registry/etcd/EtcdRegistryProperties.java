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

package org.apache.dolphinscheduler.plugin.registry.etcd;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Etcd 注册中心配置属性类。
 * 当配置 registry.type=etcd 时自动生效，绑定 application.yml 中 registry 前缀下的 Etcd 相关配置。
 * 包含 Etcd 集群连接端点、命名空间、认证信息、重试策略和负载均衡策略等配置项。
 * 使用 Lombok @Data 自动生成 getter/setter 方法。
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
@ConfigurationProperties(prefix = "registry")
public class EtcdRegistryProperties {

    /** Etcd 集群连接端点，格式为 host1:port1,host2:port2 */
    private String endpoints;

    /** 命名空间，用于隔离不同应用的数据，默认为 dolphinscheduler */
    private String namespace = "dolphinscheduler";

    /** 连接超时时间，默认 9 秒 */
    private Duration connectionTimeout = Duration.ofSeconds(9);

    // ========== 认证相关配置 ==========

    /** 用户名，用于 Etcd 认证 */
    private String user;

    /** 密码，用于 Etcd 认证 */
    private String password;

    /** 认证授权机构 */
    private String authority;

    // ========== 重试策略配置 ==========

    /** 重试初始延迟时间，默认 60 毫秒 */
    private Duration retryDelay = Duration.ofMillis(60);

    /** 重试最大延迟时间，默认 300 毫秒 */
    private Duration retryMaxDelay = Duration.ofMillis(300);

    /** 重试最大持续时间，默认 1500 毫秒。超过此时间后不再重试 */
    private Duration retryMaxDuration = Duration.ofMillis(1500);

    // ========== 负载均衡策略 ==========

    /** 负载均衡策略，如 round_robin 等 */
    private String loadBalancerPolicy;
}
