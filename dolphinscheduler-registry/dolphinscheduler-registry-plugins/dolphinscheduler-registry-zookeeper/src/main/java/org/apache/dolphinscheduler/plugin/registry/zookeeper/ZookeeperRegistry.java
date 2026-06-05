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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import com.google.common.base.Strings;
import lombok.NonNull;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 基于 ZooKeeper 的注册中心实现。
 * 使用 Apache Curator 框架封装 ZooKeeper 原生 API，提供以下分布式协调功能：
 * - 服务注册与发现：持久/临时节点的增删改查
 * - 配置订阅：基于 TreeCache 的节点变更监听
 * - 分布式锁：基于 InterProcessMutex 的可重入锁
 * - 连接管理：带重试机制的健康检查与状态监听
 * - 认证安全：支持 digest 方式的 ACL 访问控制
 *
 * 当配置 registry.type=zookeeper 时自动激活。
 */
@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "zookeeper")
public final class ZookeeperRegistry implements Registry {

    /** ZooKeeper 连接配置属性 */
    private final ZookeeperRegistryProperties.ZookeeperProperties properties;

    /** Curator 客户端，通过 Curator 框架管理 ZooKeeper 连接和操作 */
    private final CuratorFramework client;

    /** TreeCache 缓存映射，key 为节点路径，value 为对应的树缓存实例，用于订阅节点变更 */
    private final Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    /**
     * 线程级别的分布式锁映射表。
     * 使用 ThreadLocal 保证同一线程内的锁操作隔离，key 为锁路径，value 为 InterProcessMutex 实例。
     */
    private static final ThreadLocal<Map<String, InterProcessMutex>> threadLocalLockMap = new ThreadLocal<>();

    /**
     * 构造 ZooKeeper 注册中心实例。
     * 配置 Curator 客户端：连接地址、重试策略、命名空间、会话超时、连接超时，以及可选的 digest 认证。
     *
     * @param registryProperties ZooKeeper 注册中心配置属性
     */
    public ZookeeperRegistry(ZookeeperRegistryProperties registryProperties) {
        properties = registryProperties.getZookeeper();

        // 指数退避重试策略：baseSleepTime 为初始休眠时间，maxRetries 为最大重试次数，maxSleep 为最大休眠时间上限
        final ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
                (int) properties.getRetryPolicy().getBaseSleepTime().toMillis(),
                properties.getRetryPolicy().getMaxRetries(),
                (int) properties.getRetryPolicy().getMaxSleep().toMillis());

