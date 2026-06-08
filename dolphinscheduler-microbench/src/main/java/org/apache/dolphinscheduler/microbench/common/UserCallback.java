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

import org.apache.dolphinscheduler.rpc.common.AbstractRpcCallBack;

/**
 * RPC用户服务回调实现。用于异步RPC调用完成后的结果处理，继承自 {@link AbstractRpcCallBack}。
 */
public class UserCallback extends AbstractRpcCallBack {
    /**
     * RPC回调执行方法。当RPC异步调用完成时被触发。
     *
     * @param object RPC调用返回的结果对象
     */
    @Override
    public void run(Object object) {

    }
}
