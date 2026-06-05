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

package org.apache.dolphinscheduler.server.master.config;

import static org.apache.dolphinscheduler.common.constants.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;

import lombok.Data;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.ConnectStrategyProperties;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostSelector;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Master 服务配置类。绑定 application.yml 中以 "master" 为前缀的配置项，并提供参数校验。
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "master")
public class MasterConfig implements Validator {

    private Logger logger = LoggerFactory.getLogger(MasterConfig.class);

    /**
     * Master RPC 服务监听端口。
     */
    private int listenPort = 5678;
    /**
     * 从数据库批量拉取命令的最大数量。
     */
    private int fetchCommandNum = 10;
    /**
     * 用于准备流程实例的线程数，该值不应大于 fetchCommandNum。
     */
    private int preExecThreads = 10;
    /**
     * 用于处理流程实例和任务事件的线程数。
     * 将创建两个线程池分别执行 {@link WorkflowExecuteRunnable} 和 {@link TaskExecuteRunnable}。
     */
    private int execThreads = 10;
    /**
     * 任务分发线程池大小。
     */
    private int dispatchTaskNumber = 3;
    /**
     * Worker 选择策略。
     */
    private HostSelector hostSelector = HostSelector.LOWER_WEIGHT;
    /**
     * Master 心跳任务执行间隔。
     */
    private Duration heartbeatInterval = Duration.ofSeconds(10);
    /**
     * 任务提交最大重试次数。
     */
    private int taskCommitRetryTimes = 5;
    /**
     * 任务提交重试间隔。
     */
    private Duration taskCommitInterval = Duration.ofSeconds(1);
    /**
     * 状态轮询检查间隔，值越大可能增大任务/流程实例的延迟。
     */
    private Duration stateWheelInterval = Duration.ofMillis(5);
    /** CPU 最大负载平均值。 */
    private double maxCpuLoadAvg = -1;
    /** 预留内存比例。 */
    private double reservedMemory = 0.3;
    /** 故障转移间隔。 */
    private Duration failoverInterval = Duration.ofMinutes(10);
    /** 任务故障转移时是否终止 Yarn 作业。 */
    private boolean killYarnJobWhenTaskFailover = true;
    /** 注册中心断开连接策略。 */
    private ConnectStrategyProperties registryDisconnectStrategy = new ConnectStrategyProperties();

    /** Worker 分组刷新间隔。 */
    private Duration workerGroupRefreshInterval = Duration.ofSeconds(10L);

    // ip:listenPort
    /** Master 地址（格式：ip:listenPort）。 */
    private String masterAddress;

    // /nodes/master/ip:listenPort
    /** Master 注册中心路径（格式：/nodes/master/ip:listenPort）。 */
    private String masterRegistryPath;

    @Override
    public boolean supports(Class<?> clazz) {
        return MasterConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MasterConfig masterConfig = (MasterConfig) target;
        if (masterConfig.getListenPort() <= 0) {
            errors.rejectValue("listen-port", null, "is invalidated");
        }
        if (masterConfig.getFetchCommandNum() <= 0) {
            errors.rejectValue("fetch-command-num", null, "should be a positive value");
        }
        if (masterConfig.getPreExecThreads() <= 0) {
            errors.rejectValue("per-exec-threads", null, "should be a positive value");
        }
        if (masterConfig.getExecThreads() <= 0) {
            errors.rejectValue("exec-threads", null, "should be a positive value");
        }
        if (masterConfig.getDispatchTaskNumber() <= 0) {
            errors.rejectValue("dispatch-task-number", null, "should be a positive value");
        }
        if (masterConfig.getHeartbeatInterval().toMillis() < 0) {
            errors.rejectValue("heartbeat-interval", null, "should be a valid duration");
        }
        if (masterConfig.getTaskCommitRetryTimes() <= 0) {
            errors.rejectValue("task-commit-retry-times", null, "should be a positive value");
        }
        if (masterConfig.getTaskCommitInterval().toMillis() <= 0) {
            errors.rejectValue("task-commit-interval", null, "should be a valid duration");
        }
        if (masterConfig.getStateWheelInterval().toMillis() <= 0) {
            errors.rejectValue("state-wheel-interval", null, "should be a valid duration");
        }
        if (masterConfig.getFailoverInterval().toMillis() <= 0) {
            errors.rejectValue("failover-interval", null, "should be a valid duration");
        }
        if (masterConfig.getMaxCpuLoadAvg() <= 0) {
            masterConfig.setMaxCpuLoadAvg(Runtime.getRuntime().availableProcessors() * 2);
        }
        if (masterConfig.getWorkerGroupRefreshInterval().getSeconds() < 10) {
            errors.rejectValue("worker-group-refresh-interval", null, "should >= 10s");
        }

        masterConfig.setMasterAddress(NetUtils.getAddr(masterConfig.getListenPort()));
        masterConfig.setMasterRegistryPath(REGISTRY_DOLPHINSCHEDULER_MASTERS + "/" + masterConfig.getMasterAddress());
        printConfig();
    }

    private void printConfig() {
        logger.info("Master config: listenPort -> {} ", listenPort);
        logger.info("Master config: fetchCommandNum -> {} ", fetchCommandNum);
        logger.info("Master config: preExecThreads -> {} ", preExecThreads);
        logger.info("Master config: execThreads -> {} ", execThreads);
        logger.info("Master config: dispatchTaskNumber -> {} ", dispatchTaskNumber);
        logger.info("Master config: hostSelector -> {} ", hostSelector);
        logger.info("Master config: heartbeatInterval -> {} ", heartbeatInterval);
        logger.info("Master config: taskCommitRetryTimes -> {} ", taskCommitRetryTimes);
        logger.info("Master config: taskCommitInterval -> {} ", taskCommitInterval);
        logger.info("Master config: stateWheelInterval -> {} ", stateWheelInterval);
        logger.info("Master config: maxCpuLoadAvg -> {} ", maxCpuLoadAvg);
        logger.info("Master config: reservedMemory -> {} ", reservedMemory);
        logger.info("Master config: failoverInterval -> {} ", failoverInterval);
        logger.info("Master config: killYarnJobWhenTaskFailover -> {} ", killYarnJobWhenTaskFailover);
        logger.info("Master config: registryDisconnectStrategy -> {} ", registryDisconnectStrategy);
        logger.info("Master config: masterAddress -> {} ", masterAddress);
        logger.info("Master config: masterRegistryPath -> {} ", masterRegistryPath);
        logger.info("Master config: workerGroupRefreshInterval -> {} ", workerGroupRefreshInterval);
    }
}
