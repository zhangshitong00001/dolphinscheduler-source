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

package org.apache.dolphinscheduler.rpc.client;

import org.apache.dolphinscheduler.rpc.common.AbstractRpcCallBack;
import org.apache.dolphinscheduler.rpc.common.ConsumerConfigConstants;

/**
 * 消费者配置。缓存RPC调用的消费者配置，包括服务回调类、ACK回调类、异步模式、重试次数等，在RPC调用生成时优先从此处获取。
 */
public class ConsumerConfig {

    /**
     * 服务回调类
     */
    private Class<? extends AbstractRpcCallBack> serviceCallBackClass;

    /**
     * ACK回调类
     */
    private Class<? extends AbstractRpcCallBack> ackCallBackClass;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 是否异步调用
     */
    private Boolean async = ConsumerConfigConstants.DEFAULT_SYNC;

    /**
     * 是否启用回调
     */
    private Boolean callBack = ConsumerConfigConstants.DEFAULT_CALL_BACK;

    /**
     * 失败重试次数
     */
    private Integer retries = ConsumerConfigConstants.DEFAULT_RETRIES;

    public Class<? extends AbstractRpcCallBack> getServiceCallBackClass() {
        return serviceCallBackClass;
    }

    void setServiceCallBackClass(Class<? extends AbstractRpcCallBack> serviceCallBackClass) {
        this.serviceCallBackClass = serviceCallBackClass;
    }

    public Class<? extends AbstractRpcCallBack> getAckCallBackClass() {
        return ackCallBackClass;
    }

    void setAckCallBackClass(Class<? extends AbstractRpcCallBack> ackCallBackClass) {
        this.ackCallBackClass = ackCallBackClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getAsync() {
        return async;
    }

    void setAsync(Boolean async) {
        this.async = async;
    }

    Integer getRetries() {
        return retries;
    }

    void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Boolean getCallBack() {
        return callBack;
    }

    public void setCallBack(Boolean callBack) {
        this.callBack = callBack;
    }
}
