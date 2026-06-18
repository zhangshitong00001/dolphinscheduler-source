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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;

import org.apache.dolphinscheduler.api.dto.EnvironmentDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 环境配置服务实现类。负责环境的增删改查和工作组关联管理，支持环境编码自动生成和权限校验。
 */
@Service
public class EnvironmentServiceImpl extends BaseServiceImpl implements EnvironmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentServiceImpl.class);

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private EnvironmentWorkerGroupRelationMapper relationMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    /**
     * 创建环境配置。自动生成环境编码，同时创建与工作组的关联关系。
     *
     * @param loginUser 当前登录用户
     * @param name 环境名称
     * @param config 环境配置内容
     * @param desc 描述信息
     * @param workerGroups 工作组列表（JSON数组格式）
     * @return 包含新环境编码的结果Map
     */
    @Override
    @Transactional
    public Map<String, Object> createEnvironment(User loginUser, String name, String config, String desc,
                                                 String workerGroups) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ENVIRONMENT, ENVIRONMENT_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (checkDescriptionLength(desc)) {
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        Map<String, Object> checkResult = checkParams(name, config, workerGroups);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(name);
        if (environment != null) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, name);
            return result;
        }

        Environment env = new Environment();
        env.setName(name);
        env.setConfig(config);
        env.setDescription(desc);
        env.setOperator(loginUser.getId());
        env.setCreateTime(new Date());
        env.setUpdateTime(new Date());
        long code = 0L;
        try {
            code = CodeGenerateUtils.getInstance().genCode();
            env.setCode(code);
        } catch (CodeGenerateException e) {
            logger.error("Environment code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating environment code");
            return result;
        }

        if (environmentMapper.insert(env) > 0) {
            if (!StringUtils.isEmpty(workerGroups)) {
                List<String> workerGroupList = JSONUtils.parseObject(workerGroups, new TypeReference<List<String>>() {
                });
                if (CollectionUtils.isNotEmpty(workerGroupList)) {
                    workerGroupList.stream().forEach(workerGroup -> {
                        if (!StringUtils.isEmpty(workerGroup)) {
                            EnvironmentWorkerGroupRelation relation = new EnvironmentWorkerGroupRelation();
                            relation.setEnvironmentCode(env.getCode());
                            relation.setWorkerGroup(workerGroup);
                            relation.setOperator(loginUser.getId());
                            relation.setCreateTime(new Date());
                            relation.setUpdateTime(new Date());
                            relationMapper.insert(relation);
                        }
                    });
                }
            }
            result.put(Constants.DATA_LIST, env.getCode());
            putMsg(result, Status.SUCCESS);
            permissionPostHandle(AuthorizationType.ENVIRONMENT, loginUser.getId(),
                    Collections.singletonList(env.getId()), logger);
        } else {
            putMsg(result, Status.CREATE_ENVIRONMENT_ERROR);
        }
        return result;
    }

    /**
     * 分页查询环境列表，支持搜索过滤和权限控制，返回包含关联工作组信息的环境DTO。
     *
     * @param loginUser 当前登录用户
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param searchVal 搜索关键字
     * @return 包含分页环境DTO列表的结果对象
     */
    @Override
    public Result queryEnvironmentListPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal) {
        Result<Object> result = new Result();

        Page<Environment> page = new Page<>(pageNo, pageSize);
        PageInfo<EnvironmentDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        IPage<Environment> environmentIPage;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            environmentIPage = environmentMapper.queryEnvironmentListPaging(page, searchVal);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.ENVIRONMENT, loginUser.getId(), logger);
            if (ids.isEmpty()) {
                result.setData(pageInfo);
                putMsg(result, Status.SUCCESS);
                return result;
            }
            environmentIPage = environmentMapper.queryEnvironmentListPagingByIds(page, new ArrayList<>(ids), searchVal);
        }

        pageInfo.setTotal((int) environmentIPage.getTotal());

        if (CollectionUtils.isNotEmpty(environmentIPage.getRecords())) {
            Map<Long, List<String>> relationMap = relationMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EnvironmentWorkerGroupRelation::getEnvironmentCode,
                            Collectors.mapping(EnvironmentWorkerGroupRelation::getWorkerGroup, Collectors.toList())));

            List<EnvironmentDto> dtoList = environmentIPage.getRecords().stream().map(environment -> {
                EnvironmentDto dto = new EnvironmentDto();
                BeanUtils.copyProperties(environment, dto);
                List<String> workerGroups = relationMap.getOrDefault(environment.getCode(), new ArrayList<String>());
                dto.setWorkerGroups(workerGroups);
                return dto;
            }).collect(Collectors.toList());

            pageInfo.setTotalList(dtoList);
        } else {
            pageInfo.setTotalList(new ArrayList<>());
        }

        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 查询用户有权限的所有环境列表，返回包含关联工作组信息的环境DTO。
     *
     * @param loginUser 当前登录用户
     * @return 包含环境DTO列表的结果Map
     */
    @Override
    public Map<String, Object> queryAllEnvironmentList(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ENVIRONMENT,
                loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.put(Constants.DATA_LIST, Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Environment> environmentList = environmentMapper.selectBatchIds(ids);
        if (CollectionUtils.isNotEmpty(environmentList)) {
            Map<Long, List<String>> relationMap = relationMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EnvironmentWorkerGroupRelation::getEnvironmentCode,
                            Collectors.mapping(EnvironmentWorkerGroupRelation::getWorkerGroup, Collectors.toList())));

            List<EnvironmentDto> dtoList = environmentList.stream().map(environment -> {
                EnvironmentDto dto = new EnvironmentDto();
                BeanUtils.copyProperties(environment, dto);
                List<String> workerGroups = relationMap.getOrDefault(environment.getCode(), new ArrayList<String>());
                dto.setWorkerGroups(workerGroups);
                return dto;
            }).collect(Collectors.toList());
            result.put(Constants.DATA_LIST, dtoList);
        } else {
            result.put(Constants.DATA_LIST, new ArrayList<>());
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 根据环境编码查询环境详情，包含关联的工作组列表。
     *
     * @param code 环境编码
     * @return 包含环境DTO的结果Map
     */
    @Override
    public Map<String, Object> queryEnvironmentByCode(Long code) {
        Map<String, Object> result = new HashMap<>();

        Environment env = environmentMapper.queryByEnvironmentCode(code);

        if (env == null) {
            putMsg(result, Status.QUERY_ENVIRONMENT_BY_CODE_ERROR, code);
        } else {
            List<String> workerGroups = relationMapper.queryByEnvironmentCode(env.getCode()).stream()
                    .map(item -> item.getWorkerGroup())
                    .collect(Collectors.toList());

            EnvironmentDto dto = new EnvironmentDto();
            BeanUtils.copyProperties(env, dto);
            dto.setWorkerGroups(workerGroups);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * 根据环境名称查询环境详情，包含关联的工作组列表。
     *
     * @param name 环境名称
     * @return 包含环境DTO的结果Map
     */
    @Override
    public Map<String, Object> queryEnvironmentByName(String name) {
        Map<String, Object> result = new HashMap<>();

        Environment env = environmentMapper.queryByEnvironmentName(name);
        if (env == null) {
            putMsg(result, Status.QUERY_ENVIRONMENT_BY_NAME_ERROR, name);
        } else {
            List<String> workerGroups = relationMapper.queryByEnvironmentCode(env.getCode()).stream()
                    .map(item -> item.getWorkerGroup())
                    .collect(Collectors.toList());

            EnvironmentDto dto = new EnvironmentDto();
            BeanUtils.copyProperties(env, dto);
            dto.setWorkerGroups(workerGroups);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * 根据编码删除环境。删除前检查是否有关联的任务定义，同时删除关联的工作组关系。
     *
     * @param loginUser 当前登录用户
     * @param code 环境编码
     * @return 包含删除结果的结果Map
     */
    @Transactional
    @Override
    public Map<String, Object> deleteEnvironmentByCode(User loginUser, Long code) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ENVIRONMENT, ENVIRONMENT_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Long relatedTaskNumber = taskDefinitionMapper
                .selectCount(new QueryWrapper<TaskDefinition>().lambda().eq(TaskDefinition::getEnvironmentCode, code));

        if (relatedTaskNumber > 0) {
            putMsg(result, Status.DELETE_ENVIRONMENT_RELATED_TASK_EXISTS);
            return result;
        }

        int delete = environmentMapper.deleteByCode(code);
        if (delete > 0) {
            relationMapper.delete(new QueryWrapper<EnvironmentWorkerGroupRelation>()
                    .lambda()
                    .eq(EnvironmentWorkerGroupRelation::getEnvironmentCode, code));
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_ENVIRONMENT_ERROR);
        }
        return result;
    }

    /**
     * 更新环境信息。同步更新工作组的关联关系，删除不再关联的工作组并添加新增的工作组。
     *
     * @param loginUser 当前登录用户
     * @param code 环境编码
     * @param name 新的环境名称
     * @param config 新的环境配置
     * @param desc 新的描述信息
     * @param workerGroups 新的工作组列表（JSON数组格式）
     * @return 包含更新结果的结果Map
     */
    @Transactional
    @Override
    public Map<String, Object> updateEnvironmentByCode(User loginUser, Long code, String name, String config,
                                                       String desc, String workerGroups) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ENVIRONMENT, ENVIRONMENT_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Map<String, Object> checkResult = checkParams(name, config, workerGroups);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }
        if (checkDescriptionLength(desc)) {
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(name);
        if (environment != null && !environment.getCode().equals(code)) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, name);
            return result;
        }

        Set<String> workerGroupSet;
        if (!StringUtils.isEmpty(workerGroups)) {
            workerGroupSet = JSONUtils.parseObject(workerGroups, new TypeReference<Set<String>>() {
            });
        } else {
            workerGroupSet = new TreeSet<>();
        }

        Set<String> existWorkerGroupSet = relationMapper
                .queryByEnvironmentCode(code)
                .stream()
                .map(item -> item.getWorkerGroup())
                .collect(Collectors.toSet());

        Set<String> deleteWorkerGroupSet = SetUtils.difference(existWorkerGroupSet, workerGroupSet).toSet();
        Set<String> addWorkerGroupSet = SetUtils.difference(workerGroupSet, existWorkerGroupSet).toSet();

        // verify whether the relation of this environment and worker groups can be adjusted
        checkResult = checkUsedEnvironmentWorkerGroupRelation(deleteWorkerGroupSet, name, code);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Environment env = new Environment();
        env.setCode(code);
        env.setName(name);
        env.setConfig(config);
        env.setDescription(desc);
        env.setOperator(loginUser.getId());
        env.setUpdateTime(new Date());

        int update =
                environmentMapper.update(env, new UpdateWrapper<Environment>().lambda().eq(Environment::getCode, code));
        if (update > 0) {
            deleteWorkerGroupSet.stream().forEach(key -> {
                if (StringUtils.isNotEmpty(key)) {
                    relationMapper.delete(new QueryWrapper<EnvironmentWorkerGroupRelation>()
                            .lambda()
                            .eq(EnvironmentWorkerGroupRelation::getEnvironmentCode, code)
                            .eq(EnvironmentWorkerGroupRelation::getWorkerGroup, key));
                }
            });
            addWorkerGroupSet.stream().forEach(key -> {
                if (StringUtils.isNotEmpty(key)) {
                    EnvironmentWorkerGroupRelation relation = new EnvironmentWorkerGroupRelation();
                    relation.setEnvironmentCode(code);
                    relation.setWorkerGroup(key);
                    relation.setUpdateTime(new Date());
                    relation.setCreateTime(new Date());
                    relation.setOperator(loginUser.getId());
                    relationMapper.insert(relation);
                }
            });
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.UPDATE_ENVIRONMENT_ERROR, name);
        }
        return result;
    }

    /**
     * 验证环境名称是否可用（未被占用）。
     *
     * @param environmentName 环境名称
     * @return 包含验证结果的结果Map
     */
    @Override
    public Map<String, Object> verifyEnvironment(String environmentName) {
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isEmpty(environmentName)) {
            putMsg(result, Status.ENVIRONMENT_NAME_IS_NULL);
            return result;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(environmentName);
        if (environment != null) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, environmentName);
            return result;
        }

        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    /**
     * 检查待删除的工作组关联是否被任务定义引用，若引用则不允许删除。
     *
     * @param deleteKeySet 待删除的工作组集合
     * @param environmentName 环境名称
     * @param environmentCode 环境编码
     * @return 包含校验结果的结果Map
     */
    private Map<String, Object> checkUsedEnvironmentWorkerGroupRelation(Set<String> deleteKeySet,
                                                                        String environmentName, Long environmentCode) {
        Map<String, Object> result = new HashMap<>();
        for (String workerGroup : deleteKeySet) {
            List<TaskDefinition> taskDefinitionList = taskDefinitionMapper
                    .selectList(new QueryWrapper<TaskDefinition>().lambda()
                            .eq(TaskDefinition::getEnvironmentCode, environmentCode)
                            .eq(TaskDefinition::getWorkerGroup, workerGroup));

            if (Objects.nonNull(taskDefinitionList) && taskDefinitionList.size() != 0) {
                Set<String> collect =
                        taskDefinitionList.stream().map(TaskDefinition::getName).collect(Collectors.toSet());
                putMsg(result, Status.UPDATE_ENVIRONMENT_WORKER_GROUP_RELATION_ERROR, workerGroup, environmentName,
                        collect);
                return result;
            }
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    /**
     * 校验环境名称、配置和工作组参数是否合法。
     *
     * @param name 环境名称
     * @param config 环境配置
     * @param workerGroups 工作组列表（JSON格式）
     * @return 包含校验状态的结果Map
     */
    public Map<String, Object> checkParams(String name, String config, String workerGroups) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(name)) {
            putMsg(result, Status.ENVIRONMENT_NAME_IS_NULL);
            return result;
        }
        if (StringUtils.isEmpty(config)) {
            putMsg(result, Status.ENVIRONMENT_CONFIG_IS_NULL);
            return result;
        }
        if (!StringUtils.isEmpty(workerGroups)) {
            List<String> workerGroupList = JSONUtils.parseObject(workerGroups, new TypeReference<List<String>>() {
            });
            if (Objects.isNull(workerGroupList)) {
                putMsg(result, Status.ENVIRONMENT_WORKER_GROUPS_IS_INVALID);
                return result;
            }
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

}
