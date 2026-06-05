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

package org.apache.dolphinscheduler.server.worker.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.experimental.UtilityClass;

/**
 * Worker服务指标工具类。提供Worker运行状态的各种Micrometer指标收集功能，
 * 包括过载计数、提交队列满计数、资源下载成功/失败计数、下载耗时与大小统计以及运行中任务数指标。
 */
@UtilityClass
public class WorkerServerMetrics {

    private final Counter workerOverloadCounter =
        Counter.builder("ds.worker.overload.count")
            .description("overloaded workers count")
            .register(Metrics.globalRegistry);

    private final Counter workerFullSubmitQueueCounter =
        Counter.builder("ds.worker.full.submit.queue.count")
            .description("full worker submit queues count")
            .register(Metrics.globalRegistry);

    private final Counter workerResourceDownloadSuccessCounter =
            Counter.builder("ds.worker.resource.download.count")
                    .tag("status", "success")
                    .description("worker resource download success count")
                    .register(Metrics.globalRegistry);

    private final Counter workerResourceDownloadFailCounter =
            Counter.builder("ds.worker.resource.download.count")
                    .tag("status", "fail")
                    .description("worker resource download failure count")
                    .register(Metrics.globalRegistry);

    private final Timer workerResourceDownloadDurationTimer =
            Timer.builder("ds.worker.resource.download.duration")
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .description("time cost of resource download on workers")
                    .register(Metrics.globalRegistry);

    private final DistributionSummary workerResourceDownloadSizeDistribution =
            DistributionSummary.builder("ds.worker.resource.download.size")
            .baseUnit("bytes")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .publishPercentileHistogram()
            .description("size of downloaded resource files on worker")
            .register(Metrics.globalRegistry);

    /**
     * 增加Worker过载计数。当Worker检测到系统负载过高时调用此方法记录指标。
     */
    public void incWorkerOverloadCount() {
        workerOverloadCounter.increment();
    }

    /**
     * 增加Worker提交队列已满计数。当任务提交队列达到容量上限时记录此指标。
     */
    public void incWorkerSubmitQueueIsFullCount() {
        workerFullSubmitQueueCounter.increment();
    }

    /**
     * 增加资源下载成功计数。每次Worker成功下载任务所需资源文件时调用。
     */
    public void incWorkerResourceDownloadSuccessCount() {
        workerResourceDownloadSuccessCounter.increment();
    }

    /**
     * 增加资源下载失败计数。每次Worker下载任务所需资源文件失败时调用。
     */
    public void incWorkerResourceDownloadFailureCount() {
        workerResourceDownloadFailCounter.increment();
    }

    /**
     * 记录资源下载耗时。通过Timer指标记录每次资源下载的毫秒级耗时，支持百分位统计。
     *
     * @param milliseconds 下载耗时（毫秒）
     */
    public void recordWorkerResourceDownloadTime(final long milliseconds) {
        workerResourceDownloadDurationTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录资源下载文件大小。通过DistributionSummary指标记录每次下载的资源文件字节数。
     *
     * @param size 下载文件大小（字节）
     */
    public void recordWorkerResourceDownloadSize(final long size) {
        workerResourceDownloadSizeDistribution.record(size);
    }

    /**
     * 注册Worker运行中任务数指标。通过Gauge指标实时反映当前Worker正在执行的任务数量。
     *
     * @param supplier 提供当前运行任务数量的函数
     */
    public void registerWorkerRunningTaskGauge(final Supplier<Number> supplier) {
        Gauge.builder("ds.task.running", supplier)
            .description("number of running tasks on workers")
            .register(Metrics.globalRegistry);
    }

}
