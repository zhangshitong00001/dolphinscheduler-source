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

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 流程定义数据访问实现类，通过 MyBatis-Plus 分页插件查询指定用户和项目下的流程定义列表。
 */
@Repository
public class ProcessDefinitionDaoImpl implements ProcessDefinitionDao {

    /** 流程定义 Mapper */
    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * 分页查询指定用户和项目下的流程定义，支持按名称或描述模糊搜索。
     */
    @Override
    public PageListingResult<ProcessDefinition> listingProcessDefinition(int pageNumber, int pageSize, String searchVal,
                                                                         int userId, long projectCode) {
        Page<ProcessDefinition> page = new Page<>(pageNumber, pageSize);
        IPage<ProcessDefinition> processDefinitions =
                processDefinitionMapper.queryDefineListPaging(page, searchVal, userId, projectCode);

        return PageListingResult.<ProcessDefinition>builder()
                .totalCount(processDefinitions.getTotal())
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .records(processDefinitions.getRecords())
                .build();
    }
}
