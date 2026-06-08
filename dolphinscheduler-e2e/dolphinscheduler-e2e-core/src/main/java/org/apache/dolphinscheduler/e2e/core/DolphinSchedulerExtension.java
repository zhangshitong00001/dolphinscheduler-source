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

package org.apache.dolphinscheduler.e2e.core;

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;
import static org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat.MP4;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.utility.DockerImageName;

/**
 * DolphinScheduler端到端测试的JUnit 5扩展。负责在测试前启动Docker Compose容器和Selenium浏览器容器，
 * 在测试中注入WebDriver，并在测试后清理所有资源。支持本地模式和Docker模式两种运行方式。
 */
@Slf4j
final class DolphinSchedulerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
    /** 是否为本地运行模式，由系统属性 "local" 控制 */
    private final boolean LOCAL_MODE = Objects.equals(System.getProperty("local"), "true");

    /** 是否在M1芯片上运行，由系统属性 "m1_chip" 控制 */
    private final boolean M1_CHIP_FLAG = Objects.equals(System.getProperty("m1_chip"), "true");

    /** Selenium远程WebDriver实例 */
    private RemoteWebDriver driver;
    /** Docker Compose容器实例 */
    private DockerComposeContainer<?> compose;
    /** Selenium浏览器容器实例 */
    private BrowserWebDriverContainer<?> browser;
    /** Docker网络实例 */
    private Network network;
    /** DolphinScheduler服务的主机地址和端口 */
    private HostAndPort address;
    /** DolphinScheduler UI的根路径 */
    private String rootPath;

    /** 浏览器操作录制文件的输出路径 */
    private Path record;

    /**
     * 在所有测试之前初始化测试环境。
     * 设置超时策略、录制路径，根据模式启动Docker容器或本地环境，初始化浏览器和WebDriver。
     *
     * @param context JUnit扩展上下文
     * @throws IOException 当创建录制目录失败时抛出
     */
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void beforeAll(ExtensionContext context) throws IOException {
        Awaitility.setDefaultTimeout(Duration.ofSeconds(60));
        Awaitility.setDefaultPollInterval(Duration.ofSeconds(10));

        setRecordPath();

        if (LOCAL_MODE) {
            runInLocal();
        } else {
            runInDockerContainer(context);
        }

        setBrowserContainerByOsName();

        if (network != null) {
            browser.withNetwork(network);
        }
        browser.start();

        driver = browser.getWebDriver();

        driver.manage().timeouts()
              .implicitlyWait(5, TimeUnit.SECONDS)
              .pageLoadTimeout(5, TimeUnit.SECONDS);
        driver.manage().window()
              .maximize();

        driver.get(new URL("http", address.getHost(), address.getPort(), rootPath).toString());

        browser.beforeTest(new TestDescription(context));

        final Class<?> clazz = context.getRequiredTestClass();
        Stream.of(clazz.getDeclaredFields())
              .filter(it -> Modifier.isStatic(it.getModifiers()))
              .filter(f -> WebDriver.class.isAssignableFrom(f.getType()))
              .forEach(it -> setDriver(clazz, it));
    }

    /**
     * 配置本地运行模式。暴露宿主机端口并设置地址指向本地。
     */
    private void runInLocal() {
        Testcontainers.exposeHostPorts(3000);
        address = HostAndPort.fromParts("host.testcontainers.internal", 3000);
        rootPath = "/";
    }

    /**
     * 配置Docker容器运行模式。启动Docker Compose并获取DolphinScheduler容器的网络信息。
     *
     * @param context JUnit扩展上下文
     */
    private void runInDockerContainer(ExtensionContext context) {
        compose = createDockerCompose(context);
        compose.start();

        final ContainerState dsContainer = compose.getContainerByServiceName("dolphinscheduler_1")
                .orElseThrow(() -> new RuntimeException("Failed to find a container named 'dolphinscheduler'"));
        final String networkId = dsContainer.getContainerInfo().getNetworkSettings().getNetworks().keySet().iterator().next();
        network = new Network() {
            @Override
            public String getId() {
                return networkId;
            }

            @Override
            public void close() {
            }

            @Override
            public Statement apply(Statement base, Description description) {
                return null;
            }
        };
        address = HostAndPort.fromParts("dolphinscheduler", 12345);
        rootPath = "/dolphinscheduler/ui/";
    }

    /**
     * 根据操作系统和芯片类型配置浏览器容器。
     * M1芯片本地模式使用seleniarm镜像，其他模式使用标准selenium镜像。
     */
    private void setBrowserContainerByOsName() {
        DockerImageName imageName;

        if (LOCAL_MODE && M1_CHIP_FLAG) {
            imageName = DockerImageName.parse("seleniarm/standalone-chromium:4.1.2-20220227")
                    .asCompatibleSubstituteFor("selenium/standalone-chrome");

            browser = new BrowserWebDriverContainer<>(imageName)
                    .withCapabilities(new ChromeOptions())
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                    .withFileSystemBind(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(),
                            Constants.SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH);
        } else {
            browser = new BrowserWebDriverContainer<>()
                    .withCapabilities(new ChromeOptions())
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                    .withFileSystemBind(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(),
                            Constants.SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH)
                    .withRecordingMode(RECORD_ALL, record.toFile(), MP4);
        }
    }

    /**
     * 设置浏览器操作录制文件的输出路径。优先使用环境变量 {@code RECORDING_PATH}，否则创建临时目录。
     *
     * @throws IOException 当创建录制目录失败时抛出
     */
    private void setRecordPath() throws IOException {
        if (!Strings.isNullOrEmpty(System.getenv("RECORDING_PATH"))) {
            record = Paths.get(System.getenv("RECORDING_PATH"));
            if (!record.toFile().exists()) {
                if (!record.toFile().mkdir()) {
                    throw new IOException("Failed to create recording directory: " + record.toAbsolutePath());
                }
            }
        } else {
            record = Files.createTempDirectory("record-");
        }
    }

    /**
     * 在所有测试之后清理测试环境。
     * 停止浏览器录制、关闭浏览器容器，并销毁Docker Compose容器。
     *
     * @param context JUnit扩展上下文
     */
    @Override
    public void afterAll(ExtensionContext context) {
        browser.afterTest(new TestDescription(context), Optional.empty());
        browser.stop();
        if (compose != null) {
            compose.stop();
        }
    }

    /**
     * 在每个测试方法执行前，将WebDriver注入到测试实例中的 {@link WebDriver} 类型字段。
     *
     * @param context JUnit扩展上下文
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        final Object instance = context.getRequiredTestInstance();
        Stream.of(instance.getClass().getDeclaredFields())
              .filter(f -> WebDriver.class.isAssignableFrom(f.getType()))
              .forEach(it -> setDriver(instance, it));
    }

    /**
     * 通过反射将WebDriver实例设置到目标对象的指定字段中。
     *
     * @param object 目标对象实例
     * @param field  目标字段
     */
    private void setDriver(Object object, Field field) {
        try {
            field.setAccessible(true);
            field.set(object, driver);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to inject web driver to field: {}", field.getName(), e);
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
            .withLogConsumer("dolphinscheduler_1", outputFrame -> LOGGER.info(outputFrame.getUtf8String()))
            .waitingFor("dolphinscheduler_1", Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(180)));

        return compose;
    }
}
