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

package org.apache.dolphinscheduler.common.factory;

import org.apache.dolphinscheduler.common.model.OssConnection;

import lombok.experimental.UtilityClass;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

/**
 * OSS客户端工厂类，用于创建阿里云OSS客户端实例。
 */
@UtilityClass
public class OssClientFactory {

    /**
     * 根据OSS连接配置构建OSS客户端实例。
     *
     * @param ossConnection OSS连接配置
     * @return OSS客户端实例
     */
    public OSS buildOssClient(OssConnection ossConnection) {
        return new OSSClientBuilder().build(ossConnection.getEndPoint(),
                ossConnection.getAccessKeyId(), ossConnection.getAccessKeySecret());
    }
}
