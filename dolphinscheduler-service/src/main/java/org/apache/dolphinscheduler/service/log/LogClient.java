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

package org.apache.dolphinscheduler.service.log;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.GetAppIdResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;

import java.util.List;

import javax.annotation.Nullable;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 日志客户端服务，通过Netty远程通信向Worker和Master节点请求日志数据。
 * <p>支持以下日志操作：
 * <ul>
 *   <li>查看日志</li>
 *   <li>滚动查看日志（指定行号范围）</li>
 *   <li>获取日志字节内容</li>
 *   <li>删除任务日志</li>
 *   <li>获取AppId列表</li>
 * </ul></p>
 */
@Service
public class LogClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(LogClient.class);

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final NettyRemotingClient client;

    private static final long LOG_REQUEST_TIMEOUT = 10 * 1000L;

    public LogClient() {
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        this.client = new NettyRemotingClient(nettyClientConfig);
        logger.info("Initialized LogClientService with config: {}", nettyClientConfig);
    }

    /**
     * 滚动查看日志，跳过指定行数后读取限定行数的日志内容。
     *
     * @param host 目标主机地址
     * @param port 目标端口
     * @param path 日志文件路径
     * @param skipLineNum 跳过的行数
     * @param limit 读取的最大行数
     * @return 日志内容字符串
     */
    public String rollViewLog(String host, int port, String path, int skipLineNum, int limit) {
        logger.info("Roll view log from host : {}, port : {}, path {}, skipLineNum {} ,limit {}", host, port, path,
                skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                RollViewLogResponseCommand rollReviewLog =
                        JSONUtils.parseObject(response.getBody(), RollViewLogResponseCommand.class);
                return rollReviewLog.getMsg();
            }
            return "Roll view log response is null";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Roll view log from host : {}, port : {}, path {}, skipLineNum {} ,limit {} error, the current thread has been interrupted",
                    host, port, path, skipLineNum, limit, ex);
            return "Roll view log error: " + ex.getMessage();
        } catch (Exception e) {
            logger.error("Roll view log from host : {}, port : {}, path {}, skipLineNum {} ,limit {} error", host, port,
                    path, skipLineNum, limit, e);
            return "Roll view log error: " + e.getMessage();
        }
    }

    /**
     * 查看完整日志内容。
     *
     * @param host 目标主机地址
     * @param port 目标端口
     * @param path 日志文件路径
     * @return 日志内容字符串，若为本地主机则直接读取文件
     */
    public String viewLog(String host, int port, String path) {
        logger.info("View log from host: {}, port: {}, logPath: {}", host, port, path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            if (NetUtils.getHost().equals(host)) {
                return LoggerUtils.readWholeFileContent(request.getPath());
            } else {
                Command command = request.convert2Command();
                Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
                if (response != null) {
                    ViewLogResponseCommand viewLog =
                            JSONUtils.parseObject(response.getBody(), ViewLogResponseCommand.class);
                    return viewLog.getMsg();
                }
                return "View log response is null";
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("View log from host: {}, port: {}, logPath: {} error, the current thread has been interrupted",
                    host, port, path, ex);
            return "View log error: " + ex.getMessage();
        } catch (Exception e) {
            logger.error("View log from host: {}, port: {}, logPath: {} error", host, port, path, e);
            return "View log error: " + e.getMessage();
        }
    }

    /**
     * 获取日志文件的字节内容。
     *
     * @param host 目标主机地址
     * @param port 目标端口
     * @param path 日志文件路径
     * @return 日志文件内容的字节数组
     */
    public byte[] getLogBytes(String host, int port, String path) {
        logger.info("Get log bytes from host: {}, port: {}, logPath {}", host, port, path);
        GetLogBytesRequestCommand request = new GetLogBytesRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetLogBytesResponseCommand getLog =
                        JSONUtils.parseObject(response.getBody(), GetLogBytesResponseCommand.class);
                return getLog.getData() == null ? EMPTY_BYTE_ARRAY : getLog.getData();
            }
            return EMPTY_BYTE_ARRAY;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Get logSize from host: {}, port: {}, logPath: {} error, the current thread has been interrupted",
                    host, port, path, ex);
            return EMPTY_BYTE_ARRAY;
        } catch (Exception e) {
            logger.error("Get logSize from host: {}, port: {}, logPath: {} error", host, port, path, e);
            return EMPTY_BYTE_ARRAY;
        }
    }

    /**
     * 删除任务日志文件。
     *
     * @param host 目标主机地址
     * @param port 目标端口
     * @param path 日志文件路径
     * @return 删除成功返回true，否则返回false
     */
    public Boolean removeTaskLog(String host, int port, String path) {
        logger.info("Remove task log from host: {}, port: {}, logPath {}", host, port, path);
        RemoveTaskLogRequestCommand request = new RemoveTaskLogRequestCommand(path);
        final Host address = new Host(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                RemoveTaskLogResponseCommand taskLogResponse =
                        JSONUtils.parseObject(response.getBody(), RemoveTaskLogResponseCommand.class);
                return taskLogResponse.getStatus();
            }
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error(
                    "Remove task log from host: {}, port: {} logPath: {} error, the current thread has been interrupted",
                    host, port, path, ex);
            return false;
        } catch (Exception e) {
            logger.error("Remove task log from host: {}, port: {} logPath: {} error", host, port, path, e);
            return false;
        }
    }

    public @Nullable List<String> getAppIds(@NonNull String host, int port,
                                            @NonNull String taskLogFilePath) throws RemotingException, InterruptedException {
        logger.info("Begin to get appIds from worker: {}:{} taskLogPath: {}", host, port, taskLogFilePath);
        final Host workerAddress = new Host(host, port);
        List<String> appIds = null;
        if (NetUtils.getHost().equals(host)) {
            appIds = LogUtils.getAppIdsFromLogFile(taskLogFilePath);
        } else {
            final Command command = new GetAppIdRequestCommand(taskLogFilePath).convert2Command();
            Command response = this.client.sendSync(workerAddress, command, LOG_REQUEST_TIMEOUT);
            if (response != null) {
                GetAppIdResponseCommand responseCommand =
                        JSONUtils.parseObject(response.getBody(), GetAppIdResponseCommand.class);
                appIds = responseCommand.getAppIds();
            }
        }
        logger.info("Get appIds: {} from worker: {}:{} taskLogPath: {}", appIds, host, port, taskLogFilePath);
        return appIds;
    }

    @Override
    public void close() {
        this.client.close();
        logger.info("LogClientService closed");
    }

}
