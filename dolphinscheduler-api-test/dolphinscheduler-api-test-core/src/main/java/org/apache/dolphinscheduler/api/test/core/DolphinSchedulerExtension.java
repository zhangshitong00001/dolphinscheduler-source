/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.core;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;

/**
 * DolphinScheduler API测试的JUnit 5扩展。在本地模式下直接运行测试，在Docker模式下通过Docker Compose启动服务容器，
 * 并在测试完成后自动清理容器资源。
 */
@Slf4j
final class DolphinSchedulerExtension implements BeforeAllCallback, AfterAllCallback {
    /** 是否为本地模式，由系统属性 "local" 控制 */
    private final boolean localMode = Objects.equals(System.getProperty("local"), "true");

    /** Docker Compose中DolphinScheduler服务的名称 */
    private final String serviceName = "dolphinscheduler_1";

    /** Docker Compose容器实例 */
    private DockerComposeContainer<?> compose;

    /**
     * 在测试类执行前初始化测试环境。
     * 非本地模式下，根据注解配置创建并启动Docker Compose容器。
     *
     * @param context JUnit扩展上下文
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        if (!localMode) {
            compose = createDockerCompose(context);
            compose.start();
        }
    }

    /**
     * 在测试类执行后清理测试环境。
     * 停止并销毁Docker Compose容器（如果已创建）。
     *
     * @param context JUnit扩展上下文
     */
    @Override
    public void afterAll(ExtensionContext context) {
        if (compose != null) {
            compose.stop();
        }
    }

    /**
     * 根据测试类上的 @DolphinScheduler 注解配置，创建对应的Docker Compose容器实例。
     *
     * @param context JUnit扩展上下文，用于获取测试类和注解信息
     * @return 配置好的DockerComposeContainer实例
     */
    private DockerComposeContainer<?> createDockerCompose(ExtensionContext context) {
        final Class<?> clazz = context.getRequiredTestClass();
        final DolphinScheduler annotation = clazz.getAnnotation(DolphinScheduler.class);
        final List<File> files = Stream.of(annotation.composeFiles())
            .map(it -> DolphinScheduler.class.getClassLoader().getResource(it))
            .filter(Objects::nonNull)
            .map(URL::getPath)
            .map(File::new)
            .collect(Collectors.toList());

        compose = new DockerComposeContainer<>(files)
            .withPull(true)
            .withTailChildContainers(true)
            .withLogConsumer(serviceName, outputFrame -> LOGGER.info(outputFrame.getUtf8String()))
            .waitingFor(serviceName, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(Constants.DOCKER_COMPOSE_DEFAULT_TIMEOUT)));

        return compose;
    }
}
