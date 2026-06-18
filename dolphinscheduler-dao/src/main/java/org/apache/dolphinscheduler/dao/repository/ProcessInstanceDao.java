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

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;

/**
 * 流程实例数据访问接口，定义流程实例的持久化操作规范。
 */
public interface ProcessInstanceDao {

    /**
     * 插入新的流程实例记录。
     *
     * @param processInstance processInstance
     * @return insert count
     */
    public int insertProcessInstance(ProcessInstance processInstance);

    /**
     * 更新已有的流程实例记录。
     *
     * @param processInstance processInstance
     * @return update count
     */
    public int updateProcessInstance(ProcessInstance processInstance);

    /**
     * 插入或更新流程实例到数据库。
     *
     * @param processInstance processInstance
     */
    public int upsertProcessInstance(ProcessInstance processInstance);
}
