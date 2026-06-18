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

import org.apache.dolphinscheduler.rpc.base.Rpc;

/**
 * RPC用户服务接口。定义用于RPC性能基准测试的服务契约，包含同步和异步两种调用方式。
 */
public interface IUserService {

    /**
     * 异步RPC方法。通过 {@link UserCallback} 处理返回结果，支持最多9999次重试。
     *
     * @param s 输入的字符串
     * @return 处理结果
     */
    @Rpc(async = true, serviceCallback = UserCallback.class, retries = 9999)
    Boolean say(String s);

    /**
     * 同步RPC方法。返回输入值加1的结果。
     *
     * @param num 输入的整数
     * @return 输入值加1
     */
    Integer hi(int num);
}
