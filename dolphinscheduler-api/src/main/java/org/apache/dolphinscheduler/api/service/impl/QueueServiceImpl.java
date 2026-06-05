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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.YARN_QUEUE_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.YARN_QUEUE_UPDATE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 队列服务实现类。负责YARN队列的增删改查、验证和权限管理，支持队列与用户的关联更新。
 */
@Service
public class QueueServiceImpl extends BaseServiceImpl implements QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceImpl.class);

    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 校验待创建的队列对象参数是否合法，包括队列值、队列名是否为空以及是否已存在。
     *
     * @param queue 待创建的队列对象
     * @throws ServiceException 参数校验不通过时抛出
     */
    private void createQueueValid(Queue queue) throws ServiceException {
        if (StringUtils.isEmpty(queue.getQueue())) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
        } else if (StringUtils.isEmpty(queue.getQueueName())) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
        } else if (checkQueueExist(queue.getQueue())) {
            throw new ServiceException(Status.QUEUE_VALUE_EXIST, queue.getQueue());
        } else if (checkQueueNameExist(queue.getQueueName())) {
            throw new ServiceException(Status.QUEUE_NAME_EXIST, queue.getQueueName());
        }
    }

    /**
     * 校验待更新的队列对象参数是否合法，包括存在性检查、变更必要性判断和重复性检查。
     *
     * @param existsQueue 已存在的队列对象
     * @param updateQueue 待更新的队列对象
     * @throws ServiceException 参数校验不通过时抛出
     */
    private void updateQueueValid(Queue existsQueue, Queue updateQueue) throws ServiceException {
        // Check the exists queue and the necessary of update operation, in not exist checker have to use updateQueue to avoid NPE
        if (Objects.isNull(existsQueue)) {
            throw new ServiceException(Status.QUEUE_NOT_EXIST, updateQueue.getQueue());
        } else if (Objects.equals(existsQueue, updateQueue)) {
            throw new ServiceException(Status.NEED_NOT_UPDATE_QUEUE);
        }
        // Check the update queue parameters
        else if (StringUtils.isEmpty(updateQueue.getQueue())) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
        } else if (StringUtils.isEmpty(updateQueue.getQueueName())) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
        } else if (!Objects.equals(updateQueue.getQueue(), existsQueue.getQueue()) && checkQueueExist(updateQueue.getQueue())) {
            throw new ServiceException(Status.QUEUE_VALUE_EXIST, updateQueue.getQueue());
        } else if (!Objects.equals(updateQueue.getQueueName(), existsQueue.getQueueName()) && checkQueueNameExist(updateQueue.getQueueName())) {
            throw new ServiceException(Status.QUEUE_NAME_EXIST, updateQueue.getQueueName());
        }
    }

    /**
     * 查询用户有权限的队列列表。根据用户权限过滤，普通用户额外包含默认队列。
     *
     * @param loginUser 当前登录用户
     * @return 包含队列列表的结果对象
     */
    @Override
    public Result queryList(User loginUser) {
        Result result = new Result();
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE, loginUser.getId(), logger);
        if (loginUser.getUserType().equals(UserType.GENERAL_USER)) {
            ids = ids.isEmpty() ? new HashSet<>() : ids;
            ids.add(Constants.DEFAULT_QUEUE_ID);
        }
        List<Queue> queueList = queueMapper.selectBatchIds(ids);
        result.setData(queueList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 分页查询队列列表，支持关键字搜索和权限过滤。
     *
     * @param loginUser 当前登录用户
     * @param searchVal 搜索关键字
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 包含分页队列列表的结果对象
     */
    @Override
    public Result queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        PageInfo<Queue> pageInfo = new PageInfo<>(pageNo, pageSize);
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE, loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        Page<Queue> page = new Page<>(pageNo, pageSize);
        IPage<Queue> queueList = queueMapper.queryQueuePaging(page, new ArrayList<>(ids), searchVal);
        Integer count = (int) queueList.getTotal();
        pageInfo.setTotal(count);
        pageInfo.setTotalList(queueList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 创建队列。校验权限和参数后插入新队列，并执行权限后置处理。
     *
     * @param loginUser 当前登录用户
     * @param queue 队列值（YARN队列路径）
     * @param queueName 队列名称
     * @return 包含创建的队列对象的结果对象
     */
    @Override
    @Transactional
    public Result createQueue(User loginUser, String queue, String queueName) {
        Result result = new Result();
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.QUEUE,YARN_QUEUE_CREATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        Queue queueObj = new Queue(queueName, queue);
        createQueueValid(queueObj);
        queueMapper.insert(queueObj);

        result.setData(queueObj);
        putMsg(result, Status.SUCCESS);
        permissionPostHandle(AuthorizationType.QUEUE, loginUser.getId(), Collections.singletonList(queueObj.getId()), logger);
        return result;
    }

    /**
     * 更新队列信息。更新队列值或名称时，同步更新关联用户的队列引用。
     *
     * @param loginUser 当前登录用户
     * @param id 队列ID
     * @param queue 新的队列值
     * @param queueName 新的队列名称
     * @return 包含更新后的队列对象的结果对象
     */
    @Override
    public Result updateQueue(User loginUser, int id, String queue, String queueName) {
        Result result = new Result();
        if (!canOperatorPermissions(loginUser,new Object[]{id}, AuthorizationType.QUEUE,YARN_QUEUE_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        Queue updateQueue = new Queue(id, queueName, queue);
        Queue existsQueue = queueMapper.selectById(id);
        updateQueueValid(existsQueue, updateQueue);

        // check old queue using by any user
        if (checkIfQueueIsInUsing(existsQueue.getQueueName(), updateQueue.getQueueName())) {
            //update user related old queue
            Integer relatedUserNums = userMapper.updateUserQueue(existsQueue.getQueueName(), updateQueue.getQueueName());
            logger.info("old queue have related {} user, exec update user success.", relatedUserNums);
        }

        queueMapper.updateById(updateQueue);
        result.setData(updateQueue);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 验证队列值和队列名称是否合法可用（不重复且格式正确）。
     *
     * @param queue 队列值
     * @param queueName 队列名称
     * @return 包含验证结果的结果对象
     */
    @Override
    public Result<Object> verifyQueue(String queue, String queueName) {
        Result<Object> result = new Result<>();

        Queue queueValidator = new Queue(queueName, queue);
        createQueueValid(queueValidator);
        result.setData(queueValidator);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 检查队列值是否已存在。
     *
     * @param queue 队列值
     * @return true表示已存在，false表示不存在
     */
    private boolean checkQueueExist(String queue) {
        return queueMapper.existQueue(queue, null) == Boolean.TRUE;
    }

    /**
     * 检查队列名称是否已存在。
     *
     * @param queueName 队列名称
     * @return true表示已存在，false表示不存在
     */
    private boolean checkQueueNameExist(String queueName) {
        return queueMapper.existQueue(null, queueName) == Boolean.TRUE;
    }

    /**
     * 检查旧队列名称是否被用户引用，且新旧名称不同时需要更新用户关联。
     *
     * @param oldQueue 旧队列名称
     * @param newQueue 新队列名称
     * @return true表示需要更新用户队列关联
     */
    private boolean checkIfQueueIsInUsing(String oldQueue, String newQueue) {
        return !oldQueue.equals(newQueue) && userMapper.existUser(oldQueue) == Boolean.TRUE;
    }

    /**
     * 确保指定队列存在，不存在则自动创建。仅供Python网关服务使用，不应在Web UI功能中调用。
     *
     * @param queue 队列值
     * @param queueName 队列名称
     * @return 已存在或新创建的队列对象
     */
    @Override
    public Queue createQueueIfNotExists(String queue, String queueName) {
        Queue existsQueue = queueMapper.queryQueueName(queue, queueName);
        if (!Objects.isNull(existsQueue)) {
            return existsQueue;
        }
        Queue queueObj = new Queue(queueName, queue);
        createQueueValid(queueObj);
        queueMapper.insert(queueObj);
        return queueObj;
    }

}
