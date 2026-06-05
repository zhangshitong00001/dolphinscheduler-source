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

package org.apache.dolphinscheduler.remote.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.remote.utils.Constants;

/**
 * Netty客户端配置类。配置Netty客户端的连接参数，包括工作线程数、TCP参数、缓冲区大小和连接超时时间等。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NettyClientConfig {

    /**
     * 工作线程数，默认为CPU核心数
     */
    @Builder.Default
    private int workerThreads = Constants.CPUS;

    /**
     * 是否启用TCP无延迟（Nagle算法）
     */
    @Builder.Default
    private boolean tcpNoDelay = true;

    /**
     * 是否启用TCP KeepAlive保活机制
     */
    @Builder.Default
    private boolean soKeepalive = true;

    /**
     * 发送缓冲区大小（字节）
     */
    @Builder.Default
    private int sendBufferSize = 65535;

    /**
     * 接收缓冲区大小（字节）
     */
    @Builder.Default
    private int receiveBufferSize = 65535;

    /**
     * 连接超时时间（毫秒）
     */
    @Builder.Default
    private int connectTimeoutMillis = 3000;

}
