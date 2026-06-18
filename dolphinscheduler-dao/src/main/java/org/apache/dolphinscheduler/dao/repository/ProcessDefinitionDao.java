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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.model.PageListingResult;

import javax.annotation.Nullable;

/**
 * 流程定义数据访问接口，定义流程定义的分页查询规范。
 */
public interface ProcessDefinitionDao {

    /**
     * 分页查询指定用户和项目下的流程定义。如果 searchVal 不为空，则用于模糊匹配流程定义名称或描述。
     * todo: 当前方法使用模糊查询 searchVal，当目标用户/项目下存在大量流程定义时性能可能很差。
     *
     * @param pageNumber page number
     * @param pageSize page size
     * @param searchVal search value, nullable
     * @param userId user id
     * @param projectCode project code
     * @return page listing result of ProcessDefinition
     */
    PageListingResult<ProcessDefinition> listingProcessDefinition(
                                                                  int pageNumber,
                                                                  int pageSize,
                                                                  @Nullable String searchVal,
                                                                  int userId,
                                                                  long projectCode);

}
