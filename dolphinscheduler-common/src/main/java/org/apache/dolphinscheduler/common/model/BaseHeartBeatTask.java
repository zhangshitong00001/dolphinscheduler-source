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

package org.apache.dolphinscheduler.common.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;

/**
 * 心跳任务基类，定义了心跳的抽象实现框架。
 * 通过守护线程定期生成心跳信息并注册到注册中心，仅在服务处于RUNNING状态时执行。
 * 子类需实现getHeartBeat()和writeHeartBeat()方法来定义具体的心跳逻辑。
 *
 * @param <T> 心跳数据类型
 */
@Slf4j
public abstract class BaseHeartBeatTask<T> extends BaseDaemonThread {

    private final String threadName;
    private final long heartBeatInterval;

    protected boolean runningFlag;

    public BaseHeartBeatTask(String threadName, long heartBeatInterval) {
        super(threadName);
        this.threadName = threadName;
        this.heartBeatInterval = heartBeatInterval;
        this.runningFlag = true;
    }

    @Override
    public synchronized void start() {
        log.info("Starting {}", threadName);
        super.start();
        log.info("Started {}, heartBeatInterval: {}", threadName, heartBeatInterval);
    }

    @Override
    public void run() {
        while (runningFlag) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    log.info("The current server status is {}, will not write heartBeatInfo into registry", ServerLifeCycleManager.getServerStatus());
                    continue;
                }
                T heartBeat = getHeartBeat();
                writeHeartBeat(heartBeat);
            } catch (Exception ex) {
                log.error("{} task execute failed", threadName, ex);
            } finally {
                try {
                    Thread.sleep(heartBeatInterval);
                } catch (InterruptedException e) {
                    handleInterruptException(e);
                }
            }
        }
    }

    public void shutdown() {
        log.warn("{} task finished", threadName);
        runningFlag = false;
    }

    private void handleInterruptException(InterruptedException ex) {
        log.warn("{} has been interrupted", threadName, ex);
        Thread.currentThread().interrupt();
    }

    /**
     * 生成当前心跳数据。子类需实现具体的心跳数据采集逻辑。
     *
     * @return 心跳数据
     */
    public abstract T getHeartBeat();

    /**
     * 将心跳数据写入注册中心。子类需实现具体的写入逻辑。
     *
     * @param heartBeat 心跳数据
     */
    public abstract void writeHeartBeat(T heartBeat);
}
