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

package org.apache.dolphinscheduler.common.utils;

/**
 * 三参数函数式接口，接受三个输入参数并返回一个结果。
 * 用于需要三个输入参数的Lambda表达式或方法引用场景。
 *
 * @param <IN1> 第一个输入参数类型
 * @param <IN2> 第二个输入参数类型
 * @param <IN3> 第三个输入参数类型
 * @param <OUT1> 输出结果类型
 */
@FunctionalInterface
public interface TriFunction<IN1, IN2, IN3, OUT1> {

    OUT1 apply(IN1 in1, IN2 in2, IN3 in3);

}
