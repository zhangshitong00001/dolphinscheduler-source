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

package org.apache.dolphinscheduler.common.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.DATA_BASEDIR_PATH;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_VIEW_SUFFIXES;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE;
import static org.apache.dolphinscheduler.common.constants.Constants.UTF_8;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYYMMDDHHMMSS;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件操作工具类，提供文件读写、目录管理、资源路径处理等常用文件操作。
 * 支持执行目录管理、文件内容读写、目录遍历检测等功能。
 * 该类为工具类，不可实例化。
 */
public class FileUtils {

    public static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final String DATA_BASEDIR = PropertyUtils.getString(DATA_BASEDIR_PATH, "/tmp/dolphinscheduler");

    private FileUtils() {
        throw new UnsupportedOperationException("Construct FileUtils");
    }

    /**
     * 获取下载文件的绝对路径和名称，自动创建父目录。
     *
     * @param filename 文件名称
     * @return 带时间戳的下载文件路径
     */
    public static String getDownloadFilename(String filename) {
        String fileName = String.format("%s/download/%s/%s", DATA_BASEDIR, DateUtils.getCurrentTime(YYYYMMDDHHMMSS), filename);

        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * 获取上传文件的绝对路径和名称，根据租户编码分类存储。
     *
     * @param tenantCode 租户编码
     * @param filename 文件名称
     * @return 上传文件的本地路径
     */
    public static String getUploadFilename(String tenantCode, String filename) {
        String fileName = String.format("%s/%s/resources/%s", DATA_BASEDIR, tenantCode, filename);
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * 获取流程执行的工作目录路径，按照租户、项目、流程定义、实例、任务层级组织。
     *
     * @param tenant 租户编码
     * @param projectCode 项目编码
     * @param processDefineCode 流程定义编码
     * @param processDefineVersion 流程定义版本
     * @param processInstanceId 流程实例ID
     * @param taskInstanceId 任务实例ID
     * @return 流程执行目录路径
     */
    public static String getProcessExecDir(String tenant,
                                           long projectCode,
                                           long processDefineCode,
                                           int processDefineVersion,
                                           int processInstanceId,
                                           int taskInstanceId) {
        return String.format(
                "%s/exec/process/%s/%d/%d_%d/%d/%d",
                DATA_BASEDIR,
                tenant,
                projectCode,
                processDefineCode,
                processDefineVersion,
                processInstanceId,
                taskInstanceId);
    }

    /**
     * 获取支持在线预览的资源文件后缀名列表。
     *
     * @return 资源文件后缀名字符串
     */
    public static String getResourceViewSuffixes() {
        return PropertyUtils.getString(RESOURCE_VIEW_SUFFIXES, RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE);
    }

    /**
     * 创建工作目录，如果目录已存在则先删除再创建。
     *
     * @param execLocalPath 执行目录的本地路径
     * @throws IOException IO异常
     */
    public static void createWorkDirIfAbsent(String execLocalPath) throws IOException {
        //if work dir exists, first delete
        File execLocalPathFile = new File(execLocalPath);

        if (execLocalPathFile.exists()) {
            try {
                org.apache.commons.io.FileUtils.forceDelete(execLocalPathFile);
            } catch (Exception ex) {
                if (ex instanceof NoSuchFileException || ex.getCause() instanceof NoSuchFileException) {
                    // this file is already be deleted.
                } else {
                    throw ex;
                }
            }
        }

        //create work dir
        org.apache.commons.io.FileUtils.forceMkdir(execLocalPathFile);
        String mkdirLog = "create dir success " + execLocalPath;
        logger.info(mkdirLog);
    }

    /**
     * 将内容写入文件，如果父目录不存在则自动创建。
     *
     * @param content 要写入的文本内容
     * @param filePath 目标文件路径
     * @return 写入成功返回true，失败返回false
     */
    public static boolean writeContent2File(String content, String filePath) {
        FileOutputStream fos = null;
        try {
            File distFile = new File(filePath);
            if (!distFile.getParentFile().exists() && !distFile.getParentFile().mkdirs()) {
                logger.error("mkdir parent failed");
                return false;
            }
            fos = new FileOutputStream(filePath);
            IOUtils.write(content, fos, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            IOUtils.closeQuietly(fos);
        }
        return true;
    }

    /**
     * 删除文件或目录。如果是目录，则删除该目录及其所有子目录。
     * 与File.delete()的区别在于：目录不必为空即可删除，且删除失败时不会抛出异常。
     *
     * @param filename 文件或目录的路径
     */
    public static void deleteFile(String filename) {
        org.apache.commons.io.FileUtils.deleteQuietly(new File(filename));
    }

    /**
     * 获取指定目录下的所有子目录。
     *
     * @param parentDir 父目录路径，不能为空
     * @return 子目录数组
     * @throws RuntimeException 如果parentDir为空或不是有效目录
     */
    public static File[] getAllDir(String parentDir) {
        if (parentDir == null || "".equals(parentDir)) {
            throw new RuntimeException("parentDir can not be empty");
        }

        File file = new File(parentDir);
        if (!file.exists() || !file.isDirectory()) {
            throw new RuntimeException("parentDir not exist, or is not a directory:" + parentDir);
        }

        return file.listFiles(File::isDirectory);
    }

    /**
     * 从输入流中读取内容并转换为字符串。
     *
     * @param inputStream 输入流
     * @return 输入流内容的字符串
     * @throws RuntimeException 读取失败时抛出
     */
    public static String readFile2Str(InputStream inputStream) {

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            return output.toString(UTF_8);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 检测给定的路径字符串是否存在目录遍历风险。
     *
     * @param filename 文件路径字符串
     * @return 如果存在目录遍历风险返回true，否则返回false
     */
    public static boolean directoryTraversal(String filename){
        if (filename.contains(FOLDER_SEPARATOR)) {
            return true;
        }
        File file = new File(filename);
        try {
            File canonical = file.getCanonicalFile();
            File absolute = file.getAbsoluteFile();
            return !canonical.equals(absolute);
        } catch (IOException e) {
            return true;
        }
    }

}
