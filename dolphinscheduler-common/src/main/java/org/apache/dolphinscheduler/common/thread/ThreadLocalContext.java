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
 * 线程本地上下文，用于在线程级别存储时区等上下文信息。
 * 提供ThreadLocal变量来隔离不同线程的时区设置，确保多线程环境下时区信息的安全性。
 */
public class ThreadLocalContext {

    public static final ThreadLocal<String> timezoneThreadLocal = new ThreadLocal<>();

    /**
     * 获取时区信息的ThreadLocal变量。
     *
     * @return 存储时区字符串的ThreadLocal
     */
    public static ThreadLocal<String> getTimezoneThreadLocal() {
        return timezoneThreadLocal;
    }
}
