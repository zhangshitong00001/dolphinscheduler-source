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
package org.apache.dolphinscheduler.api.dto.resources.filter;

import org.apache.dolphinscheduler.dao.entity.Resource;

import java.util.List;

/**
 * 资源过滤器接口。定义资源过滤的标准接口，实现对资源列表的筛选操作。
 */
public interface IFilter {

    /**
     * 执行资源过滤
     * @return 过滤后的资源列表
     */
    List<Resource> filter();
}