        CuratorFrameworkFactory.Builder builder =
                CuratorFrameworkFactory.builder()
                        .connectString(properties.getConnectString())
                        .retryPolicy(retryPolicy)
                        .namespace(properties.getNamespace())
                        .sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
                        .connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis());

        // 如果配置了 digest，则启用 ACL 认证
        final String digest = properties.getDigest();
        if (!Strings.isNullOrEmpty(digest)) {
            buildDigest(builder, digest);
        }
        client = builder.build();
    }

    /**
     * 为 Curator Builder 配置 digest 认证方案。
     * 使用 digest 方式对 ZooKeeper 连接进行身份认证，并设置 ACL 为 CREATOR_ALL_ACL，
     * 即只有创建节点的用户拥有所有权限。
     *
     * @param builder Curator 客户端构建器
     * @param digest  digest 认证字符串，格式为 "username:password"
     */
    private void buildDigest(CuratorFrameworkFactory.Builder builder, String digest) {
        builder.authorization("digest", digest.getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {

                    @Override
                    public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }

                    @Override
                    public List<ACL> getAclForPath(final String path) {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                });
    }

    /**
     * 启动 ZooKeeper 注册中心，建立与 ZooKeeper 集群的连接。
     * 采用带重试的阻塞连接策略：先尝试多次带间隔的连接，每次等待 blockUntilConnected 指定的时间，
     * 若所有重试均失败，最后再尝试一次连接，如果仍然失败则关闭客户端并抛出异常。
     * 该方法是 Bean 初始化后自动调用的（@PostConstruct），确保在服务可用前连接已建立。
     */
    @PostConstruct
    public void start() {
        client.start();

        // 带重试的连接建立循环
        int maxRetries = 3;
        int retryCount = 0;
        boolean connected = false;

        while (retryCount < maxRetries && !connected) {
            try {
                if (client.blockUntilConnected((int) properties.getBlockUntilConnected().toMillis(), MILLISECONDS)) {
                    connected = true;
                } else {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        Thread.sleep(1000); // 连接失败，等待 1 秒后重试
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (retryCount < maxRetries - 1) {
                    retryCount++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // 最后一次连接尝试，如果失败则关闭客户端并抛出异常
        try {
            if (!client.blockUntilConnected((int) properties.getBlockUntilConnected().toMillis(), MILLISECONDS)) {
                client.close();
                throw new RegistryException("zookeeper connect timeout: " + properties.getConnectString());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 注册连接状态变化监听器。
     *
     * @param listener 连接状态变化回调接口
     */
    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        client.getConnectionStateListenable().addListener(new ZookeeperConnectionStateListener(listener));
    }

    /**
     * 阻塞等待直到成功连接 ZooKeeper 或超时。
     *
     * @param timeout 超时时间
     * @throws RegistryException 如果连接超时或线程被中断
     */
    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        try {
            if (!client.blockUntilConnected((int) timeout.toMillis(), MILLISECONDS)) {
                throw new RegistryException(
                        String.format("Cannot connect to the Zookeeper registry in %s s", timeout.getSeconds()));
            }
        } catch (RegistryException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException(
                    String.format("Cannot connect to the Zookeeper registry in %s s", timeout.getSeconds()), e);
        }
    }

    /**
     * 订阅指定路径下节点数据的变化。
     * 使用 Curator TreeCache 监听指定路径下所有节点的增删改事件。
     * 如果该路径的 TreeCache 已存在则复用，否则创建新的 TreeCache 实例。
     *
     * @param path     要订阅的 ZooKeeper 路径
     * @param listener 数据变更回调监听器
     * @return true 表示订阅成功
     * @throws RegistryException 如果启动 TreeCache 失败
     */
    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        final TreeCache treeCache = treeCacheMap.computeIfAbsent(path, $ -> new TreeCache(client, path));
        treeCache.getListenable().addListener(($, event) -> listener.notify(new EventAdaptor(event, path)));
        try {
            treeCache.start();
        } catch (Exception e) {
            treeCacheMap.remove(path);
            throw new RegistryException("Failed to subscribe listener for key: " + path, e);
        }
        return true;
    }

    /**
     * 取消对指定路径的订阅，关闭对应的 TreeCache 并释放资源。
     *
     * @param path 要取消订阅的路径
     */
    @Override
    public void unsubscribe(String path) {
        CloseableUtils.closeQuietly(treeCacheMap.get(path));
    }

    /**
     * 获取指定路径下存储的数据。
     *
     * @param key ZooKeeper 路径
     * @return 节点中存储的字符串数据
     * @throws RegistryException 如果获取数据失败
     */
    @Override
    public String get(String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RegistryException("zookeeper get data error", e);
        }
    }

    /**
     * 检查指定路径的节点是否存在。
     *
     * @param key ZooKeeper 路径
     * @return true 表示节点存在
     * @throws RegistryException 如果检查失败
     */
    @Override
    public boolean exists(String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (Exception e) {
            throw new RegistryException("zookeeper check key is existed error", e);
        }
    }

    /**
     * 向指定路径写入数据。
     * 根据 deleteOnDisconnect 参数决定创建持久节点（PERSISTENT）还是临时节点（EPHEMERAL）。
     * 如果父路径不存在，会自动创建所有需要的父节点。
     *
     * @param key                节点路径
     * @param value              要存储的数据
     * @param deleteOnDisconnect true 表示创建临时节点（连接断开后自动删除），false 表示创建持久节点
     * @throws RegistryException 如果写入数据失败
     */
    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        final CreateMode mode = deleteOnDisconnect ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;

        try {
            client.create()
                    .orSetData()           // 如果节点已存在则更新数据
                    .creatingParentsIfNeeded() // 自动创建父节点
                    .withMode(mode)
                    .forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RegistryException("Failed to put registry key: " + key, e);
        }
    }

    /**
     * 获取指定路径下的子节点列表，按倒序排列。
     * 倒序排列常用于主节点选举场景，使得序号最小的节点排在最后。
     *
     * @param key 父节点路径
     * @return 子节点名称列表（倒序）
     * @throws RegistryException 如果获取子节点失败
     */
    @Override
    public List<String> children(String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            result.sort(Comparator.reverseOrder());
            return result;
        } catch (Exception e) {
            throw new RegistryException("zookeeper get children error", e);
        }
    }

    /**
     * 删除指定节点及其所有子节点。
     * 如果节点不存在（NoNodeException），则忽略该异常，因为目标已达成。
     *
     * @param nodePath 要删除的节点路径
     * @throws RegistryException 如果删除失败且不是节点不存在的异常
     */
    @Override
    public void delete(String nodePath) {
        try {
            client.delete()
                    .deletingChildrenIfNeeded() // 递归删除所有子节点
                    .forPath(nodePath);
        } catch (KeeperException.NoNodeException ignored) {
            // 节点已删除或不存在，无需处理
        } catch (Exception e) {
            throw new RegistryException("Failed to delete registry key: " + nodePath, e);
        }
    }

    /**
     * 获取指定路径的分布式锁。
     * 使用 Curator InterProcessMutex 实现可重入的分布式锁，锁与当前线程绑定。
     *
     * @param key 锁的路径
     * @return true 表示成功获取锁
     * @throws RegistryException 如果获取锁失败或释放锁失败
     */
    @Override
    public boolean acquireLock(String key) {
        InterProcessMutex interProcessMutex = new InterProcessMutex(client, key);
        try {
            interProcessMutex.acquire();
            // 将锁与当前线程关联，存储到 ThreadLocal 中
            if (null == threadLocalLockMap.get()) {
                threadLocalLockMap.set(new HashMap<>(3));
            }
            threadLocalLockMap.get().put(key, interProcessMutex);
            return true;
        } catch (Exception e) {
            try {
                interProcessMutex.release(); // 获取锁失败时尝试释放
                throw new RegistryException("zookeeper get lock error", e);
            } catch (Exception exception) {
                throw new RegistryException("zookeeper release lock error", e);
            }
        }
    }

    /**
     * 释放指定路径的分布式锁。
     * 从 ThreadLocal 中取出当前线程持有的锁并释放，同时清理 ThreadLocal 中的锁记录。
     *
     * @param key 锁的路径
     * @return true 表示成功释放锁，false 表示当前线程未持有该锁
     * @throws RegistryException 如果释放锁失败
     */
    @Override
    public boolean releaseLock(String key) {
        if (null == threadLocalLockMap.get().get(key)) {
            return false;
        }
        try {
            threadLocalLockMap.get().get(key).release();
            threadLocalLockMap.get().remove(key);
            // 如果当前线程没有持有任何锁，清理 ThreadLocal 防止内存泄漏
            if (threadLocalLockMap.get().isEmpty()) {
                threadLocalLockMap.remove();
            }
        } catch (Exception e) {
            throw new RegistryException("zookeeper release lock error", e);
        }
        return true;
    }

    /**
     * 关闭注册中心，释放所有资源。
     * 依次关闭所有 TreeCache 和 Curator 客户端连接。
     */
    @Override
    public void close() {
        treeCacheMap.values().forEach(CloseableUtils::closeQuietly);
        CloseableUtils.closeQuietly(client);
    }

    /**
     * 事件适配器，将 Curator TreeCacheEvent 转换为 DolphinScheduler 内部的 Event 对象。
     * 负责映射事件类型（NODE_ADDED -> ADD, NODE_UPDATED -> UPDATE, NODE_REMOVED -> REMOVE）
     * 并填充路径和数据信息。
     */
    static final class EventAdaptor extends Event {

        /**
         * 构造事件适配器。
         *
         * @param event Curator 树缓存事件
         * @param key   订阅的根路径
         */
        public EventAdaptor(TreeCacheEvent event, String key) {
            key(key);

            switch (event.getType()) {
                case NODE_ADDED:
                    type(Type.ADD);
                    break;
                case NODE_UPDATED:
                    type(Type.UPDATE);
                    break;
                case NODE_REMOVED:
                    type(Type.REMOVE);
                    break;
                default:
                    break;
            }

            final ChildData data = event.getData();
            if (data != null) {
                path(data.getPath());
                data(new String(data.getData()));
            }
        }
    }
}
