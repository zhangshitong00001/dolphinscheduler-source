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

import org.apache.dolphinscheduler.remote.utils.Constants;

/**
 * Netty服务器配置类。配置Netty服务端的连接参数，包括监听端口、连接队列长度、TCP参数、缓冲区大小和工作线程数等。
 */
public class NettyServerConfig {

    /**
     * 服务端连接队列最大长度（backlog）
     */
    private int soBacklog = 1024;

    /**
     * 是否启用TCP无延迟（Nagle算法）
     */
    private boolean tcpNoDelay = true;

    /**
     * 是否启用TCP KeepAlive保活机制
     */
    private boolean soKeepalive = true;

    /**
     * 发送缓冲区大小（字节）
     */
    private int sendBufferSize = 65535;

    /**
     * 接收缓冲区大小（字节）
     */
    private int receiveBufferSize = 65535;

    /**
     * 工作线程数，默认为CPU核心数
     */
    private int workerThread = Constants.CPUS;

    /**
     * 监听端口号
     */
    private int listenPort = 12346;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public void setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isSoKeepalive() {
        return soKeepalive;
    }

    public void setSoKeepalive(boolean soKeepalive) {
        this.soKeepalive = soKeepalive;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }
}
