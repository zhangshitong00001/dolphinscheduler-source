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

package org.apache.dolphinscheduler.remote.processor;

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.Host;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * Netty远程通道。封装Netty Channel，提供通道地址、状态检测和命令发送功能。
 */
public class NettyRemoteChannel {

    /**
     * Netty通道实例
     */
    private final Channel channel;

    /**
     * 请求唯一标识
     */
    private final long opaque;

    /**
     * 远端主机信息
     */
    private final Host host;


    public NettyRemoteChannel(Channel channel, long opaque) {
        this.channel = channel;
        this.host = ChannelUtils.toAddress(channel);
        this.opaque = opaque;
    }

    public NettyRemoteChannel(Channel channel) {
        this.channel = channel;
        this.host = ChannelUtils.toAddress(channel);
        this.opaque = -1;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getOpaque() {
        return opaque;
    }

    public Host getHost() {
        return host;
    }

    /**
     * 检查通道是否活跃。
     *
     * @return 是否活跃
     */
    public boolean isActive(){
        return this.channel.isActive();
    }

    /**
     * 向通道写入并刷新命令。
     *
     * @param command command
     * @return ChannelFuture
     */
    public ChannelFuture writeAndFlush(Command command) {
        return this.channel.writeAndFlush(command);
    }

    /**
     * 关闭通道。
     */
    public void close(){
        this.channel.close();
    }
}
