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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.k8s.K8sClientService;
import org.apache.dolphinscheduler.api.service.K8sNamespaceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * K8s命名空间服务实现类。负责K8s命名空间的增删改查和资源配额管理，支持在K8s集群上创建、更新和删除命名空间并生成资源配额YAML。
 */
@Service
public class K8SNamespaceServiceImpl extends BaseServiceImpl implements K8sNamespaceService {

    private static final Logger logger = LoggerFactory.getLogger(K8SNamespaceServiceImpl.class);

    private static String resourceYaml = "apiVersion: v1\n"
            + "kind: ResourceQuota\n"
            + "metadata:\n"
            + "  name: ${name}\n"
            + "  namespace: ${namespace}\n"
            + "spec:\n"
            + "  hard:\n"
            + "    ${limitCpu}\n"
            + "    ${limitMemory}\n";

    @Autowired
    private K8sNamespaceMapper k8sNamespaceMapper;

    @Autowired
    private K8sClientService k8sClientService;

    @Autowired
    private ClusterMapper clusterMapper;

    /**
     * 分页查询K8s命名空间列表。管理员专属功能。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param pageNo 页码
     * @param searchVal 搜索关键字
     * @param pageSize 每页大小
     * @return 包含分页命名空间列表的结果对象
     */
    @Override
    public Result queryListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Page<K8sNamespace> page = new Page<>(pageNo, pageSize);

        IPage<K8sNamespace> k8sNamespaceList = k8sNamespaceMapper.queryK8sNamespacePaging(page, searchVal);

        Integer count = (int) k8sNamespaceList.getTotal();
        PageInfo<K8sNamespace> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal(count);
        pageInfo.setTotalList(k8sNamespaceList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 创建K8s命名空间。先在K8s集群上创建命名空间和资源配额，再注册到数据库。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param namespace 命名空间名称
     * @param clusterCode K8s集群编码
     * @param limitsCpu CPU限制（可为null表示不限制）
     * @param limitsMemory 内存限制（可为null表示不限制）
     * @return 包含创建结果的结果Map
     */
    @Override
    public Map<String, Object> createK8sNamespace(User loginUser, String namespace, Long clusterCode, Double limitsCpu,
                                                  Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if (StringUtils.isEmpty(namespace)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (clusterCode == null) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.CLUSTER);
            return result;
        }

        if (limitsCpu != null && limitsCpu < 0.0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if (limitsMemory != null && limitsMemory < 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        if (checkNamespaceExistInDb(namespace, clusterCode)) {
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace, clusterCode);
            return result;
        }

        Cluster cluster = clusterMapper.queryByClusterCode(clusterCode);
        if (cluster == null) {
            putMsg(result, Status.CLUSTER_NOT_EXISTS, namespace, clusterCode);
            return result;
        }

        long code = 0L;
        try {
            code = CodeGenerateUtils.getInstance().genCode();
            cluster.setCode(code);
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Cluster code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating cluster code");
            return result;
        }

        K8sNamespace k8sNamespaceObj = new K8sNamespace();
        Date now = new Date();

        k8sNamespaceObj.setCode(code);
        k8sNamespaceObj.setNamespace(namespace);
        k8sNamespaceObj.setClusterCode(clusterCode);
        k8sNamespaceObj.setUserId(loginUser.getId());
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setPodReplicas(0);
        k8sNamespaceObj.setPodRequestCpu(0.0);
        k8sNamespaceObj.setPodRequestMemory(0);
        k8sNamespaceObj.setCreateTime(now);
        k8sNamespaceObj.setUpdateTime(now);

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(k8sNamespaceObj.getClusterCode())) {
            try {
                String yamlStr = genDefaultResourceYaml(k8sNamespaceObj);
                k8sClientService.upsertNamespaceAndResourceToK8s(k8sNamespaceObj, yamlStr);
            } catch (Exception e) {
                logger.error("namespace create to k8s error", e);
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, e.getMessage());
                return result;
            }
        }

