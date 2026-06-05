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

package org.apache.dolphinscheduler.service.storage.impl;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_UDF;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.KerberosHttpClient;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.service.storage.StorageOperate;
import org.apache.dolphinscheduler.service.utils.CommonUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Hadoop HDFS工具类，单例模式。封装了HDFS文件系统的所有操作，包括文件上传下载、目录管理、Kerberos认证、
 * YARN应用状态查询（支持HA）、文件内容查看等。通过 LoadingCache 实现带过期时间的单例管理。
 */
public class HadoopUtils implements Closeable, StorageOperate {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtils.class);
    private String hdfsUser;
    public static final String RM_HA_IDS = PropertyUtils.getString(Constants.YARN_RESOURCEMANAGER_HA_RM_IDS);
    public static final String APP_ADDRESS = PropertyUtils.getString(Constants.YARN_APPLICATION_STATUS_ADDRESS);
    public static final String JOB_HISTORY_ADDRESS = PropertyUtils.getString(Constants.YARN_JOB_HISTORY_STATUS_ADDRESS);
    public static final int HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE =
            PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);
    private static final String HADOOP_UTILS_KEY = "HADOOP_UTILS_KEY";

    private static final LoadingCache<String, HadoopUtils> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(PropertyUtils.getInt(Constants.KERBEROS_EXPIRE_TIME, 2), TimeUnit.HOURS)
            .build(new CacheLoader<String, HadoopUtils>() {

                @Override
                public HadoopUtils load(String key) throws Exception {
                    return new HadoopUtils();
                }
            });

    private volatile boolean yarnEnabled = false;

    private Configuration configuration;
    private FileSystem fs;

    private HadoopUtils() {
        hdfsUser = PropertyUtils.getString(Constants.HDFS_ROOT_USER);
        init();
        initHdfsPath();
    }

    public static HadoopUtils getInstance() {
        return cache.getUnchecked(HADOOP_UTILS_KEY);
    }

    /**
     * init dolphinscheduler root path in hdfs
     */

    private void initHdfsPath() {
        Path path = new Path(RESOURCE_UPLOAD_PATH);
        try {
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * init hadoop configuration
     */
    private void init() throws NullPointerException {
        try {
            configuration = new HdfsConfiguration();

            if (CommonUtils.loadKerberosConf(configuration)) {
                hdfsUser = "";
            }

            String defaultFS = configuration.get(Constants.FS_DEFAULT_FS);

            if (StringUtils.isBlank(defaultFS)) {
                defaultFS = PropertyUtils.getString(Constants.FS_DEFAULT_FS);
            }

            // first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
            // the default is the local file system
            if (StringUtils.isNotBlank(defaultFS)) {
                Map<String, String> fsRelatedProps = PropertyUtils.getPrefixedProperties("fs.");
                configuration.set(Constants.HDFS_DEFAULT_FS, defaultFS);
                fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
            } else {
                logger.error("property:{} can not to be empty, please set!", Constants.FS_DEFAULT_FS);
                throw new NullPointerException(
                        String.format("property: %s can not to be empty, please set!", Constants.FS_DEFAULT_FS));
            }

            if (!defaultFS.startsWith("file")) {
                logger.info("get property:{} -> {}, from core-site.xml hdfs-site.xml ", Constants.FS_DEFAULT_FS,
                        defaultFS);
            }

            if (StringUtils.isNotEmpty(hdfsUser)) {
                UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                ugi.doAs((PrivilegedExceptionAction<Boolean>) () -> {
                    fs = FileSystem.get(configuration);
                    return true;
                });
            } else {
                logger.warn("resource.hdfs.root.user is not set value!");
                fs = FileSystem.get(configuration);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取Hadoop Configuration 配置对象。
     *
     * @return the Hadoop Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 获取HDFS默认文件系统URI。
     *
     * @return the default file system URI
     */
    public String getDefaultFS() {
        return getConfiguration().get(Constants.FS_DEFAULT_FS);
    }

    /**
     * 获取YARN应用的URL地址。支持单ResourceManager和HA模式。
     * 如果 rmHaIds 为空则为单ResourceManager模式，不为空则启用HA模式自动查找活跃的ResourceManager。
     *
     * @param applicationId the YARN application id
     * @return the URL of the application
     * @throws BaseException if URL generation fails
     */
    public String getApplicationUrl(String applicationId) throws BaseException {

        yarnEnabled = true;
        String appUrl = StringUtils.isEmpty(RM_HA_IDS) ? APP_ADDRESS : getAppAddress(APP_ADDRESS, RM_HA_IDS);
        if (StringUtils.isBlank(appUrl)) {
            throw new BaseException("yarn application url generation failed");
        }
        logger.debug("yarn application url:{}, applicationId:{}", appUrl, applicationId);
        return String.format(appUrl, HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE, applicationId);
    }

    public String getJobHistoryUrl(String applicationId) {
        // eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(JOB_HISTORY_ADDRESS, jobId);
    }

    /**
     * 读取HDFS文件内容并返回字节数组。
     *
     * @param hdfsFilePath the HDFS file path
     * @return byte array of file content
     * @throws IOException if I/O errors occur
     */
    public byte[] catFile(String hdfsFilePath) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            logger.error("hdfs file path:{} is blank", hdfsFilePath);
            return new byte[0];
        }

        try (FSDataInputStream fsDataInputStream = fs.open(new Path(hdfsFilePath))) {
            return IOUtils.toByteArray(fsDataInputStream);
        }
    }

    /**
     * 分页读取HDFS文件内容，跳过指定行数后读取指定行数的内容。
     *
     * @param hdfsFilePath the HDFS file path
     * @param skipLineNums lines to skip
     * @param limit maximum lines to read
     * @return list of file lines
     * @throws IOException if I/O errors occur
     */
    public List<String> catFile(String hdfsFilePath, int skipLineNums, int limit) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            logger.error("hdfs file path:{} is blank", hdfsFilePath);
            return Collections.emptyList();
        }

        try (FSDataInputStream in = fs.open(new Path(hdfsFilePath))) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Stream<String> stream = br.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public List<String> vimFile(String bucketName, String hdfsFilePath, int skipLineNums,
                                int limit) throws IOException {
        return catFile(hdfsFilePath, skipLineNums, limit);
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws IOException {
        getInstance().mkdir(tenantCode, getHdfsResDir(tenantCode));
        getInstance().mkdir(tenantCode, getHdfsUdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getHdfsResDir(tenantCode);
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getHdfsUdfDir(tenantCode);
    }

    /**
     * make the given file and all non-existent parents into
     * directories. Has the semantics of Unix 'mkdir -p'.
     * Existence of the directory hierarchy is not an error.
     *
     * @param hdfsPath path to create
     * @return mkdir result
     * @throws IOException errors
     */
    @Override
    public boolean mkdir(String tenantCode, String hdfsPath) throws IOException {
        return fs.mkdirs(new Path(hdfsPath));
    }

    @Override
    public String getResourceFileName(String tenantCode, String fullName) {
        return getHdfsResourceFileName(tenantCode, fullName);
    }

    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        return getHdfsFileName(resourceType, tenantCode, fileName);
    }

    @Override
    public void download(String bucketName, String srcHdfsFilePath, String dstFile, boolean deleteSource,
                         boolean overwrite) throws IOException {
        copyHdfsToLocal(srcHdfsFilePath, dstFile, deleteSource, overwrite);
    }

    /**
     * 在HDFS文件系统之间复制文件。
     *
     * @param srcPath the source HDFS path
     * @param dstPath the destination HDFS path
     * @param deleteSource whether to delete the source file after copy
     * @param overwrite whether to overwrite existing file
     * @return true if copy succeeds
     * @throws IOException if I/O errors occur
     */
    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        return FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), deleteSource, overwrite, fs.getConf());
    }

    /**
     * 将本地文件复制到HDFS文件系统。
     *
     * @param srcFile the local source file
     * @param dstHdfsPath the destination HDFS path
     * @param deleteSource whether to delete the source file after copy
     * @param overwrite whether to overwrite existing file
     * @return true if copy succeeds
     * @throws IOException if I/O errors occur
     */
    public boolean copyLocalToHdfs(String srcFile, String dstHdfsPath, boolean deleteSource,
                                   boolean overwrite) throws IOException {
        Path srcPath = new Path(srcFile);
        Path dstPath = new Path(dstHdfsPath);

        fs.copyFromLocalFile(deleteSource, overwrite, srcPath, dstPath);

        return true;
    }

    @Override
    public boolean upload(String buckName, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        return copyLocalToHdfs(srcFile, dstPath, deleteSource, overwrite);
    }

    /*
     * copy hdfs file to local
     *
     * @param srcHdfsFilePath source hdfs file path
     *
     * @param dstFile destination file
     *
     * @param deleteSource delete source
     *
     * @param overwrite overwrite
     *
     * @return result of copy hdfs file to local
     *
     * @throws IOException errors
     */
    public boolean copyHdfsToLocal(String srcHdfsFilePath, String dstFile, boolean deleteSource,
                                   boolean overwrite) throws IOException {
        Path srcPath = new Path(srcHdfsFilePath);
        File dstPath = new File(dstFile);

        if (dstPath.exists()) {
            if (dstPath.isFile()) {
                if (overwrite) {
                    Files.delete(dstPath.toPath());
                }
            } else {
                logger.error("destination file must be a file");
            }
        }

        if (!dstPath.getParentFile().exists() && !dstPath.getParentFile().mkdirs()) {
            return false;
        }

        return FileUtil.copy(fs, srcPath, dstPath, deleteSource, fs.getConf());
    }

    /**
     * 删除HDFS上的文件或目录。如果目标是目录且 recursive 为 true，则递归删除目录及其内容。
     *
     * @param tenantCode the tenant code
     * @param hdfsFilePath the HDFS path to delete
     * @param recursive whether to delete recursively
     * @return true if deletion succeeds
     * @throws IOException if I/O errors occur
     */
    @Override
    public boolean delete(String tenantCode, String hdfsFilePath, boolean recursive) throws IOException {
        return fs.delete(new Path(hdfsFilePath), recursive);
    }

    /**
     * 检查HDFS文件或目录是否存在。
     *
     * @param tenantCode the tenant code
     * @param hdfsFilePath the HDFS path to check
     * @return true if the path exists
     * @throws IOException if I/O errors occur
     */
    @Override
    public boolean exists(String tenantCode, String hdfsFilePath) throws IOException {
        return fs.exists(new Path(hdfsFilePath));
    }

    /**
     * 获取HDFS目录下的文件列表。
     *
     * @param filePath the HDFS directory path
     * @return array of file statuses
     * @throws IOException if I/O errors occur
     */
    public FileStatus[] listFileStatus(String filePath) throws IOException {
        try {
            return fs.listStatus(new Path(filePath));
        } catch (IOException e) {
            logger.error("Get file list exception", e);
            throw new IOException("Get file list exception", e);
        }
    }

    /**
     * 重命名HDFS上的文件或目录。
     *
     * @param src the source path
     * @param dst the destination path
     * @return true if rename succeeds
     * @throws IOException if I/O errors occur
     */
    public boolean rename(String src, String dst) throws IOException {
        return fs.rename(new Path(src), new Path(dst));
    }

    /**
     * 判断YARN ResourceManager是否已启用。
     *
     * @return true if YARN is enabled
     */
    public boolean isYarnEnabled() {
        return yarnEnabled;
    }

    /**
     * 获取YARN应用的执行状态。首先从ResourceManager查询，查询不到则从JobHistory查询。
     *
     * @param applicationId the YARN application id
     * @return the application execution status, or null if not found
     * @throws BaseException if error occurs during status retrieval
     */
    public TaskExecutionStatus getApplicationStatus(String applicationId) throws BaseException {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String result;
        String applicationUrl = getApplicationUrl(applicationId);
        logger.debug("generate yarn application url, applicationUrl={}", applicationUrl);

        String responseContent = Boolean.TRUE
                .equals(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                        ? KerberosHttpClient.get(applicationUrl)
                        : HttpUtils.get(applicationUrl);
        if (responseContent != null) {
            ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
            if (!jsonObject.has("app")) {
                return TaskExecutionStatus.FAILURE;
            }
            result = jsonObject.path("app").path("finalStatus").asText();

        } else {
            // may be in job history
            String jobHistoryUrl = getJobHistoryUrl(applicationId);
            logger.debug("generate yarn job history application url, jobHistoryUrl={}", jobHistoryUrl);
            responseContent = Boolean.TRUE
                    .equals(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                            ? KerberosHttpClient.get(jobHistoryUrl)
                            : HttpUtils.get(jobHistoryUrl);

            if (null != responseContent) {
                ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
                if (!jsonObject.has("job")) {
                    return TaskExecutionStatus.FAILURE;
                }
                result = jsonObject.path("job").path("state").asText();
            } else {
                return TaskExecutionStatus.FAILURE;
            }
        }

        return getExecutionStatus(result);
    }

    private TaskExecutionStatus getExecutionStatus(String result) {
        switch (result) {
            case Constants.ACCEPTED:
                return TaskExecutionStatus.SUBMITTED_SUCCESS;
            case Constants.SUCCEEDED:
            case Constants.ENDED:
                return TaskExecutionStatus.SUCCESS;
            case Constants.NEW:
            case Constants.NEW_SAVING:
            case Constants.SUBMITTED:
            case Constants.FAILED:
                return TaskExecutionStatus.FAILURE;
            case Constants.KILLED:
                return TaskExecutionStatus.KILL;
            case Constants.RUNNING:
            default:
                return TaskExecutionStatus.RUNNING_EXECUTION;
        }
    }

    /**
     * 获取HDFS数据基础路径。
     *
     * @return the HDFS data base path
     */
    public static String getHdfsDataBasePath() {
        if (FOLDER_SEPARATOR.equals(RESOURCE_UPLOAD_PATH)) {
            return "";
        } else {
            return RESOURCE_UPLOAD_PATH;
        }
    }

    /**
     * 根据资源类型获取租户在HDFS上的资源目录。
     *
     * @param resourceType the resource type (FILE or UDF)
     * @param tenantCode the tenant code
     * @return the HDFS directory path for the resource type
     */
    public static String getHdfsDir(ResourceType resourceType, String tenantCode) {
        String hdfsDir = "";
        if (resourceType.equals(ResourceType.FILE)) {
            hdfsDir = getHdfsResDir(tenantCode);
        } else if (resourceType.equals(ResourceType.UDF)) {
            hdfsDir = getHdfsUdfDir(tenantCode);
        }
        return hdfsDir;
    }

    @Override
    public String getDir(ResourceType resourceType, String tenantCode) {
        return getHdfsDir(resourceType, tenantCode);
    }

    /**
     * 获取租户在HDFS上的资源文件目录路径。
     *
     * @param tenantCode the tenant code
     * @return the HDFS resource directory for the tenant
     */
    public static String getHdfsResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getHdfsTenantDir(tenantCode));
    }

    /**
     * 获取租户在HDFS上的UDF函数目录路径。
     *
     * @param tenantCode the tenant code
     * @return the HDFS UDF directory for the tenant
     */
    public static String getHdfsUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getHdfsTenantDir(tenantCode));
    }

    /**
     * 获取HDFS上文件的完整路径。
     *
     * @param resourceType the resource type (FILE or UDF)
     * @param tenantCode the tenant code
     * @param fileName the file name
     * @return the full HDFS file path
     */
    public static String getHdfsFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getHdfsDir(resourceType, tenantCode), fileName);
    }

    /**
     * 获取租户资源文件在HDFS上的绝对路径和文件名。
     *
     * @param tenantCode the tenant code
     * @param fileName the file name
     * @return the absolute HDFS path for the resource file
     */
    public static String getHdfsResourceFileName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getHdfsResDir(tenantCode), fileName);
    }

    /**
     * 获取租户UDF文件在HDFS上的绝对路径和文件名。
     *
     * @param tenantCode the tenant code
     * @param fileName the file name
     * @return the absolute HDFS path for the UDF file
     */
    public static String getHdfsUdfFileName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getHdfsUdfDir(tenantCode), fileName);
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on hdfs
     */
    public static String getHdfsTenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getHdfsDataBasePath(), tenantCode);
    }

    /**
     * 解析应用地址，在YARN HA模式下自动查找活跃的ResourceManager地址。
     *
     * @param appAddress the base application address template
     * @param rmHa the ResourceManager HA IDs
     * @return the resolved application address, or null if resolution fails
     */
    public static String getAppAddress(String appAddress, String rmHa) {

        String[] split1 = appAddress.split(Constants.DOUBLE_SLASH);

        if (split1.length != 2) {
            return null;
        }

        String start = split1[0] + Constants.DOUBLE_SLASH;
        String[] split2 = split1[1].split(Constants.COLON);

        if (split2.length != 2) {
            return null;
        }

        String end = Constants.COLON + split2[1];

        // get active ResourceManager
        String activeRM = YarnHAAdminUtils.getActiveRMName(start, rmHa);

        if (StringUtils.isEmpty(activeRM)) {
            return null;
        }

        return start + activeRM + end;
    }

    @Override
    public void close() throws IOException {
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                logger.error("Close HadoopUtils instance failed", e);
                throw new IOException("Close HadoopUtils instance failed", e);
            }
        }
    }

    /**
     * yarn ha admin utils
     */
    private static final class YarnHAAdminUtils {

        /**
         *  get active resourcemanager node
         * @param protocol http protocol
         * @param rmIds yarn ha ids
         * @return yarn active node
         */
        public static String getActiveRMName(String protocol, String rmIds) {

            String[] rmIdArr = rmIds.split(Constants.COMMA);

            String yarnUrl = protocol + "%s:" + HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE + "/ws/v1/cluster/info";

            try {

                /**
                 * send http get request to rm
                 */

                for (String rmId : rmIdArr) {
                    String state = getRMState(String.format(yarnUrl, rmId));
                    if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                        return rmId;
                    }
                }

            } catch (Exception e) {
                logger.error("yarn ha application url generation failed, message:{}", e.getMessage());
            }
            return null;
        }

        /**
         * get ResourceManager state
         */
        public static String getRMState(String url) {

            String retStr = Boolean.TRUE
                    .equals(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                            ? KerberosHttpClient.get(url)
                            : HttpUtils.get(url);

            if (StringUtils.isEmpty(retStr)) {
                return null;
            }
            // to json
            ObjectNode jsonObject = JSONUtils.parseObject(retStr);

            // get ResourceManager state
            if (!jsonObject.has("clusterInfo")) {
                return null;
            }
            return jsonObject.get("clusterInfo").path("haState").asText();
        }

    }

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        String tenantPath = getHdfsDataBasePath() + FOLDER_SEPARATOR + tenantCode;

        if (exists(tenantCode, tenantPath)) {
            delete(tenantCode, tenantPath, true);

        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.HDFS;
    }
}
