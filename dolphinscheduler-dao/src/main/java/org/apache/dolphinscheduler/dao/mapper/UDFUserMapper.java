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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.UDFUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * UDF 用户关联 Mapper 接口，封装对 t_ds_udf_user 表的数据库操作。
 * 继承 MyBatis-Plus BaseMapper，提供 UDF 函数与用户授权关系的解绑等能力。
 */
public interface UDFUserMapper extends BaseMapper<UDFUser> {

    /**
     * 根据用户ID删除该用户的所有 UDF 函数授权关联。
     * DELETE FROM t_ds_udf_user WHERE user_id = #{userId}
     *
     * @param userId 用户ID
     * @return 删除的行数
     */
    int deleteByUserId(@Param("userId") int userId);

    /**
     * 根据 UDF 函数ID删除该函数的所有用户授权关联。
     * DELETE FROM t_ds_udf_user WHERE udf_func_id = #{udfFuncId}
     *
     * @param udfFuncId UDF 函数ID
     * @return 删除的行数
     */
    int deleteByUdfFuncId(@Param("udfFuncId") int udfFuncId);

}