        k8sNamespaceMapper.insert(k8sNamespaceObj);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 更新K8s命名空间的资源限制。同步更新数据库和K8s集群上的资源配额。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param id 命名空间记录ID
     * @param userName 所属用户名称
     * @param limitsCpu CPU限制
     * @param limitsMemory 内存限制
     * @return 包含更新结果的结果Map
     */
    @Override
    public Map<String, Object> updateK8sNamespace(User loginUser, int id, String userName, Double limitsCpu,
                                                  Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if (limitsCpu != null && limitsCpu < 0.0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if (limitsMemory != null && limitsMemory < 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            putMsg(result, Status.K8S_NAMESPACE_NOT_EXIST, id);
            return result;
        }

        Date now = new Date();
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setUpdateTime(now);

        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(k8sNamespaceObj.getClusterCode())) {
            try {
                String yamlStr = genDefaultResourceYaml(k8sNamespaceObj);
                k8sClientService.upsertNamespaceAndResourceToK8s(k8sNamespaceObj, yamlStr);
            } catch (Exception e) {
                logger.error("namespace update to k8s error", e);
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, e.getMessage());
                return result;
            }
        }
        // update to db
        k8sNamespaceMapper.updateById(k8sNamespaceObj);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 验证命名空间名称在指定集群中是否可用（未重复）。
     *
     * @param namespace 命名空间名称
     * @param clusterCode 集群编码
     * @return 包含验证结果的结果对象，已存在时返回错误
     */
    @Override
    public Result<Object> verifyNamespaceK8s(String namespace, Long clusterCode) {
        Result<Object> result = new Result<>();
        if (StringUtils.isEmpty(namespace)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (clusterCode == null) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.CLUSTER);
            return result;
        }

