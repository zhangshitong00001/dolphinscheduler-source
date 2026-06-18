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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;
import static org.apache.dolphinscheduler.common.constants.Constants.HTTP_HEADER_UNKNOWN;
import static org.apache.dolphinscheduler.common.constants.Constants.HTTP_X_FORWARDED_FOR;
import static org.apache.dolphinscheduler.common.constants.Constants.HTTP_X_REAL_IP;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 基础控制器。提供分页参数校验、客户端IP获取、标准结果封装等通用功能方法，供所有Controller子类继承使用。
 */
public class BaseController {

    /**
     * 校验分页参数。
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 校验结果状态码
     */
    // todo: directly throw exception
    public Result checkPageParams(int pageNo, int pageSize) {
        Result result = new Result();
        Status resultEnum = Status.SUCCESS;
        String msg = Status.SUCCESS.getMsg();
        if (pageNo <= 0) {
            resultEnum = Status.REQUEST_PARAMS_NOT_VALID_ERROR;
            msg = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.PAGE_NUMBER);
        } else if (pageSize <= 0) {
            resultEnum = Status.REQUEST_PARAMS_NOT_VALID_ERROR;
            msg = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.PAGE_SIZE);
        }
        result.setCode(resultEnum.getCode());
        result.setMsg(msg);
        return result;
    }

    /**
     * 从HTTP请求中获取客户端IP地址。优先从X-Forwarded-For和X-Real-IP头获取，无代理时返回远程地址。
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader(HTTP_X_FORWARDED_FOR);

        if (StringUtils.isNotEmpty(clientIp) && !clientIp.equalsIgnoreCase(HTTP_HEADER_UNKNOWN)) {
            int index = clientIp.indexOf(COMMA);
            if (index != -1) {
                return clientIp.substring(0, index);
            } else {
                return clientIp;
            }
        }

        clientIp = request.getHeader(HTTP_X_REAL_IP);
        if (StringUtils.isNotEmpty(clientIp) && !clientIp.equalsIgnoreCase(HTTP_HEADER_UNKNOWN)) {
            return clientIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 返回数据列表。根据Service层返回的Map结果自动构建标准成功或错误响应。
     *
     * @param result 包含status和data的Map结果
     * @return 标准结果对象
     */
    public Result returnDataList(Map<String, Object> result) {
        Status status = (Status) result.get(Constants.STATUS);
        if (status == Status.SUCCESS) {
            String msg = Status.SUCCESS.getMsg();
            Object datalist = result.get(Constants.DATA_LIST);
            return success(msg, datalist);
        } else {
            Integer code = status.getCode();
            String msg = (String) result.get(Constants.MSG);
            return error(code, msg);
        }
    }

    /**
     * 返回成功结果（无数据）。
     *
     * @return 成功结果对象
     */
    public Result success() {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());

        return result;
    }

    /**
     * 返回成功结果，携带自定义消息。
     *
     * @param msg 成功消息
     * @return 成功结果对象
     */
    public Result success(String msg) {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(msg);

        return result;
    }

    /**
     * 返回成功结果，携带消息和数据列表（不分页）。
     *
     * @param msg 成功消息
     * @param list 数据列表
     * @return 成功结果对象
     */
    public Result success(String msg, Object list) {
        return getResult(msg, list);
    }

    /**
     * 返回成功结果，携带对象数据（不分页）。
     *
     * @param list 数据对象
     * @return 成功结果对象
     */
    public Result success(Object list) {
        return getResult(Status.SUCCESS.getMsg(), list);
    }

    /**
     * 返回成功结果，携带Map格式的数据。
     *
     * @param msg 成功消息
     * @param object Map格式数据
     * @return 成功结果对象
     */
    public Result success(String msg, Map<String, Object> object) {
        return getResult(msg, object);
    }

    /**
     * 返回分页数据。包含数据列表、当前页码、总记录数和总页数。
     *
     * @param totalList 数据列表
     * @param currentPage 当前页码
     * @param total 总记录数
     * @param totalPage 总页数
     * @return 成功结果对象
     */
    public Result success(Object totalList, Integer currentPage,
                          Integer total, Integer totalPage) {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.SUCCESS.getMsg());

        Map<String, Object> map = new HashMap<>(8);
        map.put(Constants.TOTAL_LIST, totalList);
        map.put(Constants.CURRENT_PAGE, currentPage);
        map.put(Constants.TOTAL_PAGE, totalPage);
        map.put(Constants.TOTAL, total);
        result.setData(map);
        return result;
    }

    /**
     * 返回错误结果。
     *
     * @param code 错误码
     * @param msg 错误消息
     * @return 错误结果对象
     */
    public Result error(Integer code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 将状态信息放入Map结果中。
     *
     * @param result 结果Map
     * @param status 状态枚举
     * @param statusParams 状态参数（用于消息格式化）
     */
    protected void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    /**
     * 将状态信息放入Result对象中。
     *
     * @param result Result对象
     * @param status 状态枚举
     * @param statusParams 状态参数（用于消息格式化）
     */
    protected void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());

        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }

    }

    /**
     * get result
     *
     * @param msg message
     * @param list object list
     * @return result code
     */
    private Result getResult(String msg, Object list) {
        Result result = new Result();
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(msg);

        result.setData(list);
        return result;
    }
}
