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

package org.apache.dolphinscheduler.rpc.common;

/**
 * RPC 异步回调抽象基类。当发送异步消息且响应成功返回后，会调用此回调方法。
 * 子类需实现 run 方法以处理异步响应结果。
 */
public abstract class AbstractRpcCallBack {

    /**
     * 当异步消息的响应成功返回后被调用。
     *
     * @param object RPC 响应结果对象
     */
    public abstract void run(Object object);

}
