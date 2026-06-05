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

package org.apache.dolphinscheduler.plugin.registry.etcd;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Splitter;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.support.Observers;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.NonNull;

/**
 * 基于 Etcd 的注册中心实现。
 * 使用 etcd-java (jetcd) 客户端连接 Etcd 集群，提供以下分布式协调功能：
 * - 服务注册与发现：基于 key-value 的持久/临时数据存储（临时数据通过租约实现）
 * - 配置订阅：基于 Watch 机制的前缀匹配监听
 * - 分布式锁：基于 Etcd Lease 的锁实现（撤销租约即释放锁）
 * - 连接管理：定时轮询探测连接状态变化
 * - 认证安全：支持用户名/密码认证和负载均衡策略配置
 *
 * 与 ZooKeeper 的主要区别：
 * - Etcd 不支持递归创建路径，需确保父路径已存在
 * - Etcd 临时数据通过租约（Lease）+ keepAlive 实现，而非临时节点
 * - Etcd 连接状态通过主动轮询检测，而非被动回调
 *
 * 当配置 registry.type=etcd 时自动激活。
 */
@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
public class EtcdRegistry implements Registry {

    private static Logger LOGGER = LoggerFactory.getLogger(EtcdRegistry.class);

    /** Etcd 客户端 */
    private final Client client;

    /** Etcd 连接状态监听器，通过定时轮询检测连接状态 */
    private EtcdConnectionStateListener etcdConnectionStateListener;

    /** 租约保活管理器，用于维护临时 key 的租约心跳 */
    private EtcdKeepAliveLeaseManager etcdKeepAliveLeaseManager;

    /** 路径分隔符 */
    public static final String FOLDER_SEPARATOR = "/";

    /**
     * 线程级别的分布式锁信息映射表。
     * key 为锁路径，value 为对应的租约 ID。
     * 使用 ThreadLocal 保证同一线程内的锁操作隔离，释放锁时通过 revoke 租约来实现。
     */
    private static final ThreadLocal<Map<String, Long>> threadLocalLockMap = new ThreadLocal<>();

    /** Watch 监听器缓存，key 为监听的路径前缀，value 为对应的 Watcher 实例 */
    private final Map<String, Watch.Watcher> watcherMap = new ConcurrentHashMap<>();

    /** 租约默认 TTL，30 秒。超过此时间未续约，租约将过期 */
    private static final long TIME_TO_LIVE_SECONDS = 30L;

    /**
     * 构造 Etcd 注册中心实例。
     * 配置 Etcd 客户端：连接端点、命名空间、认证信息、重试策略、负载均衡策略等。
     * 同时初始化连接状态监听器和租约保活管理器。
     *
     * @param registryProperties Etcd 注册中心配置属性
     */
    public EtcdRegistry(EtcdRegistryProperties registryProperties) {
        // 构建 Etcd 客户端
        ClientBuilder clientBuilder = Client.builder()
                .endpoints(Util.toURIs(Splitter.on(",").trimResults().splitToList(registryProperties.getEndpoints())))
                .namespace(byteSequence(registryProperties.getNamespace()))
                .connectTimeout(registryProperties.getConnectionTimeout())
                .retryChronoUnit(ChronoUnit.MILLIS)
                .retryDelay(registryProperties.getRetryDelay().toMillis())
                .retryMaxDelay(registryProperties.getRetryMaxDelay().toMillis())
                .retryMaxDuration(registryProperties.getRetryMaxDuration());

        // 如果配置了用户名和密码，则启用认证
        if (StringUtils.hasLength(registryProperties.getUser()) && StringUtils.hasLength(registryProperties.getPassword())) {
            clientBuilder.user(byteSequence(registryProperties.getUser()));
            clientBuilder.password(byteSequence(registryProperties.getPassword()));
        }

        // 配置负载均衡策略
        if (StringUtils.hasLength(registryProperties.getLoadBalancerPolicy())) {
            clientBuilder.loadBalancerPolicy(registryProperties.getLoadBalancerPolicy());
        }

        // 配置认证授权机构
        if (StringUtils.hasLength(registryProperties.getAuthority())) {
            clientBuilder.authority(registryProperties.getAuthority());
        }

        client = clientBuilder.build();
        LOGGER.info("Started Etcd Registry...");
        etcdConnectionStateListener = new EtcdConnectionStateListener(client);
        etcdKeepAliveLeaseManager = new EtcdKeepAliveLeaseManager(client);
    }

    /**
     * 启动 Etcd 注册中心，启动连接状态监听器的定时轮询任务。
     * 该方法在 Bean 初始化完成后自动调用（@PostConstruct）。
     */
    @PostConstruct
    public void start() {
        LOGGER.info("Starting Etcd ConnectionListener...");
        etcdConnectionStateListener.start();
        LOGGER.info("Started Etcd ConnectionListener...");
    }

