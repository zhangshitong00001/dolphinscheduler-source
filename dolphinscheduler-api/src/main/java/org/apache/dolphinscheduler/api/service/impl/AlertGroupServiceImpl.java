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
import org.apache.dolphinscheduler.api.service.AlertGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;

/**
 * 告警组服务实现类。负责告警组的增删改查和权限管理，告警组关联告警插件实例，用于任务告警通知的分组管理。
 */
@Service
public class AlertGroupServiceImpl extends BaseServiceImpl implements AlertGroupService {

    private Logger logger = LoggerFactory.getLogger(AlertGroupServiceImpl.class);

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * 查询告警组列表。管理员可查看所有告警组，普通用户仅查看有权限的告警组。
     *
     * @param loginUser 当前登录用户
     * @return 包含告警组列表的结果Map
     */
    @Override
    public Map<String, Object> queryAlertgroup(User loginUser) {
        HashMap<String, Object> result = new HashMap<>();
        List<AlertGroup> alertGroups;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            alertGroups = alertGroupMapper.queryAllGroupList();
        } else {
            Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP, loginUser.getId(), logger);
            if (ids.isEmpty()) {
                result.put(Constants.DATA_LIST, Collections.emptyList());
                putMsg(result, Status.SUCCESS);
                return result;
            }
            alertGroups = alertGroupMapper.selectBatchIds(ids);
        }
        result.put(Constants.DATA_LIST, alertGroups);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 根据ID查询告警组详情，需要告警组查看权限。
     *
     * @param loginUser 当前登录用户
     * @param id 告警组ID
     * @return 包含告警组详情的结果Map
     */
    @Override
    public Map<String, Object> queryAlertGroupById(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (!canOperatorPermissions(loginUser,new Object[]{id}, AuthorizationType.ALERT_GROUP,ALERT_GROUP_VIEW)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        //check if exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;
        }
        result.put("data", alertGroup);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 分页查询告警组列表，支持搜索过滤和权限控制。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索关键字
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页告警组列表的结果对象
     */
    @Override
    public Result listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        Result result = new Result();
        IPage<AlertGroup> alertGroupPage;
        PageInfo<AlertGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<AlertGroup> page = new Page<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            alertGroupPage = alertGroupMapper.queryAlertGroupPage(page, searchVal);
        } else {
            Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.ALERT_GROUP, loginUser.getId(), logger);
            if (ids.isEmpty()) {
                result.setData(pageInfo);
                putMsg(result, Status.SUCCESS);
                return result;
            }
            alertGroupPage = alertGroupMapper.queryAlertGroupPageByIds(page, new ArrayList<>(ids), searchVal);
        }
        pageInfo.setTotal((int) alertGroupPage.getTotal());
        pageInfo.setTotalList(alertGroupPage.getRecords());
        result.setData(pageInfo);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 创建告警组。需要告警组创建权限，组名不能重复，创建后执行权限后置处理。
     *
     * @param loginUser 当前登录用户
     * @param groupName 告警组名称
     * @param desc 描述信息
     * @param alertInstanceIds 告警插件实例ID列表（逗号分隔）
     * @return 包含创建结果的结果Map
     */
    @Override
    @Transactional
    public Map<String, Object> createAlertgroup(User loginUser, String groupName, String desc, String alertInstanceIds) {
        Map<String, Object> result = new HashMap<>();
        //only admin can operate
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.ALERT_GROUP, ALERT_GROUP_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if(checkDescriptionLength(desc)){
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        AlertGroup alertGroup = new AlertGroup();
        Date now = new Date();

        alertGroup.setGroupName(groupName);
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        alertGroup.setDescription(desc);
        alertGroup.setCreateTime(now);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());

        // insert
        try {
            int insert = alertGroupMapper.insert(alertGroup);
            if (insert > 0) {
                result.put(Constants.DATA_LIST, alertGroup);
                putMsg(result, Status.SUCCESS);
                permissionPostHandle(AuthorizationType.ALERT_GROUP, loginUser.getId(), Collections.singletonList(alertGroup.getId()), logger);
            } else {
                putMsg(result, Status.CREATE_ALERT_GROUP_ERROR);
            }
        } catch (DuplicateKeyException ex) {
            logger.error("Create alert group error.", ex);
            putMsg(result, Status.ALERT_GROUP_EXIST);
        }

        return result;
    }

    /**
     * 更新告警组信息。需要告警组更新权限，更新时检查组名唯一性。
     *
     * @param loginUser 当前登录用户
     * @param id 告警组ID
     * @param groupName 新的告警组名称
     * @param desc 新的描述信息
     * @param alertInstanceIds 新的告警插件实例ID列表
     * @return 包含更新结果的结果Map
     */
    @Override
    public Map<String, Object> updateAlertgroup(User loginUser, int id, String groupName, String desc, String alertInstanceIds) {
        Map<String, Object> result = new HashMap<>();

        if (!canOperatorPermissions(loginUser, new Object[]{id},AuthorizationType.ALERT_GROUP,ALERT_GROUP_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if(checkDescriptionLength(desc)){
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        AlertGroup alertGroup = alertGroupMapper.selectById(id);

        if (alertGroup == null) {
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;

        }

        Date now = new Date();

        if (!StringUtils.isEmpty(groupName)) {
            alertGroup.setGroupName(groupName);
        }
        alertGroup.setDescription(desc);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        try {
            alertGroupMapper.updateById(alertGroup);
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            logger.error("Update alert group error.", ex);
            putMsg(result, Status.ALERT_GROUP_EXIST);
        }
        return result;
    }

    /**
     * 根据ID删除告警组。需要告警组删除权限，不允许删除默认告警组（ID=1）。
     *
     * @param loginUser 当前登录用户
     * @param id 告警组ID
     * @return 包含删除结果的结果Map
     */
    @Override
    @Transactional
    public Map<String, Object> delAlertgroupById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (!canOperatorPermissions(loginUser,new Object[]{id}, AuthorizationType.ALERT_GROUP,ALERT_GROUP_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // Not allow to delete the default alarm group ,because the module of service need to use it.
        if (id == 1) {
            putMsg(result, Status.NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP);
            return result;
        }

        //check exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            putMsg(result, Status.ALERT_GROUP_NOT_EXIST);
            return result;
        }

        alertGroupMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 检查告警组名称是否已存在。
     *
     * @param groupName 告警组名称
     * @return true表示已存在，false表示不存在
     */
    @Override
    public boolean existGroupName(String groupName) {
        return alertGroupMapper.existGroupName(groupName) == Boolean.TRUE;
    }
}
