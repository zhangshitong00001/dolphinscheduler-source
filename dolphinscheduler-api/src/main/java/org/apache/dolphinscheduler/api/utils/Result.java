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

package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.api.enums.Status;

import java.text.MessageFormat;

/**
 * 统一API响应结果封装类。用于封装接口调用的状态码、消息和数据，支持成功和错误响应的构建。
 *
 * @param <T> 数据类型
 */
public class Result<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Result(Status status) {
        if (status != null) {
            this.code = status.getCode();
            this.msg = status.getMsg();
        }
    }
    
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 创建成功响应结果。包含成功状态码和数据。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功Result对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), data);
    }
    
    public static Result success() {
        return success(null);
    }

    public boolean isSuccess() {
        return this.isStatus(Status.SUCCESS);
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

    public boolean isStatus(Status status) {
        return this.code != null && this.code.equals(status.getCode());
    }

    /**
     * 创建错误响应结果。使用预定义的Status枚举构建。
     *
     * @param status 状态枚举
     * @param <T>    数据类型
     * @return 错误Result对象
     */
    public static <T> Result<T> error(Status status) {
        return new Result<>(status);
    }

    /**
     * 创建带格式化参数的错误响应结果。使用MessageFormat格式化状态消息。
     *
     * @param status 状态枚举
     * @param args   消息格式化参数
     * @param <T>    数据类型
     * @return 错误Result对象
     */
    public static <T> Result<T> errorWithArgs(Status status, Object... args) {
        return new Result<>(status.getCode(), MessageFormat.format(status.getMsg(), args));
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Status{"
                + "code='" + code
                + '\'' + ", msg='"
                + msg + '\''
                + ", data=" + data
                + '}';
    }

    public Boolean checkResult() {
        return this.code == Status.SUCCESS.getCode();
    }
}
