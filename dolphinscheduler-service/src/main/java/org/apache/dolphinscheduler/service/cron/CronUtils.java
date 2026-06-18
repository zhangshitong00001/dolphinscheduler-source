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

package org.apache.dolphinscheduler.service.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.NonNull;

import static com.cronutils.model.CronType.QUARTZ;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.day;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.hour;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.min;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.month;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.week;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.year;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cron表达式工具类，提供Cron解析、校验以及基于Cron调度时间计算等功能。
 * <p>依赖Quartz和cron-utils库进行Cron解析，支持获取指定时间范围内的触发时间列表。</p>
 */
public class CronUtils {

    private CronUtils() {
        throw new IllegalStateException("CronUtils class");
    }

    private static final Logger logger = LoggerFactory.getLogger(CronUtils.class);

    private static final CronParser QUARTZ_CRON_PARSER =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));

    /**
     * 将Cron表达式字符串解析为Cron对象。
     *
     * @param cronExpression Cron表达式字符串，不能为null
     * @return 与输入表达式对应的Cron实例
     * @throws CronParseException 当Cron表达式格式无效时抛出
     */
    public static Cron parse2Cron(String cronExpression) throws CronParseException {
        try {
            return QUARTZ_CRON_PARSER.parse(cronExpression);
        } catch (IllegalArgumentException ex) {
            throw new CronParseException(String.format("Parse corn expression: [%s] error", cronExpression), ex);
        }
    }

    /**
     * 判断指定的Cron表达式是否有效，能否被成功解析。
     *
     * @param cronExpression 待校验的Cron表达式字符串
     * @return 若表达式有效返回true，否则返回false
     */
    public static boolean isValidExpression(String cronExpression) {
        try {
            parse2Cron(cronExpression);
        } catch (CronParseException e) {
            return false;
        }

        return true;
    }

    /**
     * 获取Cron表达式的最大调度周期。
     *
     * @param cron Cron对象
     * @return 最大调度周期枚举值
     */
    public static CycleEnum getMaxCycle(Cron cron) {
        return min(cron).addCycle(hour(cron)).addCycle(day(cron)).addCycle(week(cron)).addCycle(month(cron))
                .addCycle(year(cron)).getCycle();
    }

    /**
     * 获取Cron表达式的最小调度周期。
     *
     * @param cron Cron对象
     * @return 最小调度周期枚举值
     */
    public static CycleEnum getMiniCycle(Cron cron) {
        return min(cron).addCycle(hour(cron))
                .addCycle(day(cron))
                .addCycle(week(cron))
                .addCycle(month(cron))
                .addCycle(year(cron))
                .getMiniCycle();
    }

    /**
     * 根据Crontab字符串获取最大调度周期。
     *
     * @param crontab Crontab表达式字符串
     * @return 最大调度周期枚举值
     */
    public static CycleEnum getMaxCycle(String crontab) {
        try {
            return getMaxCycle(parse2Cron(crontab));
        } catch (CronParseException ex) {
            throw new RuntimeException("Get max cycle error", ex);
        }
    }

    public static List<ZonedDateTime> getFireDateList(@NonNull ZonedDateTime startTime,
                                                      @NonNull ZonedDateTime endTime,
                                                      @NonNull String cron) throws CronParseException {
        return getFireDateList(startTime, endTime, parse2Cron(cron));
    }

    /**
     * 获取指定时间范围内所有不基于自依赖的调度触发时间。
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param cron Cron对象
     * @return 调度触发时间列表
     */
    public static List<ZonedDateTime> getFireDateList(@NonNull ZonedDateTime startTime,
                                                      @NonNull ZonedDateTime endTime,
                                                      @NonNull Cron cron) {
        List<ZonedDateTime> dateList = new ArrayList<>();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        while (!ServerLifeCycleManager.isStopped()) {
            Optional<ZonedDateTime> nextExecutionTimeOptional = executionTime.nextExecution(startTime);
            if (!nextExecutionTimeOptional.isPresent()) {
                break;
            }
            startTime = nextExecutionTimeOptional.get();
            if (startTime.isAfter(endTime)) {
                break;
            }
            dateList.add(startTime);
        }

        return dateList;
    }

    /**
     * 获取指定时间范围内基于自依赖的期望调度触发时间，限制触发次数。
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param cron Cron对象
     * @param fireTimes 最大触发次数
     * @return 调度触发时间列表
     */
    public static List<ZonedDateTime> getSelfFireDateList(@NonNull ZonedDateTime startTime,
                                                          @NonNull ZonedDateTime endTime, @NonNull Cron cron,
                                                          int fireTimes) {
        List<ZonedDateTime> executeTimes = new ArrayList<>();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        while (fireTimes > 0) {
            Optional<ZonedDateTime> nextTime = executionTime.nextExecution(startTime);
            if (!nextTime.isPresent()) {
                break;
            }
            startTime = nextTime.get();
            if (startTime.isAfter(endTime)) {
                break;
            }
            executeTimes.add(startTime);
            fireTimes--;
        }
        return executeTimes;
    }

    public static List<Date> getSelfFireDateList(@NonNull final Date startTime,
                                                 @NonNull final Date endTime,
                                                 @NonNull final List<Schedule> schedules) throws CronParseException {
        ZonedDateTime zonedDateTimeStart = ZonedDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault());
        ZonedDateTime zonedDateTimeEnd = ZonedDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault());

        return getSelfFireDateList(zonedDateTimeStart, zonedDateTimeEnd, schedules).stream()
                .map(zonedDateTime -> new Date(zonedDateTime.toInstant().toEpochMilli()))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定时间范围内基于自依赖的调度触发时间列表，支持多个调度配置。
     * <p>如果调度列表为空，则默认使用每天一次的调度。</p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param schedules 调度配置列表
     * @return 调度触发时间列表
     * @throws CronParseException 当Cron表达式解析失败时抛出
     */
    public static List<ZonedDateTime> getSelfFireDateList(@NonNull final ZonedDateTime startTime,
                                                          @NonNull final ZonedDateTime endTime,
                                                          @NonNull final List<Schedule> schedules) throws CronParseException {
        List<ZonedDateTime> result = new ArrayList<>();
        if (startTime.equals(endTime)) {
            result.add(startTime);
            return result;
        }

        // support left closed and right closed time interval (startDate <= N <= endDate)
        ZonedDateTime from = startTime.minusSeconds(1L);
        ZonedDateTime to = endTime.plusSeconds(1L);

        List<Schedule> listSchedule = new ArrayList<>();
        listSchedule.addAll(schedules);
        if (CollectionUtils.isEmpty(listSchedule)) {
            Schedule schedule = new Schedule();
            schedule.setCrontab(Constants.DEFAULT_CRON_STRING);
            listSchedule.add(schedule);
        }
        for (Schedule schedule : listSchedule) {
            result.addAll(CronUtils.getFireDateList(from, to, schedule.getCrontab()));
        }
        return result;
    }

    /**
     * 获取过期时间点，根据开始时间和周期类型计算依赖任务的最晚可接受时间。
     *
     * @param startTime 开始时间
     * @param cycleEnum 调度周期类型
     * @return 过期时间点
     */
    public static Date getExpirationTime(Date startTime, CycleEnum cycleEnum) {
        Date maxExpirationTime = null;
        Date startTimeMax = null;
        try {
            startTimeMax = getEndTime(startTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            switch (cycleEnum) {
                case HOUR:
                    calendar.add(Calendar.HOUR, 1);
                    break;
                case DAY:
                    calendar.add(Calendar.DATE, 1);
                    break;
                case WEEK:
                    calendar.add(Calendar.DATE, 1);
                    break;
                case MONTH:
                    calendar.add(Calendar.DATE, 1);
                    break;
                default:
                    logger.error("Dependent process definition's  cycleEnum is {},not support!!", cycleEnum);
                    break;
            }
            maxExpirationTime = calendar.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return DateUtils.compare(startTimeMax, maxExpirationTime) ? maxExpirationTime : startTimeMax;
    }

    /**
     * get the end time of the day by value of date
     *
     * @return date
     */
    private static Date getEndTime(Date date) {
        Calendar end = new GregorianCalendar();
        end.setTime(date);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTime();
    }

    /**
     * 获取补数调度日期列表，从命令参数中解析补数日期集合。
     *
     * @param param 命令参数字典
     * @return 补数日期列表，如果没有配置补数日期则返回null
     */
    public static List<Date> getSelfScheduleDateList(Map<String, String> param) {
        List<Date> result = new ArrayList<>();
        String scheduleDates = param.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
        if (StringUtils.isNotEmpty(scheduleDates)) {
            for (String stringDate : scheduleDates.split(COMMA)) {
                result.add(DateUtils.stringToDate(stringDate.trim()));
            }
            return result;
        }
        return null;
    }

}
