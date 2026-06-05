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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务生命周期管理器，管理DolphinScheduler服务的运行状态。
 * 维护服务的运行状态（RUNNING/WAITING/STOPPED），并提供状态切换和查询功能。
 * 使用synchronized确保状态切换的线程安全性。
 */
@Slf4j
@UtilityClass
public class ServerLifeCycleManager {

    /** 当前服务运行状态 */
    private static volatile ServerStatus serverStatus = ServerStatus.RUNNING;

    /** 服务启动时间戳 */
    private static long serverStartupTime = System.currentTimeMillis();

    /**
     * 获取服务启动时间戳。
     *
     * @return 启动时间（毫秒）
     */
    public static long getServerStartupTime() {
        return serverStartupTime;
    }

    /**
     * 判断服务是否处于运行状态。
     *
     * @return 如果服务正在运行返回true
     */
    public static boolean isRunning() {
        return serverStatus == ServerStatus.RUNNING;
    }

    /**
     * 判断服务是否已停止。
     *
     * @return 如果服务已停止返回true
     */
    public static boolean isStopped() {
        return serverStatus == ServerStatus.STOPPED;
    }

    /**
     * 获取当前服务的状态。
     *
     * @return 当前服务状态枚举
     */
    public static ServerStatus getServerStatus() {
        return serverStatus;
    }

    /**
     * 将服务状态从RUNNING切换为WAITING。
     * 仅当服务处于RUNNING状态时才能切换，已停止的服务无法进入等待状态。
     *
     * @throws ServerLifeCycleException 如果服务已停止，状态切换失败
     */
    public static synchronized void toWaiting() throws ServerLifeCycleException {
        if (isStopped()) {
            throw new ServerLifeCycleException("The current server is already stopped, cannot change to waiting");
        }

        if (serverStatus == ServerStatus.WAITING) {
            log.warn("The current server is already at waiting status, cannot change to waiting");
            return;
        }
        serverStatus = ServerStatus.WAITING;
    }

    /**
     * 将服务状态从WAITING恢复为RUNNING，并更新启动时间。
     * 仅当服务处于WAITING状态且未停止时才能恢复。
     *
     * @throws ServerLifeCycleException 如果服务已停止，无法恢复
     */
    public static synchronized void recoverFromWaiting() throws ServerLifeCycleException {
        if (isStopped()) {
            throw new ServerLifeCycleException("The current server is already stopped, cannot recovery");
        }

        if (serverStatus == ServerStatus.RUNNING) {
            log.warn("The current server status is already running, cannot recover form waiting");
            return;
        }
        serverStartupTime = System.currentTimeMillis();
        serverStatus = ServerStatus.RUNNING;
    }

    public static synchronized boolean toStopped() {
        if (serverStatus == ServerStatus.STOPPED) {
            return false;
        }
        serverStatus = ServerStatus.STOPPED;
        return true;
    }

}
