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

package org.apache.dolphinscheduler.service.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.dolphinscheduler.common.constants.Constants.COLON;
import static org.apache.dolphinscheduler.common.constants.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.constants.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
import static org.apache.dolphinscheduler.common.constants.Constants.SINGLE_SLASH;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

/**
 * 注册中心客户端，封装了对ZooKeeper等注册中心的操作。提供Master/Worker节点的发现、心跳管理、服务器列表获取、
 * 分布式锁、节点订阅和连接状态监听等功能，是DolphinScheduler分布式调度的核心基础设施组件。
 */
@Component
public class RegistryClient {

    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

    private static final String EMPTY = "";
    private IStoppable stoppable;

    private final Registry registry;

    public RegistryClient(Registry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void afterConstruct() {
        initNodes();
    }

    /**
     * 在指定超时时间内连接到注册中心。
     *
     * @param duration the maximum time to wait for connection
     * @throws RegistryException if connection fails within the timeout
     */
    public void connectUntilTimeout(@NonNull Duration duration) throws RegistryException {
        registry.connectUntilTimeout(duration);
    }

    /**
     * 获取当前活跃的Master节点数量。
     *
     * @return number of active master nodes
     */
    public int getActiveMasterNum() {
        Collection<String> childrenList = new ArrayList<>();
        try {
            // read master node parent path from conf
            if (exists(rootNodePath(NodeType.MASTER))) {
                childrenList = getChildrenKeys(rootNodePath(NodeType.MASTER));
            }
        } catch (Exception e) {
            logger.error("getActiveMasterNum error", e);
        }
        return childrenList.size();
    }

    /**
     * 获取指定节点类型的所有服务器列表，包含服务器的主机、端口、心跳等信息。
     *
     * @param nodeType the type of node (MASTER or WORKER)
     * @return list of server instances
     */
    public List<Server> getServerList(NodeType nodeType) {
        Map<String, String> serverMaps = getServerMaps(nodeType);
        String parentPath = rootNodePath(nodeType);

        List<Server> serverList = new ArrayList<>();
        for (Map.Entry<String, String> entry : serverMaps.entrySet()) {
            String serverPath = entry.getKey();
            String heartBeatJson = entry.getValue();
            if (StringUtils.isEmpty(heartBeatJson)) {
                logger.error("The heartBeatJson is empty, serverPath: {}", serverPath);
                continue;
            }
            Server server = new Server();
            switch (nodeType) {
                case MASTER:
                    MasterHeartBeat masterHeartBeat = JSONUtils.parseObject(heartBeatJson, MasterHeartBeat.class);
                    server.setCreateTime(new Date(masterHeartBeat.getStartupTime()));
                    server.setLastHeartbeatTime(new Date(masterHeartBeat.getReportTime()));
                    server.setId(masterHeartBeat.getProcessId());
                    break;
                case WORKER:
                    WorkerHeartBeat workerHeartBeat = JSONUtils.parseObject(heartBeatJson, WorkerHeartBeat.class);
                    server.setCreateTime(new Date(workerHeartBeat.getStartupTime()));
                    server.setLastHeartbeatTime(new Date(workerHeartBeat.getReportTime()));
                    server.setId(workerHeartBeat.getProcessId());
                    break;
            }

            server.setResInfo(heartBeatJson);
            // todo: add host, port in heartBeat Info, so that we don't need to parse this again
            server.setZkDirectory(parentPath + "/" + serverPath);
            // set host and port
            String[] hostAndPort = serverPath.split(COLON);
            // fetch the last one
            server.setHost(hostAndPort[0]);
            server.setPort(Integer.parseInt(hostAndPort[1]));
            serverList.add(server);
        }
        return serverList;
    }

    /**
     * 获取指定节点类型的服务器映射表，返回 host:port 到心跳信息的映射。
     *
     * @param nodeType the type of node (MASTER or WORKER)
     * @return server host:port -> value map
     */
    public Map<String, String> getServerMaps(NodeType nodeType) {
        Map<String, String> serverMap = new HashMap<>();
        try {
            String path = rootNodePath(nodeType);
            Collection<String> serverList = getServerNodes(nodeType);
            for (String server : serverList) {
                serverMap.putIfAbsent(server, get(path + SINGLE_SLASH + server));
            }
        } catch (Exception e) {
            logger.error("get server list failed", e);
        }

        return serverMap;
    }

    /**
     * 检查指定主机和节点类型是否已存在于注册中心。
     *
     * @param host the host to check
     * @param nodeType the type of node
     * @return true if the node exists
     */
    public boolean checkNodeExists(String host, NodeType nodeType) {
        return getServerMaps(nodeType).keySet()
                .stream()
                .anyMatch(it -> it.contains(host));
    }

    /**
     * 直接从注册中心获取Master节点列表。
     *
     * @return collection of master node keys
     */
    public Collection<String> getMasterNodesDirectly() {
        return getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_MASTERS);
    }

