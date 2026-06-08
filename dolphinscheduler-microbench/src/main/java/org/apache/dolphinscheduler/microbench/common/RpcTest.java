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

package org.apache.dolphinscheduler.microbench.common;

import org.apache.dolphinscheduler.microbench.base.AbstractBaseBenchmark;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.rpc.client.IRpcClient;
import org.apache.dolphinscheduler.rpc.client.RpcClient;
import org.apache.dolphinscheduler.rpc.remote.NettyClient;
import org.apache.dolphinscheduler.rpc.remote.NettyServer;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * DolphinScheduler RPC调用的JMH性能基准测试。测试RPC客户端创建并调用远程服务的吞吐量和平均耗时。
 */
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
public class RpcTest extends AbstractBaseBenchmark {
    /** Netty RPC服务端实例 */
    private NettyServer nettyServer;

    /** RPC用户服务代理 */
    private IUserService userService;

    /** RPC目标主机地址 */
    private Host host;
    /** RPC客户端实例 */
    private IRpcClient rpcClient = new RpcClient();

    /**
     * 基准测试前的初始化方法。
     * 启动Netty服务端，创建RPC客户端并连接到本地主机。
     *
     * @throws Exception 当服务启动或连接失败时抛出
     */
    @Setup
    public void before() throws Exception {
        nettyServer = new NettyServer(new NettyServerConfig());
        IRpcClient rpcClient = new RpcClient();
        host = new Host("127.0.0.1", 12346);
        userService = rpcClient.create(IUserService.class, host);

    }

    /**
     * 执行RPC调用基准测试。创建RPC代理并调用 {@link IUserService#hi(int)} 方法。
     *
     * @throws Exception 当RPC调用失败时抛出
     */
    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void sendTest() throws Exception {

        userService = rpcClient.create(IUserService.class, host);
        Integer result = userService.hi(1);
    }

    /**
     * 基准测试后的清理方法。
     * 关闭Netty客户端连接和RPC服务端。
     */
    @TearDown
    public void after() {
        NettyClient.getInstance().close();
        nettyServer.close();
    }

}
