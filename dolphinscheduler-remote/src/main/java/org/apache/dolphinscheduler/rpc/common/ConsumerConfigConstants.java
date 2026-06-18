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

package org.apache.dolphinscheduler.rpc.common;

/**
 * RPC 消费者配置常量。定义 RPC 客户端调用的默认参数，如同步模式、重试次数和回调开关。
 */
public class ConsumerConfigConstants {

    private ConsumerConfigConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** 默认同步模式：异步 */
    public static final Boolean DEFAULT_SYNC = false;

    /** 默认重试次数：3次 */
    public static final Integer DEFAULT_RETRIES = 3;

    /** 默认回调开关：关闭 */
    public static final Boolean DEFAULT_CALL_BACK = false;
}