    /**
     * 阻塞等待直到成功连接 Etcd 或超时。
     * Etcd 客户端的 connectTimeout 已在构造函数中设置，因此此处无需额外等待。
     *
     * @param timeout 超时时间
     */
    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        // connectTimeout 已在构造函数中设置
    }

    /**
     * 订阅指定路径前缀下的所有 key 变更事件。
     * 使用 Etcd Watch 机制，以 prefix 模式监听 key 的 PUT 和 DELETE 事件。
     * 如果该路径已有 Watcher 则复用，否则创建新的 Watcher。
     *
     * @param path     要监听的 key 前缀
     * @param listener 数据变更回调监听器
     * @return true 表示订阅成功
     * @throws RegistryException 如果创建 Watch 失败
     */
    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        try {
            ByteSequence watchKey = byteSequence(path);
            // 使用 prefix 模式监听，匹配所有以 path 为前缀的 key
            WatchOption watchOption = WatchOption.newBuilder().isPrefix(true).build();
            watcherMap.computeIfAbsent(path, $ -> client.getWatchClient().watch(watchKey, watchOption, watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    listener.notify(new EventAdaptor(event, path));
                }
            }));
        } catch (Exception e) {
            throw new RegistryException("Failed to subscribe listener for key: " + path, e);
        }
        return true;
    }

    /**
     * 取消对指定路径的订阅，关闭对应的 Watcher 并释放资源。
     *
     * @param path 要取消订阅的路径前缀
     * @throws RegistryException 如果取消订阅失败或路径对应的 Watcher 不存在
     */
    @Override
    public void unsubscribe(String path) {
        try {
            watcherMap.get(path).close();
            watcherMap.remove(path);
        } catch (Exception e) {
            throw new RegistryException("Failed to unsubscribe listener for key: " + path, e);
        }
    }

    /**
     * 注册连接状态变化监听器。
     *
     * @param listener 连接状态变化回调接口
     */
    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        etcdConnectionStateListener.addConnectionListener(listener);
    }

    /**
     * 获取指定 key 对应的 value。
     *
     * @param key key 路径
     * @return key 对应的字符串值
     * @throws RegistryException 如果 key 不存在或获取数据失败
     */
    @Override
    public String get(String key) {
        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(key)).get().getKvs();
            return keyValues.iterator().next().getValue().toString(StandardCharsets.UTF_8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd get data error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd get data error, key = " + key, e);
        }
    }

    /**
     * 向指定 key 写入数据。
     *
     * @param key                节点路径
     * @param value              要存储的数据
     * @param deleteOnDisconnect true 表示创建临时 key（通过租约实现，客户端断连后自动过期删除），
     *                           false 表示创建持久 key
     * @throws RegistryException 如果写入数据失败
     */
    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        try {
            if (deleteOnDisconnect) {
                // 通过租约保持 key：创建一个带 TTL 的租约并启动 keepAlive 心跳
                // 当客户端断开连接后，keepAlive 停止，租约过期，key 被 Etcd 自动删除
                long leaseId = etcdKeepAliveLeaseManager.getOrCreateKeepAliveLease(key, TIME_TO_LIVE_SECONDS);
                PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
                client.getKVClient().put(byteSequence(key), byteSequence(value), putOption).get();
            } else {
                client.getKVClient().put(byteSequence(key), byteSequence(value)).get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("Failed to put registry key: " + key, e);
        } catch (ExecutionException e) {
            throw new RegistryException("Failed to put registry key: " + key, e);
        }
    }

    /**
     * 删除指定 key 及其所有子 key（前缀匹配删除）。
     * 使用 prefix 模式进行批量删除，与 ZooKeeper 的递归删除语义一致。
     *
     * @param key 要删除的 key 前缀
     * @throws RegistryException 如果删除失败
     */
    @Override
    public void delete(String key) {
        try {
            DeleteOption deleteOption = DeleteOption.newBuilder().isPrefix(true).build();
            client.getKVClient().delete(byteSequence(key), deleteOption).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("Failed to delete registry key: " + key, e);
        } catch (ExecutionException e) {
            throw new RegistryException("Failed to delete registry key: " + key, e);
        }
    }

    /**
     * 获取指定 key 下的直接子节点列表。
     * 通过前缀查询所有子 key，然后提取出直接子节点名称（以 "/" 分割的第一级）。
     *
     * @param key 父路径
     * @return 去重后的直接子节点名称列表
     * @throws RegistryException 如果获取子节点失败
     */
    @Override
    public Collection<String> children(String key) {
        // 确保路径以 "/" 结尾，避免前缀匹配到无关的 key
        // 例如 key=/nodes 需要变为 /nodes/ 以避免匹配到 /nodes-other
        String prefix = key.endsWith(FOLDER_SEPARATOR) ? key : key + FOLDER_SEPARATOR;

        // 按 key 排序，升序
        GetOption getOption = GetOption.newBuilder()
                .isPrefix(true)
                .withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.ASCEND)
                .build();

        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(prefix), getOption).get().getKvs();
            return keyValues.stream()
                    .map(e -> getSubNodeKeyName(prefix, e.getKey().toString(StandardCharsets.UTF_8)))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd get children error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd get children error, key: " + key, e);
        }
    }

    /**
     * 从完整路径中提取直接子节点名称。
     * 例如：prefix="/nodes/", fullPath="/nodes/child/grandchild" -> "child"
     *
     * @param prefix   父路径前缀
     * @param fullPath 完整的 key 路径
     * @return 直接子节点名称
     */
    private String getSubNodeKeyName(final String prefix, final String fullPath) {
        String pathWithoutPrefix = fullPath.substring(prefix.length());
        return pathWithoutPrefix.contains(FOLDER_SEPARATOR)
                ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(FOLDER_SEPARATOR))
                : pathWithoutPrefix;
    }

    /**
     * 检查指定 key 是否存在。
     * 使用 count_only 模式，只返回匹配数量而不返回具体数据，节省网络传输。
     *
     * @param key key 路径
     * @return true 表示 key 存在（匹配数量 >= 1）
     * @throws RegistryException 如果检查失败
     */
    @Override
    public boolean exists(String key) {
        GetOption getOption = GetOption.newBuilder().withCountOnly(true).build();
        try {
            if (client.getKVClient().get(byteSequence(key), getOption).get().getCount() >= 1) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd check key is existed error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd check key is existed error, key: " + key, e);
        }
        return false;
    }

    /**
     * 获取指定 key 的分布式锁。
     * 基于 Etcd Lease 实现：先创建一个 TTL 租约，然后使用该租约获取锁。
     * 通过撤销租约（revoke）来释放锁，因此即使客户端崩溃，租约过期后锁也会自动释放。
     *
     * @param key 锁的路径
     * @return true 表示成功获取锁
     * @throws RegistryException 如果获取锁失败
     */
    @Override
    public boolean acquireLock(String key) {
        Lock lockClient = client.getLockClient();
        Lease leaseClient = client.getLeaseClient();

        try {
            // 创建租约
            long leaseId = leaseClient.grant(TIME_TO_LIVE_SECONDS).get().getID();
            // 启动 keepAlive 心跳维持租约
            client.getLeaseClient().keepAlive(leaseId, Observers.observer(response -> {}));
            // 使用租约获取锁
            lockClient.lock(byteSequence(key), leaseId).get();

            // 将租约 ID 与当前线程关联，用于后续释放锁
            if (null == threadLocalLockMap.get()) {
                threadLocalLockMap.set(new HashMap<>());
            }
            threadLocalLockMap.get().put(key, leaseId);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd get lock error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd get lock error, lockKey: " + key, e);
        }
    }

    /**
     * 释放指定 key 的分布式锁。
     * 通过撤销（revoke）锁对应的租约来释放锁，这是一种可靠的释放方式：
     * revoke 租约会使 Etcd 自动释放该租约持有的所有锁。
     *
     * @param key 锁的路径
     * @return true 表示成功释放锁
     * @throws RegistryException 如果释放锁失败
     */
    @Override
    public boolean releaseLock(String key) {
        try {
            Long leaseId = threadLocalLockMap.get().get(key);
            // 撤销租约，Etcd 自动释放该租约持有的锁
            client.getLeaseClient().revoke(leaseId);
            threadLocalLockMap.get().remove(key);
            // 如果当前线程没有持有任何锁，清理 ThreadLocal 防止内存泄漏
            if (threadLocalLockMap.get().isEmpty()) {
                threadLocalLockMap.remove();
            }
        } catch (Exception e) {
            throw new RegistryException("etcd release lock error, lockKey: " + key, e);
        }
        return true;
    }

    /**
     * 关闭注册中心，释放 Etcd 客户端连接。
     * 关闭客户端时会自动关闭所有关联的 Watch。
     */
    @Override
    public void close() throws IOException {
        // 关闭 client 时会自动关闭所有 watch
        client.close();
    }

    /**
     * 将字符串转换为 Etcd 的 ByteSequence 类型。
     * 使用 UTF-8 编码。
     *
     * @param val 原始字符串
     * @return Etcd ByteSequence 实例
     */
    private static ByteSequence byteSequence(String val) {
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }

    /**
     * 事件适配器，将 Etcd WatchEvent 转换为 DolphinScheduler 内部的 Event 对象。
     * 事件类型映射：
     * - PUT -> ADD（包括新增和更新）
     * - DELETE -> REMOVE
     */
    static final class EventAdaptor extends Event {

        /**
         * 构造事件适配器。
         *
         * @param event Etcd Watch 事件
         * @param key   监听的路径前缀
         */
        public EventAdaptor(WatchEvent event, String key) {
            key(key);

            switch (event.getEventType()) {
                case PUT:
                    type(Type.ADD);
                    break;
                case DELETE:
                    type(Type.REMOVE);
                    break;
                default:
                    break;
            }
            KeyValue keyValue = event.getKeyValue();
            if (keyValue != null) {
                path(keyValue.getKey().toString(StandardCharsets.UTF_8));
                data(keyValue.getValue().toString(StandardCharsets.UTF_8));
            }
        }
    }
}

