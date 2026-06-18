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
import org.openjdk.jmh.annotations.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 枚举取值方式的JMH性能基准测试。对比通过 {@code values()} 遍历和静态Map查找两种方式在枚举取值时的性能差异。
 */
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 4, time = 1)
@State(Scope.Benchmark)
public class EnumBenchMark extends AbstractBaseBenchmark {

    /**
     * 简单的基准方法，始终返回true，用于验证JMH框架正常运行。
     *
     * @return 始终返回 {@code true}
     */
    @Benchmark
    public boolean simpleTest(){
        return Boolean.TRUE;
    }
    /** 测试用的枚举类型码参数 */
    @Param({"101", "108", "103", "104", "105", "103"})
    private int testNum;


    /**
     * 测试通过 {@code values()} 遍历查找枚举值的性能。
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void enumValuesTest() {
        TestTypeEnum.oldGetNameByType(testNum);
    }

    /**
     * 测试通过静态Map查找枚举值的性能。
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void enumStaticMapTest() {
        TestTypeEnum.newGetNameByType(testNum);
    }


    /** 测试用枚举类型，包含两种按code查找名称的实现方式 */
    public enum  TestTypeEnum {

        TYPE_101(101, "TYPE101"),
        TYPE_102(102, "TYPE102"),
        TYPE_103(103, "TYPE103"),
        TYPE_104(104, "TYPE104"),
        TYPE_105(105, "TYPE105"),
        TYPE_106(106, "TYPE106"),
        TYPE_107(107, "TYPE107"),
        TYPE_108(108, "TYPE108");

        /** 枚举类型码 */
        private int code;
        /** 枚举名称 */
        private String name;

        public int getCode() {
            return code;
        }


        public String getName() {
            return name;
        }


        TestTypeEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        /** 用于快速查找的静态Map，key为code，value为枚举实例 */
        private static final Map<Integer, TestTypeEnum> TEST_TYPE_MAP = new HashMap<>();

        static {
            for (TestTypeEnum testTypeEnum : TestTypeEnum.values()) {
                TEST_TYPE_MAP.put(testTypeEnum.code,testTypeEnum);
            }
        }

        /**
         * 通过静态Map查找枚举值（新方式）。
         *
         * @param code 枚举类型码
         * @throws IllegalArgumentException 当code无效时抛出
         */
        public static void newGetNameByType(int code) {
            if (TEST_TYPE_MAP.containsKey(code)) {
                TEST_TYPE_MAP.get(code);
                return;
            }
            throw new IllegalArgumentException("invalid code : " + code);
        }

        /**
         * 通过 {@code values()} 遍历查找枚举值（旧方式）。
         *
         * @param code 枚举类型码
         * @throws IllegalArgumentException 当code无效时抛出
         */
        public static void oldGetNameByType(int code) {
            for (TestTypeEnum testTypeEnum : TestTypeEnum.values()) {
                if (testTypeEnum.getCode() == code) {
                    return;
                }
            }
            throw new IllegalArgumentException("invalid code : " + code);
        }
    }

}
