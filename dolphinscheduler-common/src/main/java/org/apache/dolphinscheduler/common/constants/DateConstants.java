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

package org.apache.dolphinscheduler.common.constants;

/**
 * 日期常量类。
 * 定义系统中使用的日期格式化模板、内置时间参数名以及时间函数关键字。
 * 这些常量用于时间参数替换和Cron表达式生成。
 */
public class DateConstants {

    // ==================== 日期时间格式化模板 ====================

    /** 标准日期时间格式: yyyy-MM-dd HH:mm:ss */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    /** 年月日格式: yyyyMMdd */
    public static final String YYYYMMDD = "yyyyMMdd";
    /** 年月日时分秒格式: yyyyMMddHHmmss */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    /** 年月日时分秒毫秒格式: yyyyMMddHHmmssSSS */
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

    // ==================== 参数格式化常量 ====================

    /** 日期参数格式: yyyyMMdd */
    public static final String PARAMETER_FORMAT_DATE = "yyyyMMdd";
    /** 时间参数格式: yyyyMMddHHmmss */
    public static final String PARAMETER_FORMAT_TIME = "yyyyMMddHHmmss";

    // ==================== 内置系统时间参数名 ====================

    /** 系统日期时间参数: 当前日期时间(yyyyMMddHHmmss) */
    public static final String PARAMETER_DATETIME = "system.datetime";
    /** 系统业务当前日期: 今天(yyyyMMdd) */
    public static final String PARAMETER_CURRENT_DATE = "system.biz.curdate";
    /** 系统业务日期: 昨天(yyyyMMdd)，通常等于T-1日 */
    public static final String PARAMETER_BUSINESS_DATE = "system.biz.date";

    // ==================== 自定义时间函数关键字 ====================

    /** 月初函数: 返回传入日期的月初日期 */
    public static final String MONTH_BEGIN = "month_begin";
    /** 月份加法函数: 对传入日期增加月份 */
    public static final String ADD_MONTHS = "add_months";
    /** 月末函数: 返回传入日期的月末日期 */
    public static final String MONTH_END = "month_end";
    /** 周初函数: 返回传入日期所在周的周一 */
    public static final String WEEK_BEGIN = "week_begin";
    /** 周末函数: 返回传入日期所在周的周日 */
    public static final String WEEK_END = "week_end";
    /** 时间戳函数: 返回Unix时间戳 */
    public static final String TIMESTAMP = "timestamp";
}
