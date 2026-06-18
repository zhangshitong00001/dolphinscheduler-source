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

import org.apache.dolphinscheduler.rpc.base.RpcService;

/**
 * RPC用户服务实现。提供 {@code say} 和 {@code hi} 两个测试方法，用于RPC性能基准测试。
 */
@RpcService("IUserService")
public class UserService implements IUserService {

    /**
     * 输出传入的字符串并返回true。
     *
     * @param s 输入的字符串
     * @return 始终返回 {@code true}
     */
    @Override
    public Boolean say(String s) {
        return true;
    }

    /**
     * 返回传入数值加1的结果。
     *
     * @param num 输入的整数
     * @return 输入值加1
     */
    @Override
    public Integer hi(int num) {
        return ++num;
    }
}
