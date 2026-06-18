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

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;

/**
 * 告警插件实例服务实现类。负责告警插件实例的增删改查、参数解析和关联检查，支持将插件参数在前端UI格式与后端存储格式之间转换。
 */
@Service
@Lazy
public class AlertPluginInstanceServiceImpl extends BaseServiceImpl implements AlertPluginInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceServiceImpl.class);

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * 创建告警插件实例。校验权限和实例名称唯一性，将插件参数转换为存储格式后持久化。
     *
     * @param loginUser 当前登录用户
     * @param pluginDefineId 插件定义ID
     * @param instanceName 实例名称
     * @param pluginInstanceParams 插件实例参数（UI格式）
     * @return 包含创建结果的结果Map
     */
    @Override
    public Map<String, Object> create(User loginUser, int pluginDefineId, String instanceName, String pluginInstanceParams) {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        alertPluginInstance.setPluginInstanceParams(paramsMapJson);
        alertPluginInstance.setInstanceName(instanceName);
        alertPluginInstance.setPluginDefineId(pluginDefineId);

        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.ALERT_PLUGIN_INSTANCE,ALART_INSTANCE_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (alertPluginInstanceMapper.existInstanceName(alertPluginInstance.getInstanceName()) == Boolean.TRUE) {
            logger.error("Plugin instance with the same name already exists, name:{}.",
                    alertPluginInstance.getInstanceName());
            putMsg(result, Status.PLUGIN_INSTANCE_ALREADY_EXISTS);
            return result;
        }

        int i = alertPluginInstanceMapper.insert(alertPluginInstance);
        if (i > 0) {
            result.put(Constants.DATA_LIST, alertPluginInstance);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        putMsg(result, Status.SAVE_ERROR);
        return result;
    }

    /**
     * 更新告警插件实例。校验权限后将更新的参数转换并保存。
     *
     * @param loginUser 当前登录用户
     * @param pluginInstanceId 插件实例ID
     * @param instanceName 新的实例名称
     * @param pluginInstanceParams 新的插件实例参数（UI格式）
     * @return 包含更新结果的结果Map
     */
    @Override
    public Map<String, Object> update(User loginUser, int pluginInstanceId, String instanceName, String pluginInstanceParams) {

        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(pluginInstanceId, paramsMapJson, instanceName, new Date());

        Map<String, Object> result = new HashMap<>();

        if (!canOperatorPermissions(loginUser,null, AuthorizationType.ALERT_PLUGIN_INSTANCE,ALERT_PLUGIN_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        int i = alertPluginInstanceMapper.updateById(alertPluginInstance);

        if (i > 0) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        putMsg(result, Status.SAVE_ERROR);
        return result;
    }

    /**
     * 删除告警插件实例。删除前检查是否有关联的告警组，有关联时不允许删除。
     *
     * @param loginUser 当前登录用户
     * @param id 插件实例ID
     * @return 包含删除结果的结果Map
     */
    @Override
    public Map<String, Object> delete(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        //check if there is an associated alert group
        boolean hasAssociatedAlertGroup = checkHasAssociatedAlertGroup(String.valueOf(id));
        if (hasAssociatedAlertGroup) {
            putMsg(result, Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED);
            return result;
        }
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.ALERT_PLUGIN_INSTANCE,ALERT_PLUGIN_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        int i = alertPluginInstanceMapper.deleteById(id);
        if (i > 0) {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * 根据ID获取告警插件实例详情。
     *
     * @param loginUser 当前登录用户
     * @param id 插件实例ID
     * @return 包含插件实例详情的结果Map
     */
    @Override
    public Map<String, Object> get(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        AlertPluginInstance alertPluginInstance = alertPluginInstanceMapper.selectById(id);
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.ALERT_PLUGIN_INSTANCE,ApiFuncIdentificationConstant.ALARM_INSTANCE_MANAGE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (null != alertPluginInstance) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, alertPluginInstance);
        }

        return result;
    }

    /**
     * 查询所有告警插件实例列表，返回包含插件名称和解析后参数的VO列表。
     *
     * @return 包含告警插件实例VO列表的结果Map
     */
    @Override
    public Map<String, Object> queryAll() {
        Map<String, Object> result = new HashMap<>();
        List<AlertPluginInstance> alertPluginInstances = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = buildPluginInstanceVOList(alertPluginInstances);
        if (null != alertPluginInstances) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, alertPluginInstanceVOS);
        }
        return result;
    }

    /**
     * 检查插件实例名称是否已存在。
     *
     * @param pluginInstanceName 插件实例名称
     * @return true表示已存在，false表示不存在
     */
    @Override
    public boolean checkExistPluginInstanceName(String pluginInstanceName) {
        return alertPluginInstanceMapper.existInstanceName(pluginInstanceName) == Boolean.TRUE;
    }

    /**
     * 分页查询告警插件实例列表，返回包含插件名称和解析后参数的VO分页数据。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索关键字
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页VO列表的结果对象
     */
    @Override
    public Result listPaging(User loginUser, String searchVal, int pageNo, int pageSize) {

        Result result = new Result();
        Page<AlertPluginInstance> page = new Page<>(pageNo, pageSize);
        IPage<AlertPluginInstance> alertPluginInstanceIPage = alertPluginInstanceMapper.queryByInstanceNamePage(page, searchVal);

        PageInfo<AlertPluginInstanceVO> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) alertPluginInstanceIPage.getTotal());
        pageInfo.setTotalList(buildPluginInstanceVOList(alertPluginInstanceIPage.getRecords()));
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<AlertPluginInstanceVO> buildPluginInstanceVOList(List<AlertPluginInstance> alertPluginInstances) {
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(alertPluginInstances)) {
            return alertPluginInstanceVOS;
        }
        List<PluginDefine> pluginDefineList = pluginDefineMapper.queryAllPluginDefineList();
        if (CollectionUtils.isEmpty(pluginDefineList)) {
            return alertPluginInstanceVOS;
        }
        Map<Integer, PluginDefine> pluginDefineMap =
                pluginDefineList.stream().collect(Collectors.toMap(PluginDefine::getId, Function.identity()));
        alertPluginInstances.forEach(alertPluginInstance -> {
            AlertPluginInstanceVO alertPluginInstanceVO = new AlertPluginInstanceVO();

            alertPluginInstanceVO.setCreateTime(alertPluginInstance.getCreateTime());
            alertPluginInstanceVO.setUpdateTime(alertPluginInstance.getUpdateTime());
            alertPluginInstanceVO.setPluginDefineId(alertPluginInstance.getPluginDefineId());
            alertPluginInstanceVO.setInstanceName(alertPluginInstance.getInstanceName());
            alertPluginInstanceVO.setId(alertPluginInstance.getId());
            PluginDefine pluginDefine = pluginDefineMap.get(alertPluginInstance.getPluginDefineId());
            //FIXME When the user removes the plug-in, this will happen. At this time, maybe we should add a new field to indicate that the plug-in has expired?
            if (null == pluginDefine) {
                return;
            }
            alertPluginInstanceVO.setAlertPluginName(pluginDefine.getPluginName());
            //todo List pages do not recommend returning this parameter
            String pluginParamsMapString = alertPluginInstance.getPluginInstanceParams();
            String uiPluginParams = parseToPluginUiParams(pluginParamsMapString, pluginDefine.getPluginParams());
            alertPluginInstanceVO.setPluginInstanceParams(uiPluginParams);
            alertPluginInstanceVOS.add(alertPluginInstanceVO);
        });
        return alertPluginInstanceVOS;

    }

    /**
     * 将前端UI格式的完整插件参数解析为后端存储的键值对JSON格式。
     *
     * @param pluginParams 完整参数（含UI配置）
     * @return 键值对格式的JSON字符串
     */
    private String parsePluginParamsMap(String pluginParams) {
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(pluginParams);
        return JSONUtils.toJsonString(paramsMap);
    }

    /**
     * 将后端存储的键值对参数与插件UI定义合并，生成前端UI格式的完整参数列表。
     *
     * @param pluginParamsMapString 后端键值对格式的JSON字符串
     * @param pluginUiParams 插件UI参数定义（含UI配置）
     * @return 前端UI格式的完整参数列表JSON字符串
     */
    private String parseToPluginUiParams(String pluginParamsMapString, String pluginUiParams) {
        List<Map<String, Object>> pluginParamsList = PluginParamsTransfer.generatePluginParams(pluginParamsMapString, pluginUiParams);
        return JSONUtils.toJsonString(pluginParamsList);
    }

    private boolean checkHasAssociatedAlertGroup(String id) {
        List<String> idsList = alertGroupMapper.queryInstanceIdsList();
        if (CollectionUtils.isEmpty(idsList)) {
            return false;
        }
        Optional<String> first = idsList.stream().filter(k -> null != k && Arrays.asList(k.split(",")).contains(id)).findFirst();
        return first.isPresent();
    }

}
