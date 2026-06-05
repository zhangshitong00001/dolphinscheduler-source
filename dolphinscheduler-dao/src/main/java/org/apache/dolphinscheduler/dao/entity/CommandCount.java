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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.CommandType;

/**
 * 命令统计实体，非数据库表映射，用于命令数量的统计结果封装。
 * 按命令类型统计各状态的命令数量，通常用于 Dashboard 展示运维概览数据。
 */
public class CommandCount {

    /** 命令类型，枚举值如 START_PROCESS、COMPLEMENT_DATA 等 */
    private CommandType commandType;

    /** 该类型命令的数量 */
    private int count;

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommandCount that = (CommandCount) o;

        if (count != that.count) {
            return false;
        }
        return commandType == that.commandType;

    }

    @Override
    public int hashCode() {
        int result = commandType != null ? commandType.hashCode() : 0;
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString() {
        return "CommandCount{"
                + "commandType=" + commandType
                + ", count=" + count
                + '}';
    }
}
