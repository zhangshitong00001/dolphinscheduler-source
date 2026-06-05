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

package org.apache.dolphinscheduler.common.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Shell命令执行器，AbstractShell的具体实现类。
 * 适用于无需显式解析输出的简单命令执行场景，命令执行后输出按原样存储。
 * 支持工作目录、环境变量和超时时间的配置。
 * 输出较小，适合一次性获取的场景。
 */
public class ShellExecutor extends AbstractShell {

    private String[] command;
    private StringBuffer output;

    public ShellExecutor(String... execString) {
        this(execString, null);
    }

    public ShellExecutor(String[] execString, File dir) {
        this(execString, dir, null);
    }

    public ShellExecutor(String[] execString, File dir,
                                Map<String, String> env) {
        this(execString, dir, env,0L);
    }

    /**
     * 创建ShellExecutor实例以执行命令。
     *
     * @param execString 要执行的命令及其参数
     * @param dir 命令的工作目录，为null时不修改当前工作目录
     * @param env 环境变量Map，为null时不修改当前环境
     * @param timeout 超时时间（毫秒），0表示不超时
     */
    public ShellExecutor(String[] execString, File dir,
                                Map<String, String> env, long timeout) {
        command = execString.clone();
        if (dir != null) {
            setWorkingDirectory(dir);
        }
        if (env != null) {
            setEnvironment(env);
        }
        timeOutInterval = timeout;
    }

    /**
     * 静态方法，执行Shell命令的快捷方式。
     * 覆盖大多数简单场景，无需用户实现AbstractShell接口。
     *
     * @param cmd 要执行的Shell命令
     * @return 命令执行的输出
     * @throws IOException IO异常
     */
    public static String execCommand(String... cmd) throws IOException {
        return execCommand(null, cmd, 0L);
    }

    /**
     * 静态方法，带环境变量和超时的Shell命令执行。
     *
     * @param env 环境变量Map
     * @param cmd 要执行的命令数组
     * @param timeout 超时时间（毫秒），超时后命令被终止并标记为超时
     * @return 命令执行的输出
     * @throws IOException IO异常
     */
    public static String execCommand(Map<String, String> env, String[] cmd,
                                     long timeout) throws IOException {
        ShellExecutor exec = new ShellExecutor(cmd, null, env,
                timeout);
        exec.execute();
        return exec.getOutput();
    }

    /**
     * 静态方法，带环境变量的Shell命令执行（无超时）。
     *
     * @param env 环境变量Map
     * @param cmd 要执行的Shell命令
     * @return 命令执行的输出
     * @throws IOException IO异常
     */
    public static String execCommand(Map<String,String> env, String... cmd)
            throws IOException {
        return execCommand(env, cmd, 0L);
    }

    /**
     * 执行Shell命令。
     *
     * @throws IOException IO异常
     */
    public void execute() throws IOException {
        this.run();
    }

    @Override
    protected String[] getExecString() {
        return command;
    }

    @Override
    protected void parseExecResult(BufferedReader lines) throws IOException {
        output = new StringBuffer();
        char[] buf = new char[1024];
        int nRead;
        String line = "";
        while ((nRead = lines.read(buf, 0, buf.length)) > 0) {
            line = new String(buf,0,nRead);
            output.append(line);
        }
    }

    /**
     * 获取Shell命令的输出。
     *
     * @return 命令输出字符串
     */
    public String getOutput() {
        return (output == null) ? "" : output.toString();
    }


    /**
     * Returns the commands of this instance.
     * Arguments with spaces in are presented with quotes round; other
     * arguments are presented raw
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String[] args = getExecString();
        for (String s : args) {
            if (s.indexOf(' ') >= 0) {
                builder.append('"').append(s).append('"');
            } else {
                builder.append(s);
            }
            builder.append(' ');
        }
        return builder.toString();
    }
}