    /**
     * get host ip:port, path format: parentPath/ip:port
     *
     * @param path path
     * @return host ip:port, string format: parentPath/ip:port
     */
    /**
     * 从事件数据路径中解析主机信息（ip:port）。路径格式为 parentPath/ip:port。
     *
     * @param path the event data path
     * @return host ip:port string
     */
    public String getHostByEventDataPath(String path) {
        checkArgument(!Strings.isNullOrEmpty(path), "path cannot be null or empty");

        final String[] pathArray = path.split(SINGLE_SLASH);

        checkArgument(pathArray.length >= 1, "cannot parse path: %s", path);

        return pathArray[pathArray.length - 1];
    }

    /**
     * 关闭注册中心客户端连接。
     *
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        registry.close();
    }

    /**
     * 向注册中心写入临时节点（会话结束时自动删除）。
     *
     * @param key the key path
     * @param value the value to store
     */
    public void persistEphemeral(String key, String value) {
        registry.put(key, value, true);
    }

    /**
     * 从注册中心删除指定的键。
     *
     * @param key the key to remove
     */
    public void remove(String key) {
        registry.delete(key);
    }

    /**
     * 从注册中心获取指定键的值。
     *
     * @param key the key to get
     * @return the value associated with the key
     */
    public String get(String key) {
        return registry.get(key);
    }

    /**
     * 订阅指定路径的变更事件。
     *
     * @param path the path to subscribe
     * @param listener the listener to handle events
     */
    public void subscribe(String path, SubscribeListener listener) {
        registry.subscribe(path, listener);
    }

    /**
     * 添加连接状态监听器，用于监听与注册中心的连接状态变化。
     *
     * @param listener the connection listener
     */
    public void addConnectionStateListener(ConnectionListener listener) {
        registry.addConnectionStateListener(listener);
    }

    /**
     * 检查指定键是否存在于注册中心。
     *
     * @param key the key to check
     * @return true if the key exists
     */
    public boolean exists(String key) {
        return registry.exists(key);
    }

    /**
     * 获取分布式锁。
     *
     * @param key the lock key
     * @return true if the lock was acquired
     */
    public boolean getLock(String key) {
        return registry.acquireLock(key);
    }

    /**
     * 释放分布式锁。
     *
     * @param key the lock key
     * @return true if the lock was released
     */
    public boolean releaseLock(String key) {
        return registry.releaseLock(key);
    }

    public void setStoppable(IStoppable stoppable) {
        this.stoppable = stoppable;
    }

    public IStoppable getStoppable() {
        return stoppable;
    }

    /**
     * 判断路径是否为Master节点路径。
     *
     * @param path the path to check
     * @return true if the path is a master path
     */
    public boolean isMasterPath(String path) {
        return path != null && path.startsWith(REGISTRY_DOLPHINSCHEDULER_MASTERS);
    }

    /**
     * 判断路径是否为Worker节点路径。
     *
     * @param path the path to check
     * @return true if the path is a worker path
     */
    public boolean isWorkerPath(String path) {
        return path != null && path.startsWith(REGISTRY_DOLPHINSCHEDULER_WORKERS);
    }

    /**
     * 获取指定父路径下的所有子节点。
     *
     * @param key the parent path
     * @return collection of child keys
     */
    public Collection<String> getChildrenKeys(final String key) {
        return registry.children(key);
    }

    /**
     * 获取指定节点类型的服务器节点集合。
     *
     * @param nodeType the type of node
     * @return set of server nodes
     */
    public Set<String> getServerNodeSet(NodeType nodeType) {
        try {
            return new HashSet<>(getServerNodes(nodeType));
        } catch (Exception e) {
            throw new RegistryException("Failed to get server node: " + nodeType, e);
        }
    }

    private void initNodes() {
        registry.put(REGISTRY_DOLPHINSCHEDULER_MASTERS, EMPTY, false);
        registry.put(REGISTRY_DOLPHINSCHEDULER_WORKERS, EMPTY, false);
    }

    private String rootNodePath(NodeType type) {
        switch (type) {
            case MASTER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
            case WORKER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
            default:
                throw new IllegalStateException("Should not reach here");
        }
    }

    private Collection<String> getServerNodes(NodeType nodeType) {
        final String path = rootNodePath(nodeType);
        return getChildrenKeys(path);
    }

}
