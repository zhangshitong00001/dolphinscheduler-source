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

package org.apache.dolphinscheduler.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.common.config.YamlPropertySourceFactory;
import org.apache.dolphinscheduler.common.constants.Constants;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 任务类型配置。从task-type-config.yaml读取任务类型分类配置，将任务分为通用、云服务、逻辑、
 * 数据集成、数据质量、机器学习和其它等类别，并生成默认收藏任务类型集合。
 */
@Component
@EnableConfigurationProperties
@PropertySource(value = {"classpath:task-type-config.yaml"}, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "task")
@Getter
@Setter
public class TaskTypeConfiguration {

    private List<String> universal;
    private List<String> cloud;
    private List<String> logic;
    private List<String> dataIntegration;
    private List<String> dataQuality;
    private List<String> other;

    private List<String> machineLearning;

    /**
     * 获取默认任务类型集合。将配置中的所有分类任务类型转换为FavTaskDto集合并返回。
     *
     * @return 默认任务类型集合
     */
    public Set<FavTaskDto> getDefaultTaskTypes() {
        Set<FavTaskDto> defaultTaskTypes = new HashSet<>();
        if (CollectionUtils.isNotEmpty(defaultTaskTypes)) {
            return defaultTaskTypes;
        }
        universal.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_UNIVERSAL)));
        cloud.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_CLOUD)));
        logic.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_LOGIC)));
        dataIntegration.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_DATA_INTEGRATION)));
        dataQuality.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_DATA_QUALITY)));
        machineLearning.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_MACHINE_LEARNING)));
        other.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_OTHER)));

        return defaultTaskTypes;
    }
}
