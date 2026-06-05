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

package org.apache.dolphinscheduler.server.master.dispatch.host.assign;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;

/**
 * 抽象选择器。实现公共的 select 方法：空集合抛异常，单元素直接返回，多元素委托给子类的 doSelect 方法。
 *
 * @param <T> 选择元素的类型
 */
public  abstract class AbstractSelector<T> implements Selector<T> {
    /**
     * 从集合中选择一个元素。空集合抛异常，单元素直接返回，多元素委托子类实现。
     *
     * @param source 待选择的源集合，不能为空
     * @return 选中的元素
     */
    @Override
    public T select(Collection<T> source) {

        if (CollectionUtils.isEmpty(source)) {
            throw new IllegalArgumentException("Empty source.");
        }

        /*
         * 只有一个元素时直接返回。
         */
        if (source.size() == 1) {
            return (T)source.toArray()[0];
        }
        return doSelect(source);
    }

    /**
     * 子类实现具体的多元素选择算法。
     *
     * @param source 待选择的源集合（元素数量大于 1）
     * @return 选中的元素
     */
    protected abstract T doSelect(Collection<T> source);

}
