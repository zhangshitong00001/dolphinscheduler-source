package org.apache.dolphinscheduler.service.queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的任务优先级队列实现，使用Redis的Sorted Set（ZSet）结构存储任务。通过评分机制实现优先级排序，
 * 同优先级下保证FIFO顺序。当配置 queue.type=redis 时启用此实现。
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "queue", name = "type", havingValue = "redis")
public class RedisTaskPriorityQueueImpl implements TaskPriorityQueue<TaskPriority> {

    private static final String REDIS_TASK_QUEUE_KEY = "dolphinscheduler:task_queue";

    // 用于将时间戳归一化到小数位，保证整数部分主要是优先级
    private static final double SCORE_DIVIDER = 10000000000000.0;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void put(TaskPriority taskPriority) throws TaskPriorityQueueException {
        try {
            // 1. 序列化任务对象
            String taskJson = JSONUtils.toJsonString(taskPriority);

            // 2. 计算 Score (优先级 + 时间戳)
            // 假设 processInstancePriority 越小优先级越高 (0=Highest, 1=High, ...)
            // 使用 Checkpoint (毫秒时间戳) 作为小数部分，保证同优先级下 FIFO
            double score = taskPriority.getProcessInstancePriority() + (taskPriority.getCheckpoint() / SCORE_DIVIDER);

            // 3. ZADD
            redisTemplate.opsForZSet().add(REDIS_TASK_QUEUE_KEY, taskJson, score);
        } catch (Exception e) {
            log.error("Failed to put task into redis queue={}",e.getMessage());
            throw new TaskPriorityQueueException("Failed to put task into redis queue", e);
        }
    }

    @Override
    public TaskPriority take() throws TaskPriorityQueueException, InterruptedException {
        // 简单实现：循环 polling (实际生产建议使用 Lua 脚本或 BZPOPMIN 命令)
        while (true) {
            TaskPriority task = poll(1000, TimeUnit.MILLISECONDS);
            if (task != null) {
                return task;
            }
            // 避免空轮询 CPU 飙升
            Thread.sleep(100);
        }
    }

    @Override
    public TaskPriority poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException, InterruptedException {
        long expireTime = System.currentTimeMillis() + unit.toMillis(timeout);

        while (System.currentTimeMillis() < expireTime) {
            // ZPOPMIN: 弹出分数最小的元素 (优先级最高)
            // 注意：popMin 是原子操作 (Redis 5.0+)
            ZSetOperations.TypedTuple<String> tuple = redisTemplate.opsForZSet().popMin(REDIS_TASK_QUEUE_KEY);

            if (tuple != null && tuple.getValue() != null) {
                return JSONUtils.parseObject(tuple.getValue(), TaskPriority.class);
            }

            // 短暂休眠，避免死循环打满 Redis 连接
            Thread.sleep(200);
        }
        return null;
    }

    @Override
    public int size() throws TaskPriorityQueueException {
        Long size = redisTemplate.opsForZSet().size(REDIS_TASK_QUEUE_KEY);
        return size != null ? size.intValue() : 0;
    }
}
