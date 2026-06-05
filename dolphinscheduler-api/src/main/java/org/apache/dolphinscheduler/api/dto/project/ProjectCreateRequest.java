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

package org.apache.dolphinscheduler.api.dto.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 项目创建请求DTO。用于接收创建项目时的请求参数，包含项目名称和描述信息。
 */
@Data
public class ProjectCreateRequest {

    /** 项目名称 */
    @ApiModelProperty(example = "pro123", required = true)
    private String projectName;

    /** 项目描述 */
    @ApiModelProperty(example = "this is a project")
    private String description;
}
