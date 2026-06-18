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

package org.apache.dolphinscheduler.api;

import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * DolphinScheduler API应用服务启动类。作为Spring Boot应用入口，负责启动API服务并在应用就绪后加载任务插件信息到数据库。
 */
@ServletComponentScan
@SpringBootApplication
@ComponentScan("org.apache.dolphinscheduler")
public class ApiApplicationServer {

    private final Logger logger = LoggerFactory.getLogger(ApiApplicationServer.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private PluginDao pluginDao;

    public static void main(String[] args) {
        SpringApplication.run(ApiApplicationServer.class);
    }

    /**
     * 应用就绪事件处理。在Spring上下文就绪后加载所有任务插件并将插件参数同步到数据库。
     *
     * @param readyEvent 应用就绪事件
     */
    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {
        logger.info("Received spring application context ready event will load taskPlugin and write to DB");
        // install task plugin
        taskPluginManager.loadPlugin();
        for (Map.Entry<String, TaskChannelFactory> entry : taskPluginManager.getTaskChannelFactoryMap().entrySet()) {
            String taskPluginName = entry.getKey();
            TaskChannelFactory taskChannelFactory = entry.getValue();
            List<PluginParams> params = taskChannelFactory.getParams();
            String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            PluginDefine pluginDefine = new PluginDefine(taskPluginName, PluginType.TASK.getDesc(), paramsJson);
            pluginDao.addOrUpdatePluginDefine(pluginDefine);
        }
    }
}
