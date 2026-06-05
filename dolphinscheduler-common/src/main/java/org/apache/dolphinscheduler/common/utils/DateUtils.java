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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.thread.ThreadLocalContext;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日期时间工具类，提供日期格式化、解析、转换和计算等常用方法。
 * 支持时区感知的日期操作，包含毫秒、秒、分钟、小时、天等单位换算的内部常量。
 * 该类为工具类，不可实例化。
 */
public final class DateUtils {

    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;
    static final long C6 = C5 * 24L;

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
    private static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS =
            DateTimeFormatter.ofPattern(DateConstants.YYYY_MM_DD_HH_MM_SS);

    private DateUtils() {
        throw new UnsupportedOperationException("Construct DateUtils");
    }

    /**
     * date to local datetime
     *
     * @param date date
     * @return local datetime
     */
    private static LocalDateTime date2LocalDateTime(Date date) {
        String timezone = ThreadLocalContext.getTimezoneThreadLocal().get();
        ZoneId zoneId = StringUtils.isNotEmpty(timezone) ? ZoneId.of(timezone) : ZoneId.systemDefault();
        return date2LocalDateTime(date, zoneId);
    }

    /**
     * date to local datetime
     *
     * @param date   date
     * @param zoneId zoneId
     * @return local datetime
     */
    private static LocalDateTime date2LocalDateTime(Date date, ZoneId zoneId) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }

    /**
     * local datetime to date
     *
     * @param localDateTime local datetime
     * @return date
     */
    private static Date localDateTime2Date(LocalDateTime localDateTime) {
        String timezone = ThreadLocalContext.getTimezoneThreadLocal().get();
        ZoneId zoneId = StringUtils.isNotEmpty(timezone) ? ZoneId.of(timezone) : ZoneId.systemDefault();
        return localDateTime2Date(localDateTime, zoneId);
    }

    /**
     * local datetime to date
     *
     * @param localDateTime local datetime
     * @return date
     */
    private static Date localDateTime2Date(LocalDateTime localDateTime, ZoneId zoneId) {
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        return Date.from(instant);
    }

    /**
     * 获取当前时间的指定格式字符串。
     *
     * @param format 日期格式
     * @return 格式化后的日期字符串
     */
    public static String getCurrentTime(String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将Date对象按指定格式和时区转换为日期字符串。
     *
     * @param date 日期对象
     * @param format 日期格式，如 yyyy-MM-dd HH:mm:ss
     * @param timezone 时区，为null时使用系统默认时区
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String format, String timezone) {
        return format(date, DateTimeFormatter.ofPattern(format), timezone);
    }

    public static String format(Date date, DateTimeFormatter dateTimeFormatter, String timezone) {
        LocalDateTime localDateTime =
            StringUtils.isEmpty(timezone) ? date2LocalDateTime(date) : date2LocalDateTime(date, ZoneId.of(timezone));
        return format(localDateTime, dateTimeFormatter);
    }

    /**
     * 将LocalDateTime对象按指定格式转换为日期字符串。
     *
     * @param localDateTime 本地日期时间
     * @param format 日期格式，如 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的日期字符串
     */
    public static String format(LocalDateTime localDateTime, String format) {
        return format(localDateTime, DateTimeFormatter.ofPattern(format));
    }

    public static String format(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * 将Date对象转换为 yyyy-MM-dd HH:mm:ss 格式的字符串。
     *
     * @param date 日期对象
     * @return yyyy-MM-dd HH:mm:ss 格式的日期字符串
     */
    public static String dateToString(Date date) {
        return format(date, YYYY_MM_DD_HH_MM_SS, null);
    }

    /**
     * 将Date对象按指定时区转换为 yyyy-MM-dd HH:mm:ss 格式的字符串。
     *
     * @param date 日期对象
     * @param timezone 时区
     * @return yyyy-MM-dd HH:mm:ss 格式的日期字符串
     */
    public static String dateToString(Date date, String timezone) {
        return format(date, YYYY_MM_DD_HH_MM_SS, timezone);
    }

    /**
     * 将ZonedDateTime对象转换为 yyyy-MM-dd HH:mm:ss 格式的字符串。
     *
     * @param zonedDateTime 带时区的日期时间
     * @return yyyy-MM-dd HH:mm:ss 格式的日期字符串
     */
    public static String dateToString(ZonedDateTime zonedDateTime) {
        return YYYY_MM_DD_HH_MM_SS.format(zonedDateTime);
    }

    /**
     * 将ZonedDateTime对象按指定时区转换为 yyyy-MM-dd HH:mm:ss 格式的字符串。
     *
     * @param zonedDateTime 带时区的日期时间
     * @param timezone 目标时区
     * @return yyyy-MM-dd HH:mm:ss 格式的日期字符串
     */
    public static String dateToString(ZonedDateTime zonedDateTime, String timezone) {
        return dateToString(zonedDateTime, ZoneId.of(timezone));
    }

    /**
     * 将ZonedDateTime对象按指定ZoneId转换为 yyyy-MM-dd HH:mm:ss 格式的字符串。
     *
     * @param zonedDateTime 带时区的日期时间
     * @param zoneId 时区ID
     * @return yyyy-MM-dd HH:mm:ss 格式的日期字符串
     */
    public static String dateToString(ZonedDateTime zonedDateTime, ZoneId zoneId) {
        return DateTimeFormatter.ofPattern(DateConstants.YYYY_MM_DD_HH_MM_SS).withZone(zoneId).format(zonedDateTime);
    }

    /**
     * 将日期字符串按指定格式和时区解析为Date对象。
     *
     * @param date 日期字符串
     * @param format 日期格式
     * @param timezone 时区，可为null（使用系统默认时区）
     * @return 解析后的Date对象，解析失败返回null
     */
    public static Date parse(String date, String format, String timezone) {
        return parse(date, DateTimeFormatter.ofPattern(format), timezone);
    }

    public static Date parse(String date, DateTimeFormatter dateTimeFormatter, String timezone) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(date, dateTimeFormatter);
            if (StringUtils.isEmpty(timezone)) {
                return localDateTime2Date(ldt);
            }
            return localDateTime2Date(ldt, ZoneId.of(timezone));
        } catch (Exception e) {
            logger.error("error while parse date:" + date, e);
        }
        return null;
    }

    public static ZonedDateTime parseZoneDateTime(@Nonnull String date, @Nonnull DateTimeFormatter dateTimeFormatter,
                                                  @Nullable String timezone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, dateTimeFormatter);
        if (StringUtils.isNotEmpty(timezone)) {
            return zonedDateTime.withZoneSameInstant(ZoneId.of(timezone));
        }
        return zonedDateTime;
    }

    /**
     * 将 yyyy-MM-dd HH:mm:ss 格式的日期字符串解析为Date对象。
     *
     * @param date yyyy-MM-dd HH:mm:ss 格式的日期字符串
     * @return 解析后的Date对象，或null
     */
    public static @Nullable Date stringToDate(String date) {
        return parse(date, YYYY_MM_DD_HH_MM_SS, null);
    }

    public static ZonedDateTime stringToZoneDateTime(@Nonnull String date) {
        Date d = stringToDate(date);
        if (d == null) {
            throw new IllegalArgumentException(String.format(
                "data: %s should be a validate data string - yyyy-MM-dd HH:mm:ss ",
                date));
        }
        return ZonedDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 将 yyyy-MM-dd HH:mm:ss 格式的日期字符串按指定时区解析为Date对象。
     *
     * @param date yyyy-MM-dd HH:mm:ss 格式的日期字符串
     * @param timezone 时区
     * @return 解析后的Date对象
     */
    public static Date stringToDate(String date, String timezone) {
        return parse(date, YYYY_MM_DD_HH_MM_SS, timezone);
    }

    /**
     * 计算两个日期之间相差的秒数。
     *
     * @param d1 日期1
     * @param d2 日期2
     * @return 相差的秒数
     */
    public static long differSec(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return 0;
        }
        return (long) Math.ceil(differMs(d1, d2) / 1000.0);
    }

    /**
     * 计算两个日期之间相差的毫秒数。
     *
     * @param d1 日期1
     * @param d2 日期2
     * @return 相差的毫秒数
     */
    public static long differMs(Date d1, Date d2) {
        return Math.abs(d1.getTime() - d2.getTime());
    }

    /**
     * 获取指定日期前后若干天的日期。
     *
     * @param date 基准日期
     * @param day 偏移天数，正数表示之后，负数表示之前
     * @return 偏移后的日期
     */
    public static Date getSomeDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的小时数（0-23）。
     *
     * @param date 日期对象
     * @return 小时数
     */
    public static int getHourIndex(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 比较两个日期，判断future是否晚于old。
     *
     * @param future 较新的日期
     * @param old 较旧的日期
     * @return 如果future时间晚于old时间则返回true
     */
    public static boolean compare(Date future, Date old) {
        return future.getTime() > old.getTime();
    }

    /**
     * 将毫秒数格式化为可读的时间字符串（dd HH:mm:ss格式）。
     *
     * @param ms 毫秒数
     * @return 格式化的可读时间字符串
     */
    public static String format2Readable(long ms) {

        long days = MILLISECONDS.toDays(ms);
        long hours = MILLISECONDS.toDurationHours(ms);
        long minutes = MILLISECONDS.toDurationMinutes(ms);
        long seconds = MILLISECONDS.toDurationSeconds(ms);

        return String.format("%02d %02d:%02d:%02d", days, hours, minutes, seconds);

    }

    /**
     * 计算两个日期之间的持续时间，如果结束日期为null则使用当前时间作为结束时间。
     *
     * @param start 开始日期
     * @param end 结束日期，可为null
     * @return 格式化的持续时间字符串
     */
    public static String format2Duration(Date start, Date end) {
        if (start == null) {
            return null;
        }
        if (end == null) {
            end = new Date();
        }
        return format2Duration(differMs(start, end));
    }

    /**
     * 将毫秒数格式化为持续时间字符串（如 "1d 2h 3m 4s"）。
     *
     * @param ms 毫秒数
     * @return 格式化的持续时间字符串
     */
    public static String format2Duration(long ms) {

        long days = MILLISECONDS.toDays(ms);
        long hours = MILLISECONDS.toDurationHours(ms);
        long minutes = MILLISECONDS.toDurationMinutes(ms);
        long seconds = MILLISECONDS.toDurationSeconds(ms);

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            // if duration is zero, set 1s
            seconds = 1;
        }

        StringBuilder strBuilder = new StringBuilder();
        strBuilder = days > 0 ? strBuilder.append(days).append("d").append(" ") : strBuilder;
        strBuilder = hours > 0 ? strBuilder.append(hours).append("h").append(" ") : strBuilder;
        strBuilder = minutes > 0 ? strBuilder.append(minutes).append("m").append(" ") : strBuilder;
        strBuilder = seconds > 0 ? strBuilder.append(seconds).append("s") : strBuilder;

        return strBuilder.toString();

    }

    /**
     * 获取指定日期所在周的周一日期。
     * 注意：将每周的第一天设为周一（默认为周日）。
     *
     * @param date 基准日期
     * @return 周一对应的日期
     */
    public static Date getMonday(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        return cal.getTime();
    }

    /**
     * 获取指定日期所在周的周日日期。
     * 注意：将每周的第一天设为周一（默认为周日）。
     *
     * @param date 基准日期
     * @return 周日对应的日期
     */
    public static Date getSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        return cal.getTime();
    }

    /**
     * 获取指定日期所在月的第一天。
     *
     * @param date 基准日期
     * @return 当月第一天对应的日期
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    /**
     * 获取指定日期偏移若干小时后的整点时间。
     *
     * @param date 基准日期
     * @param offsetHour 偏移小时数
     * @return 偏移后的整点日期
     */
    public static Date getSomeHourOfDay(Date date, int offsetHour) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + offsetHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * 获取指定日期所在月的最后一天。
     *
     * @param date 基准日期
     * @return 当月最后一天对应的日期
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }

    /**
     * 获取指定日期当天的起始时间（00:00:00.000）。
     *
     * @param inputDay 输入日期
     * @return 当天起始时间
     */
    public static Date getStartOfDay(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取指定日期当天的结束时间（23:59:59.999）。
     *
     * @param inputDay 输入日期
     * @return 当天结束时间
     */
    public static Date getEndOfDay(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取指定日期所在小时的起始时间（xx:00:00.000）。
     *
     * @param inputDay 输入日期
     * @return 该小时起始时间
     */
    public static Date getStartOfHour(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取指定日期所在小时的结束时间（xx:59:59.999）。
     *
     * @param inputDay 输入日期
     * @return 该小时结束时间
     */
    public static Date getEndOfHour(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取当前日期时间。
     *
     * @return 当前Date对象
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * 对指定日期进行字段加减操作。
     *
     * @param date 基准日期，不能为null
     * @param calendarField Calendar字段，如Calendar.DAY_OF_MONTH
     * @param amount 增减量
     * @return 计算后的Date
     */
    public static Date add(final Date date, final int calendarField, final int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    /**
     * 从当前时间起，计算距离目标时间还剩多少秒。
     * 目标时间 = 基准时间 + 间隔秒数。
     *
     * @param baseTime 基准时间
     * @param intervalSeconds 间隔秒数
     * @return 剩余秒数
     */
    public static long getRemainTime(Date baseTime, long intervalSeconds) {
        if (baseTime == null) {
            return 0;
        }
        long usedTime = (System.currentTimeMillis() - baseTime.getTime()) / 1000;
        return intervalSeconds - usedTime;
    }

    /**
     * 获取当前时间戳字符串，格式为 yyyyMMddHHmmssSSS。
     *
     * @return 当前时间戳字符串
     */
    public static String getCurrentTimeStamp() {
        return getCurrentTime(DateConstants.YYYYMMDDHHMMSSSSS);
    }

    /**
     * 将日期从系统默认时区转换到目标时区。
     * 源时区为系统默认时区。
     *
     * @param date 原始日期
     * @param targetTimezoneId 目标时区ID
     * @return 转换时区后的日期
     */
    public static Date transformTimezoneDate(Date date, String targetTimezoneId) {
        return transformTimezoneDate(date, ZoneId.systemDefault().getId(), targetTimezoneId);
    }

    /**
     * 将日期从源时区转换到目标时区。
     * 例如：输入日期 `Thu Apr 28 10:00:00 UTC 2022`，源时区为UTC，目标时区为 Asia/Shanghai，
     * 将返回 `Thu Apr 28 02:00:00 UTC 2022`。
     *
     * @param date 原始日期
     * @param sourceTimezoneId 源时区ID
     * @param targetTimezoneId 目标时区ID
     * @return 转换时区后的日期
     */
    public static Date transformTimezoneDate(Date date, String sourceTimezoneId, String targetTimezoneId) {
        if (StringUtils.isEmpty(sourceTimezoneId) || StringUtils.isEmpty(targetTimezoneId)) {
            return date;
        }
        String dateToString = dateToString(date, sourceTimezoneId);
        LocalDateTime localDateTime =
                LocalDateTime.parse(dateToString, DateTimeFormatter.ofPattern(DateConstants.YYYY_MM_DD_HH_MM_SS));
        ZonedDateTime zonedDateTime =
            ZonedDateTime.of(localDateTime, TimeZone.getTimeZone(targetTimezoneId).toZoneId());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 根据时区ID获取对应的TimeZone对象。
     *
     * @param timezoneId 时区ID
     * @return 对应的TimeZone，如果ID为空则返回null
     */
    public static TimeZone getTimezone(String timezoneId) {
        if (StringUtils.isEmpty(timezoneId)) {
            return null;
        }
        return TimeZone.getTimeZone(timezoneId);
    }

    /**
     * 毫秒时间单位工具类，提供天、小时、分钟、秒的换算方法。
     */
    public static class MILLISECONDS {
        public static long toDays(long d) {
            return d / (C6 / C2);
        }

        public static long toDurationSeconds(long d) {
            return (d % (C4 / C2)) / (C3 / C2);
        }

        public static long toDurationMinutes(long d) {
            return (d % (C5 / C2)) / (C4 / C2);
        }

        public static long toDurationHours(long d) {
            return (d % (C6 / C2)) / (C5 / C2);
        }

    }

    /**
     * 将时间戳（毫秒）转换为Date对象。
     *
     * @param timeStamp 时间戳（毫秒）
     * @return 转换后的Date，如果时间戳<=0则返回null
     */
    public static @Nullable Date timeStampToDate(long timeStamp) {
        return timeStamp <= 0L ? null : new Date(timeStamp);
    }

    /**
     * 将Date对象转换为时间戳（毫秒）。
     *
     * @param date Date对象
     * @return 时间戳（毫秒），如果date为null则返回0
     */
    public static long dateToTimeStamp(Date date) {
        return date == null ? 0L : date.getTime();
    }

    /**
     * a default datetime formatter for the timestamp
     */
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 将时间戳（毫秒）格式化为 yyyy-MM-dd HH:mm:ss 字符串。
     *
     * @param timeMillis 毫秒时间戳，如 System.currentTimeMillis()
     * @return yyyy-MM-dd HH:mm:ss 格式的日期字符串
     */
    public static String formatTimeStamp(long timeMillis) {
        return formatTimeStamp(timeMillis, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 使用指定的DateTimeFormatter将时间戳格式化为字符串。
     *
     * @param timeMillis 毫秒时间戳
     * @param dateTimeFormatter 日期格式化器，如 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的日期字符串
     */
    public static String formatTimeStamp(long timeMillis, DateTimeFormatter dateTimeFormatter) {
        Objects.requireNonNull(dateTimeFormatter);
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis),
                ZoneId.systemDefault()));
    }

    /**
     * 将Date对象按指定格式转换为日期字符串（使用系统默认时区）。
     *
     * @param date 日期对象
     * @param format 日期格式，如 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String format) {
        return format(date2LocalDateTime(date), format);
    }

    /**
     * 将日期字符串按指定格式解析为Date对象（使用系统默认时区）。
     *
     * @param date 日期字符串
     * @param format 日期格式
     * @return 解析后的Date，解析失败返回null
     */
    public static Date parse(String date, String format) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            return localDateTime2Date(ldt);
        } catch (Exception e) {
            logger.error("error while parse date:" + date, e);
        }
        return null;
    }

    /**
     * 将调度时间字符串转换为Date对象。
     *
     * @param schedule yyyy-MM-dd HH:mm:ss 格式的调度时间字符串
     * @return 解析后的Date对象
     */
    public static Date getScheduleDate(String schedule) {
        return stringToDate(schedule);
    }

    public static Date addMonths(Date date, int amount) {
        return add(date, 2, amount);
    }

    public static Date addDays(Date date, int amount) {
        return add(date, 5, amount);
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, 12, amount);
    }

    public static String getTimestampString() {
        return String.valueOf(System.currentTimeMillis());
    }

}
