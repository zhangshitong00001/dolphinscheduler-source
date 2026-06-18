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

package org.apache.dolphinscheduler.common.thread;

/**
 * DolphinScheduler守护线程基类，所有DolphinScheduler中的线程都应继承此类。
 * 设置为守护线程（Daemon Thread），防止JVM因用户线程未退出而无法正常关闭，避免服务挂起问题。
 */
public abstract class BaseDaemonThread extends Thread {

    protected BaseDaemonThread(Runnable runnable) {
        super(runnable);
        this.setDaemon(true);
    }

    protected BaseDaemonThread(String threadName) {
        super();
        this.setName(threadName);
        this.setDaemon(true);
    }

}
