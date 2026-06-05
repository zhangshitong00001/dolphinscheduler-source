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
package org.apache.dolphinscheduler.remote.future;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 释放信号量。用于确保信号量仅被释放一次，防止重复释放导致的信号量计数错误。
 */
public class ReleaseSemaphore {

    /**
     * 信号量实例
     */
    private final Semaphore semaphore;

    /**
     * 是否已释放标志
     */
    private final AtomicBoolean released;

    public ReleaseSemaphore(Semaphore semaphore){
        this.semaphore = semaphore;
        this.released = new AtomicBoolean(false);
    }

    /**
     * 释放信号量。使用CAS操作确保信号量仅被释放一次。
     */
    public void release(){
        if(this.released.compareAndSet(false, true)){
            this.semaphore.release();
        }
    }
}
