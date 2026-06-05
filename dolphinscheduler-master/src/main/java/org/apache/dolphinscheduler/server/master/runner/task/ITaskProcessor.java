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

package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPI;

/**
 * Master 端任务处理器的标准接口，定义了任务在 Master 中的生命周期管理操作。
 * 所有任务类型处理器（通用任务、条件任务、依赖任务、子流程等）均需实现此接口。
 * 通过 SPI 机制加载不同的任务处理器实现。
 */
public interface ITaskProcessor extends PrioritySPI {

    /**
     * 初始化任务处理器，注入任务实例和工作流实例。
     *
     * @param taskInstance    任务实例
     * @param processInstance 工作流实例
     */
    void init(TaskInstance taskInstance, ProcessInstance processInstance);

    /**
     * 执行指定的任务动作。
     *
     * @param taskAction 任务动作类型
     * @return 是否执行成功
     */
    boolean action(TaskAction taskAction);

    /**
     * 获取任务处理器对应的任务类型标识。
     *
     * @return 任务类型字符串
     */
    String getType();

    /**
     * 获取当前绑定的任务实例。
     *
     * @return 任务实例
     */
    TaskInstance taskInstance();

}
