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
package org.apache.dolphinscheduler.api.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件工具类。提供文件复制、文件与Resource对象转换、MultipartFile转字符串等功能。
 * 该类为工具类。
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 将上传文件的输入流复制到目标文件。
     *
     * @param file         上传的MultipartFile文件
     * @param destFilename 目标文件路径
     */
    public static void copyInputStreamToFile(MultipartFile file, String destFilename) {
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(file.getInputStream(), new File(destFilename));
        } catch (IOException e) {
            logger.error("failed to copy file , {} is empty file", file.getOriginalFilename(), e);
        }
    }

    /**
     * 将文件名转换为Spring Resource对象。如果文件不可读则返回null。
     *
     * @param filename 文件路径名称
     * @return Resource对象，文件不可读时返回null
     * @throws MalformedURLException 文件路径格式异常
     */
    public static Resource file2Resource(String filename) throws MalformedURLException {
        Path file = Paths.get(filename);

        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            logger.error("file can not read : {}", filename);

        }
        return null;
    }

    /**
     * 将MultipartFile文件内容转换为UTF-8格式的字符串。
     *
     * @param file MultipartFile文件
     * @return 文件内容的字符串表示，转换失败返回空字符串
     */
    public static String file2String(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("file convert to string failed: {}", file.getName());
        }

        return "";
    }
}
