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

import org.apache.dolphinscheduler.api.dto.ClusterDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.k8s.K8sManager;
import org.apache.dolphinscheduler.api.service.ClusterService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 集群服务实现类。负责集群（K8s/YARN等）的增删改查、参数校验和K8s客户端管理，支持集群与命名空间的关联检查。
 */
@Service
public class ClusterServiceImpl extends BaseServiceImpl implements ClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterServiceImpl.class);

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private K8sManager k8sManager;

    @Autowired
    private K8sNamespaceMapper k8sNamespaceMapper;
    /**
     * 创建集群。管理员专属操作，自动生成集群编码并写入数据库。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param name 集群名称
     * @param config 集群配置（如K8s kubeconfig）
     * @param desc 集群描述
     * @return 包含新集群编码的结果Map
     */
    @Transactional
    @Override
    public Map<String, Object> createCluster(User loginUser, String name, String config, String desc) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Map<String, Object> checkResult = checkParams(name, config);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Cluster clusterExistByName = clusterMapper.queryByClusterName(name);
        if (clusterExistByName != null) {
            putMsg(result, Status.CLUSTER_NAME_EXISTS, name);
            return result;
        }

        Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setConfig(config);
        cluster.setDescription(desc);
        cluster.setOperator(loginUser.getId());
        cluster.setCreateTime(new Date());
        cluster.setUpdateTime(new Date());
        long code = 0L;
        try {
            code = CodeGenerateUtils.getInstance().genCode();
            cluster.setCode(code);
        } catch (CodeGenerateException e) {
            logger.error("Cluster code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating cluster code");
            return result;
        }

        if (clusterMapper.insert(cluster) > 0) {
            result.put(Constants.DATA_LIST, cluster.getCode());
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_CLUSTER_ERROR);
        }
        return result;
    }

    /**
     * 分页查询集群列表，支持搜索过滤。
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param searchVal 搜索关键字
     * @return 包含分页集群DTO列表的结果对象
     */
    @Override
    public Result queryClusterListPaging(Integer pageNo, Integer pageSize, String searchVal) {
        Result result = new Result();

        Page<Cluster> page = new Page<>(pageNo, pageSize);

        IPage<Cluster> clusterIPage = clusterMapper.queryClusterListPaging(page, searchVal);

        PageInfo<ClusterDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) clusterIPage.getTotal());

        if (CollectionUtils.isNotEmpty(clusterIPage.getRecords())) {

            List<ClusterDto> dtoList = clusterIPage.getRecords().stream().map(cluster -> {
                ClusterDto dto = new ClusterDto();
                BeanUtils.copyProperties(cluster, dto);
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
     * 查询所有集群列表，返回集群DTO列表。
     *
     * @return 包含所有集群DTO列表的结果Map
     */
    @Override
    public Map<String, Object> queryAllClusterList() {
        Map<String, Object> result = new HashMap<>();
        List<Cluster> clusterList = clusterMapper.queryAllClusterList();

        if (CollectionUtils.isNotEmpty(clusterList)) {

            List<ClusterDto> dtoList = clusterList.stream().map(cluster -> {
                ClusterDto dto = new ClusterDto();
                BeanUtils.copyProperties(cluster, dto);
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
     * 根据集群编码查询集群详情。
     *
     * @param code 集群编码
     * @return 包含集群DTO的结果Map，不存在时返回错误信息
     */
    @Override
    public Map<String, Object> queryClusterByCode(Long code) {
        Map<String, Object> result = new HashMap<>();

        Cluster cluster = clusterMapper.queryByClusterCode(code);

        if (cluster == null) {
            putMsg(result, Status.QUERY_CLUSTER_BY_CODE_ERROR, code);
        } else {

            ClusterDto dto = new ClusterDto();
            BeanUtils.copyProperties(cluster, dto);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * 根据集群名称查询集群详情。
     *
     * @param name 集群名称
     * @return 包含集群DTO的结果Map，不存在时返回错误信息
     */
    @Override
    public Map<String, Object> queryClusterByName(String name) {
        Map<String, Object> result = new HashMap<>();

        Cluster cluster = clusterMapper.queryByClusterName(name);
        if (cluster == null) {
            putMsg(result, Status.QUERY_CLUSTER_BY_NAME_ERROR, name);
        } else {

            ClusterDto dto = new ClusterDto();
            BeanUtils.copyProperties(cluster, dto);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * 根据编码删除集群。管理员专属操作，会检查是否有关联的命名空间。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param code 集群编码
     * @return 包含删除结果的结果Map
     */
    @Transactional
    @Override
    public Map<String, Object> deleteClusterByCode(User loginUser, Long code) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Long relatedNamespaceNumber = k8sNamespaceMapper
                .selectCount(new QueryWrapper<K8sNamespace>().lambda().eq(K8sNamespace::getClusterCode, code));

        if (relatedNamespaceNumber > 0) {
            putMsg(result, Status.DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS);
            return result;
        }

        int delete = clusterMapper.deleteByCode(code);
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_CLUSTER_ERROR);
        }
        return result;
    }

    /**
     * 更新集群信息。配置变更时自动更新K8s客户端连接。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param code 集群编码
     * @param name 新的集群名称
     * @param config 新的集群配置
     * @param desc 新的集群描述
     * @return 包含更新结果的结果Map
     */
    @Transactional
    @Override
    public Map<String, Object> updateClusterByCode(User loginUser, Long code, String name, String config, String desc) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if (checkDescriptionLength(desc)) {
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }

        Map<String, Object> checkResult = checkParams(name, config);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Cluster clusterExistByName = clusterMapper.queryByClusterName(name);
        if (clusterExistByName != null && !clusterExistByName.getCode().equals(code)) {
            putMsg(result, Status.CLUSTER_NAME_EXISTS, name);
            return result;
        }

        Cluster clusterExist = clusterMapper.queryByClusterCode(code);
        if (clusterExist == null) {
            putMsg(result, Status.CLUSTER_NOT_EXISTS, name);
            return result;
        }

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(clusterExist.getCode())
                && !config.equals(ClusterConfUtils.getK8sConfig(clusterExist.getConfig()))) {
            try {
                k8sManager.getAndUpdateK8sClient(code, true);
            } catch (RemotingException e) {
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, name);
                return result;
            }
        }

        // update cluster
        clusterExist.setConfig(config);
        clusterExist.setName(name);
        clusterExist.setDescription(desc);
        clusterMapper.updateById(clusterExist);
        // need not update relation

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 验证集群名称是否可用（未被占用）。
     *
     * @param clusterName 集群名称
     * @return 包含验证结果的结果Map，名称已存在时返回错误信息
     */
    @Override
    public Map<String, Object> verifyCluster(String clusterName) {
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isEmpty(clusterName)) {
            putMsg(result, Status.CLUSTER_NAME_IS_NULL);
            return result;
        }

        Cluster cluster = clusterMapper.queryByClusterName(clusterName);
        if (cluster != null) {
            putMsg(result, Status.CLUSTER_NAME_EXISTS, clusterName);
            return result;
        }

        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    /**
     * 校验集群名称和配置参数是否为空。
     *
     * @param name 集群名称
     * @param config 集群配置
     * @return 包含校验状态的结果Map
     */
    public Map<String, Object> checkParams(String name, String config) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(name)) {
            putMsg(result, Status.CLUSTER_NAME_IS_NULL);
            return result;
        }
        if (StringUtils.isEmpty(config)) {
            putMsg(result, Status.CLUSTER_CONFIG_IS_NULL);
            return result;
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

}
