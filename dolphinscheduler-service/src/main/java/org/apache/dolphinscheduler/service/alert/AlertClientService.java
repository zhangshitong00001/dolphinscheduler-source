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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 告警客户端服务，通过Netty远程通信向告警服务器同步发送告警请求。
 * <p>实现{@link AutoCloseable}接口，支持资源自动释放。</p>
 */
public class AlertClientService implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AlertClientService.class);

    private final NettyClientConfig clientConfig;

    private final NettyRemotingClient client;

    private final AtomicBoolean isRunning;

    private String host;

    private int port;

    /** 告警请求超时时间（毫秒） */
    private static final long ALERT_REQUEST_TIMEOUT = 10 * 1000L;

    public AlertClientService() {
        this.clientConfig = new NettyClientConfig();
        this.client = new NettyRemotingClient(clientConfig);
        this.isRunning = new AtomicBoolean(true);
    }

    public AlertClientService(String host, int port) {
        this();
        this.host = host;
        this.port = port;
    }

    /**
     * 关闭告警客户端，释放Netty连接资源。
     */
    @Override
    public void close() {
        if (isRunning.compareAndSet(true, false)) {
            logger.warn("Alert client is already closed");
            return;
        }

        logger.info("Alter client closing");
        this.client.close();
        logger.info("Alter client closed");
    }

    /**
     * 使用默认的host和port同步发送告警。
     *
     * @param groupId 告警组ID
     * @param title 告警标题
     * @param content 告警内容
     * @param strategy 告警策略
     * @return 告警发送响应结果
     */
    public AlertSendResponseCommand sendAlert(int groupId, String title,  String content, int strategy) {
        return this.sendAlert(this.host,this.port,groupId,title,content,strategy);
    }

    /**
     * 向指定地址的告警服务器同步发送告警。
     *
     * @param host 告警服务器主机地址
     * @param port 告警服务器端口
     * @param groupId 告警组ID
     * @param title 告警标题
     * @param content 告警内容
     * @param strategy 告警策略
     * @return 告警发送响应结果，发送失败时返回null
     */
    public AlertSendResponseCommand sendAlert(String host, int port, int groupId, String title,  String content, int strategy) {
        logger.info("sync alert send, host : {}, port : {}, groupId : {}, title : {} , strategy : {} ", host, port, groupId, title, strategy);
        AlertSendRequestCommand request = new AlertSendRequestCommand(groupId, title, content, strategy);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, ALERT_REQUEST_TIMEOUT);
            if (response != null) {
                return JsonSerializer.deserialize(response.getBody(), AlertSendResponseCommand.class);
            }
        } catch (Exception e) {
            logger.error("sync alert send error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return null;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