        if (checkNamespaceExistInDb(namespace, clusterCode)) {
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace, clusterCode);
            return result;
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 根据ID删除K8s命名空间。同时从K8s集群和数据库中移除。
     *
     * @param loginUser 当前登录用户（需为管理员）
     * @param id 命名空间记录ID
     * @return 包含删除结果的结果Map
     */
    @Override
    public Map<String, Object> deleteNamespaceById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            putMsg(result, Status.K8S_NAMESPACE_NOT_EXIST, id);
            return result;
        }
        if (!Constants.K8S_LOCAL_TEST_CLUSTER_CODE.equals(k8sNamespaceObj.getClusterCode())) {
            try {
                k8sClientService.deleteNamespaceToK8s(k8sNamespaceObj.getNamespace(), k8sNamespaceObj.getClusterCode());
            } catch (RemotingException e) {
                putMsg(result, Status.K8S_CLIENT_OPS_ERROR, id);
                return result;
            }
        }
        k8sNamespaceMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 检查命名空间在指定集群中是否已存在于数据库。
     *
     * @param namespace 命名空间名称
     * @param clusterCode 集群编码
     * @return true表示已存在，false表示不存在
     */
    private boolean checkNamespaceExistInDb(String namespace, Long clusterCode) {
        return k8sNamespaceMapper.existNamespace(namespace, clusterCode) == Boolean.TRUE;
    }

    /**
     * 根据命名空间的CPU和内存限制生成K8s ResourceQuota的YAML配置内容。
     *
     * @param k8sNamespace K8s命名空间实体
     * @return 资源配额YAML字符串
     */
    private String genDefaultResourceYaml(K8sNamespace k8sNamespace) {
        // resource use same name with namespace
        String name = k8sNamespace.getNamespace();
        String namespace = k8sNamespace.getNamespace();
        String cpuStr = null;
        if (k8sNamespace.getLimitsCpu() != null) {
            cpuStr = k8sNamespace.getLimitsCpu() + "";
        }

        String memoryStr = null;
        if (k8sNamespace.getLimitsMemory() != null) {
            memoryStr = k8sNamespace.getLimitsMemory() + "Gi";
        }

        String result = resourceYaml.replace("${name}", name)
                .replace("${namespace}", namespace);
        if (cpuStr == null) {
            result = result.replace("${limitCpu}", "");
        } else {
            result = result.replace("${limitCpu}", "limits.cpu: '" + cpuStr + "'");
        }

        if (memoryStr == null) {
            result = result.replace("${limitMemory}", "");
        } else {
            result = result.replace("${limitMemory}", "limits.memory: " + memoryStr);
        }
        return result;
    }

    /**
     * 查询用户未授权的命名空间列表。管理员或用户本人可查询。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @return 包含未授权命名空间列表的结果Map
     */
    @Override
    public Map<String, Object> queryUnauthorizedNamespace(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (loginUser.getId() != userId && isNotAdmin(loginUser, result)) {
            return result;
        }
        // query all namespace list,this auth does not like project
        List<K8sNamespace> namespaceList = k8sNamespaceMapper.selectList(null);
        List<K8sNamespace> resultList = new ArrayList<>();
        Set<K8sNamespace> namespaceSet;
        if (namespaceList != null && !namespaceList.isEmpty()) {
            namespaceSet = new HashSet<>(namespaceList);
            List<K8sNamespace> authedProjectList = k8sNamespaceMapper.queryAuthedNamespaceListByUserId(userId);
            resultList = getUnauthorizedNamespaces(namespaceSet, authedProjectList);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 查询用户已授权的命名空间列表。管理员或用户本人可查询。
     *
     * @param loginUser 当前登录用户
     * @param userId 目标用户ID
     * @return 包含已授权命名空间列表的结果Map
     */
    @Override
    public Map<String, Object> queryAuthorizedNamespace(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        if (loginUser.getId() != userId && isNotAdmin(loginUser, result)) {
            return result;
        }

        List<K8sNamespace> namespaces = k8sNamespaceMapper.queryAuthedNamespaceListByUserId(userId);
        result.put(Constants.DATA_LIST, namespaces);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 查询当前用户可用的命名空间列表。管理员返回所有，普通用户返回已授权的。
     *
     * @param loginUser 当前登录用户
     * @return 可用命名空间列表（含集群名称）
     */
    @Override
    public List<K8sNamespace> queryNamespaceAvailable(User loginUser) {
        List<K8sNamespace> k8sNamespaces;
        if (isAdmin(loginUser)) {
            k8sNamespaces = k8sNamespaceMapper.selectList(null);
        } else {
            k8sNamespaces = k8sNamespaceMapper.queryAuthedNamespaceListByUserId(loginUser.getId());
        }
        setClusterName(k8sNamespaces);
        return k8sNamespaces;
    }

    /**
     * 为命名空间列表设置对应的集群名称。
     *
     * @param k8sNamespaces 命名空间列表
     */
    private void setClusterName(List<K8sNamespace> k8sNamespaces) {
        if (CollectionUtils.isNotEmpty(k8sNamespaces)) {
            List<Cluster> clusters = clusterMapper.queryAllClusterList();
            if (CollectionUtils.isNotEmpty(clusters)) {
                Map<Long, String> codeNameMap = clusters.stream()
                        .collect(Collectors.toMap(Cluster::getCode, Cluster::getName, (a, b) -> a));
                for (K8sNamespace k8sNamespace : k8sNamespaces) {
                    String clusterName = codeNameMap.get(k8sNamespace.getClusterCode());
                    k8sNamespace.setClusterName(clusterName);
                }
            }
        }
    }

    /**
     * get unauthorized namespace
     *
     * @param namespaceSet        namespace set
     * @param authedNamespaceList authed namespace list
     * @return namespace list that authorization
     */
    private List<K8sNamespace> getUnauthorizedNamespaces(Set<K8sNamespace> namespaceSet,
                                                         List<K8sNamespace> authedNamespaceList) {
        List<K8sNamespace> resultList = new ArrayList<>();
        for (K8sNamespace k8sNamespace : namespaceSet) {
            boolean existAuth = false;
            if (authedNamespaceList != null && !authedNamespaceList.isEmpty()) {
                for (K8sNamespace k8sNamespaceAuth : authedNamespaceList) {
                    if (k8sNamespace.equals(k8sNamespaceAuth)) {
                        existAuth = true;
                    }
                }
            }

            if (!existAuth) {
                resultList.add(k8sNamespace);
            }
        }
        return resultList;
    }
}
