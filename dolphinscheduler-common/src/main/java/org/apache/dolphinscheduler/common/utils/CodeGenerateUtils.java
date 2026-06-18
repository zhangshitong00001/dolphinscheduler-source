/** Copyright 2010-2012 Twitter, Inc.*/

package org.apache.dolphinscheduler.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * 基于Twitter雪花算法的分布式唯一ID生成工具类。
 * 该类使用雪花算法（Snowflake）生成全局唯一的Long类型ID，支持高并发场景下的分布式ID生成。
 * 该类为单例模式，不可直接实例化。
 */
public class CodeGenerateUtils {

    // start timestamp
    private static final long START_TIMESTAMP = 1609430400000L; // 2021-01-01 00:00:00
    // Each machine generates 32 in the same millisecond
    private static final long LOW_DIGIT_BIT = 5L;
    private static final long MIDDLE_BIT = 2L;
    private static final long MAX_LOW_DIGIT = ~(-1L << LOW_DIGIT_BIT);
    // The displacement to the left
    private static final long MIDDLE_LEFT = LOW_DIGIT_BIT;
    private static final long HIGH_DIGIT_LEFT = LOW_DIGIT_BIT + MIDDLE_BIT;
    private final long machineHash;
    private long lowDigit = 0L;
    private long recordMillisecond = -1L;

    private static final long SYSTEM_TIMESTAMP = System.currentTimeMillis();
    private static final long SYSTEM_NANOTIME = System.nanoTime();

    private CodeGenerateUtils() throws CodeGenerateException {
        try {
            this.machineHash =
                    Math.abs(Objects.hash(InetAddress.getLocalHost().getHostName())) % (2 << (MIDDLE_BIT - 1));
        } catch (UnknownHostException e) {
            throw new CodeGenerateException(e.getMessage());
        }
    }

    private static CodeGenerateUtils instance = null;

    /**
     * 获取CodeGenerateUtils的单例实例。
     *
     * @return CodeGenerateUtils单例实例
     * @throws CodeGenerateException 如果获取本地主机名失败
     */
    public static synchronized CodeGenerateUtils getInstance() throws CodeGenerateException {
        if (instance == null) {
            instance = new CodeGenerateUtils();
        }
        return instance;
    }

    /**
     * 生成一个全局唯一的分布式ID。
     *
     * @return 生成的唯一ID
     * @throws CodeGenerateException 如果系统时钟回拨导致无法生成ID
     */
    public synchronized long genCode() throws CodeGenerateException {
        long nowtMillisecond = systemMillisecond();
        if (nowtMillisecond < recordMillisecond) {
            throw new CodeGenerateException("New code exception because time is set back.");
        }
        if (nowtMillisecond == recordMillisecond) {
            lowDigit = (lowDigit + 1) & MAX_LOW_DIGIT;
            if (lowDigit == 0L) {
                while (nowtMillisecond <= recordMillisecond) {
                    nowtMillisecond = systemMillisecond();
                }
            }
        } else {
            lowDigit = 0L;
        }
        recordMillisecond = nowtMillisecond;
        return (nowtMillisecond - START_TIMESTAMP) << HIGH_DIGIT_LEFT | machineHash << MIDDLE_LEFT | lowDigit;
    }

    private long systemMillisecond() {
        return SYSTEM_TIMESTAMP + (System.nanoTime() - SYSTEM_NANOTIME) / 1000000;
    }

    /**
     * 雪花算法ID生成异常，当系统时钟回拨或网络不可达时抛出。
     */
    public static class CodeGenerateException extends RuntimeException {

        public CodeGenerateException(String message) {
            super(message);
        }
    }
}
