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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 流程实例数据访问实现类，提供流程实例的增、改、插入或更新操作。
 */
@Slf4j
@Repository
public class ProcessInstanceDaoImpl implements ProcessInstanceDao {

    /** 流程实例 Mapper */
    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    /**
     * 插入新的流程实例记录。
     */
    @Override
    public int insertProcessInstance(ProcessInstance processInstance) {
        return processInstanceMapper.insert(processInstance);
    }

    /**
     * 更新已有的流程实例记录。
     */
    @Override
    public int updateProcessInstance(ProcessInstance processInstance) {
        return processInstanceMapper.updateById(processInstance);
    }

    /**
     * 插入或更新流程实例。如果 ID 已存在则更新，否则插入新记录。
     */
    @Override
    public int upsertProcessInstance(@NonNull ProcessInstance processInstance) {
        if (processInstance.getId() != null) {
            return updateProcessInstance(processInstance);
        } else {
            return insertProcessInstance(processInstance);
        }
    }
}
