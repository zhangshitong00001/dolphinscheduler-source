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

package org.apache.dolphinscheduler.microbench.base;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMH基准测试抽象基类。提供默认的预热、测量和Fork配置，以及报告输出和测试运行能力。
 * 所有JMH基准测试类应继承此类以复用公共配置。
 */
@Warmup(iterations = AbstractBaseBenchmark.DEFAULT_WARMUP_ITERATIONS)
@Measurement(iterations = AbstractBaseBenchmark.DEFAULT_MEASURE_ITERATIONS)
@State(Scope.Thread)
@Fork(AbstractBaseBenchmark.DEFAULT_FORKS)
public abstract class AbstractBaseBenchmark {

    /** 默认预热迭代次数 */
    static final int DEFAULT_WARMUP_ITERATIONS = 10;

    /** 默认测量迭代次数 */
    static final int DEFAULT_MEASURE_ITERATIONS = 10;

    /** 默认Fork次数 */
    static final int DEFAULT_FORKS = 2;

    private static Logger logger = LoggerFactory.getLogger(AbstractBaseBenchmark.class);

    /**
     * 构建JMH运行选项。根据类名自动匹配测试、设置迭代参数，并处理JSON格式报告输出。
     *
     * @return 配置好的ChainedOptionsBuilder实例
     */
    private ChainedOptionsBuilder newOptionsBuilder() {

        String className = getClass().getSimpleName();

        ChainedOptionsBuilder optBuilder = new OptionsBuilder()
                // set benchmark ClassName
                .include(className);

        if (getMeasureIterations() > 0) {
            optBuilder.warmupIterations(getMeasureIterations());
        }

        if (getMeasureIterations() > 0) {
            optBuilder.measurementIterations(getMeasureIterations());
        }

        if (getForks() > 0) {
            optBuilder.forks(getForks());
        }

        String output = getReportDir();
        if (output != null) {
            boolean writeFileStatus;
            String filePath = getReportDir() + className + ".json";
            File file = new File(filePath);

            if (file.exists()) {
                writeFileStatus = file.delete();

            } else {
                writeFileStatus = file.getParentFile().mkdirs();
                try {
                    writeFileStatus = file.createNewFile();
                } catch (IOException e) {
                    logger.warn("jmh test create file error" + e);
                }
            }
            if (writeFileStatus) {
                optBuilder.resultFormat(ResultFormatType.JSON)
                        .result(filePath);
            }
        }
        return optBuilder;
    }

    /**
     * 执行JMH基准测试。通过JUnit的 {@link Test} 注解触发，构建运行选项并启动JMH Runner。
     *
     * @throws Exception 当基准测试执行失败时抛出
     */
    @Test
    public void run() throws Exception {
        new Runner(newOptionsBuilder().build()).run();
    }

    private int getWarmupIterations() {

        String value = System.getProperty("warmupIterations");
        return null != value ? Integer.parseInt(value) : -1;
    }

    private int getMeasureIterations() {
        String value = System.getProperty("measureIterations");
        return null != value ? Integer.parseInt(value) : -1;
    }

    private static String getReportDir() {
        return System.getProperty("perfReportDir");
    }

    private static int getForks() {
        String value = System.getProperty("forkCount");
        return null != value ? Integer.parseInt(value) : -1;
    }

}

