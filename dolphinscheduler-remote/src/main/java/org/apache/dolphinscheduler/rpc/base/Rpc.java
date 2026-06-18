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

package org.apache.dolphinscheduler.rpc.base;

import org.apache.dolphinscheduler.rpc.common.AbstractRpcCallBack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC方法注解。用于标记需要远程调用的方法，配置重试次数、异步模式、回调等参数。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rpc {

    /**
     * 失败重试次数
     */
    int retries() default 3;

    /**
     * 是否异步调用
     */
    boolean async() default false;

    /**
     * 是否需要ACK确认
     */
    boolean ack() default false;

    /**
     * 是否启用回调
     */
    boolean callBack() default false;

    //todo It is better to set the timeout period for synchronous calls

    /**
     * 异步调用时的服务回调类
     */
    Class<? extends AbstractRpcCallBack> serviceCallback() default AbstractRpcCallBack.class;

    /**
     * ACK确认时的回调类
     */
    Class<? extends AbstractRpcCallBack> ackCallback() default AbstractRpcCallBack.class;




}
