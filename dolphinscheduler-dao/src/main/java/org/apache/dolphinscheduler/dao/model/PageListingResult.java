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

package org.apache.dolphinscheduler.dao.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 分页查询结果通用封装类，包含当前页数据列表、总记录数、页码和每页大小。
 *
 * @param <T> 分页数据的类型
 */
@Data
@Builder
@AllArgsConstructor
public class PageListingResult<T> {

    /** 当前页数据记录列表 */
    private List<T> records;

    /** 总记录数 */
    private long totalCount;

    /** 当前页码 */
    private int currentPage;

    /** 每页数据量 */
    private int pageSize;
}
