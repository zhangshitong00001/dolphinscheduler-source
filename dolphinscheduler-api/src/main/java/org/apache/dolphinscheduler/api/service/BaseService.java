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

package org.apache.dolphinscheduler.api.service;

import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;

/**
 * 基础服务接口。提供所有服务实现类的公共方法，包括管理员判断、权限校验、消息封装、日期解析等工具方法。
 * 各业务服务接口通过实现此接口获得统一的基础能力。
 */
public interface BaseService {

    /**
     * 判断用户是否为管理员。
     *
     * @param user 待判断的用户
     * @return 是管理员返回true，否则返回false
     */
    boolean isAdmin(User user);


    /**
     * 判断用户是否为非管理员，并将错误信息写入result。
     *
     * @param loginUser 登录用户
     * @param result    用于存放错误信息的结果Map
     * @return 是非管理员返回true，否则返回false
     */
    boolean isNotAdmin(User loginUser, Map<String, Object> result);

    /**
     * 权限后置处理，资源创建后将当前用户与资源关联。
     *
     * @param authorizationType 授权类型
     * @param userId            用户ID
     * @param ids               资源ID列表
     * @param logger            日志记录器
     */
    void permissionPostHandle(AuthorizationType authorizationType, Integer userId, List<Integer> ids, Logger logger);

    /**
     * 向Map结果中填充状态消息。
     *
     * @param result      结果Map
     * @param status      状态枚举
     * @param statusParams 状态消息参数
     */
    void putMsg(Map<String, Object> result, Status status, Object... statusParams);

    /**
     * 向Result对象中填充状态消息。
     *
     * @param result      结果对象
     * @param status      状态枚举
     * @param statusParams 状态消息参数
     */
    void putMsg(Result<Object> result, Status status, Object... statusParams);

    /**
     * 检查条件并根据结果向Map写入错误信息。
     *
     * @param result              结果Map
     * @param bool                条件判断值
     * @param userNoOperationPerm 无操作权限的状态码
     * @return 检查结果，条件为true时返回true
     */
    boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm);


    /**
     * 校验操作用户是否具备对资源的操作权限（基于创建者ID）。
     *
     * @param operateUser  操作用户
     * @param createUserId 资源创建者用户ID
     * @return 有操作权限返回true，否则返回false
     */
    boolean canOperator(User operateUser, int createUserId);

    /**
     * 校验操作用户是否具备对指定类型资源的操作权限。
     *
     * @param user 操作用户
     * @param ids  资源ID数组
     * @param type 授权类型
     * @param perm 权限键
     * @return 有操作权限返回true，否则返回false
     */
    boolean canOperatorPermissions(User user, Object[] ids, AuthorizationType type, String perm);

    /**
     * 检查并解析日期参数，验证日期格式和起止日期逻辑。
     *
     * @param startDateStr 开始日期字符串
     * @param endDateStr   结束日期字符串
     * @return 包含status、startDate和endDate的结果Map
     */
    Map<String, Object> checkAndParseDateParameters(String startDateStr, String endDateStr);

    /**
     * 检查描述文本的长度是否在允许范围内。
     *
     * @param description 描述文本
     * @return 长度合格返回true，否则返回false
     */
    boolean checkDescriptionLength(String description);

}
