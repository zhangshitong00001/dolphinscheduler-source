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

import org.apache.dolphinscheduler.registry.api.RegistryException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;

/**
 * Etcd 租约保活管理器，负责管理和维护 Etcd key 的租约生命周期。
 *
 * Etcd 通过租约（Lease）机制实现临时节点（Ephemeral Key）：
 * 1. 创建一个带 TTL 的租约
 * 2. 对该租约启动 keepAlive 以维持心跳
 * 3. 将 key 与租约绑定，当 keepAlive 中断（如客户端断连）后，租约过期，绑定的 key 自动删除
 *
 * 使用 ConcurrentHashMap 缓存 key 与 leaseId 的映射关系，线程安全且高效。
 */
@Slf4j
public class EtcdKeepAliveLeaseManager {

    /**
     * key 与 leaseId 的映射缓存。
     * key 为业务数据路径，value 为对应的 Etcd 租约 ID。
     */
    private final Map<String, Long> keyLeaseCache = new ConcurrentHashMap<>();

    /** Etcd 客户端 */
    private final Client client;

    EtcdKeepAliveLeaseManager(Client client) {
        this.client = client;
    }

    /**
     * 获取或创建一个带保活机制的租约。
     * 如果指定 key 已有对应的租约则直接返回；如果不存在则创建新租约并启动 keepAlive 心跳。
     *
     * keepAlive 是 gRPC 双向流：客户端持续发送心跳请求，服务端响应。
     * - onError：保活出错时，清理缓存中的租约记录
     * - onCompleted：保活流结束时，清理缓存中的租约记录
     *
     * @param key        业务数据路径
     * @param timeToLive 租约的 TTL（秒），到期后如果未续约，绑定的 key 会被 Etcd 自动删除
     * @return 租约 ID
     * @throws RegistryException 如果创建租约失败
     */
    long getOrCreateKeepAliveLease(String key, long timeToLive) {
        return keyLeaseCache.computeIfAbsent(key, $ -> {
            try {
                // 向 Etcd 申请一个指定 TTL 的租约
                long leaseId = client.getLeaseClient().grant(timeToLive).get().getID();

                // 启动 keepAlive 心跳保活
                client.getLeaseClient().keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {

                    @Override
                    public void onNext(LeaseKeepAliveResponse value) {
                        // 心跳响应正常，无需处理
                    }

                    @Override
                    public void onError(Throwable t) {
                        // 保活出错（通常是连接断开），从缓存中移除该租约
                        log.error("Lease {} keep alive error, remove cache with key:{}", leaseId, key, t);
                        keyLeaseCache.remove(key);
                    }

                    @Override
                    public void onCompleted() {
                        // 保活流结束，从缓存中移除该租约
                        log.error("Lease {} keep alive complete, remove cache with key:{}", leaseId, key);
                        keyLeaseCache.remove(key);
                    }
                });
                log.info("Lease {} keep alive create with key:{}", leaseId, key);
                return leaseId;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RegistryException("Failed to create lease key: " + key, e);
            } catch (ExecutionException e) {
                throw new RegistryException("Failed to create lease key: " + key, e);
            }
        });
    }

    /**
     * 获取指定 key 对应的租约 ID。
     *
     * @param key 业务数据路径
     * @return 租约 ID 的 Optional，如果不存在则为 Optional.empty()
     */
    Optional<Long> getKeepAliveLease(String key) {
        return Optional.ofNullable(keyLeaseCache.get(key));
    }
}
