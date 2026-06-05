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

package org.apache.dolphinscheduler.common.constants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * 全局常量类。
 * 定义DolphinScheduler系统各模块共用的常量，包括：
 * - 注册中心路径（ZooKeeper节点路径）
 * - 资源存储类型与配置键
 * - 权限与角色定义
 * - HTTP/网络相关常量
 * - 工作流与任务状态常量
 * - Hadoop/YARN/Kerberos相关配置
 * - SPI插件参数字段名
 * - 数据类型和校验规则常量
 *
 * 该类为工具类，不可实例化。
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Construct Constants");
    }

    // ==================== 配置路径与格式 ====================

    /** common.properties配置文件路径 */
    public static final String COMMON_PROPERTIES_PATH = "/common.properties";

    // ==================== 注册中心路径 ====================

    /** ZooKeeper中Master节点注册路径 */
    public static final String REGISTRY_DOLPHINSCHEDULER_MASTERS = "/nodes/master";
    /** ZooKeeper中Worker节点注册路径 */
    public static final String REGISTRY_DOLPHINSCHEDULER_WORKERS = "/nodes/worker";
    /** ZooKeeper中节点根路径 */
    public static final String REGISTRY_DOLPHINSCHEDULER_NODE = "/nodes";
    /** ZooKeeper中Master分布式锁路径 */
    public static final String REGISTRY_DOLPHINSCHEDULER_LOCK_MASTERS = "/lock/masters";
    /** ZooKeeper中Master故障转移锁路径 */
    public static final String REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS = "/lock/failover/masters";

    // ==================== 字符串格式化模板 ====================

    /** 简单拼接格式: %s%s */
    public static final String FORMAT_SS = "%s%s";
    /** 路径格式: %s/%s */
    public static final String FORMAT_S_S = "%s/%s";
    /** 冒号分隔格式: %s:%s */
    public static final String FORMAT_S_S_COLON = "%s:%s";
    /** 文件夹分隔符 */
    public static final String FOLDER_SEPARATOR = "/";

    // ==================== 资源类型 ====================

    /** 文件资源类型标识 */
    public static final String RESOURCE_TYPE_FILE = "resources";
    /** UDF资源类型标识 */
    public static final String RESOURCE_TYPE_UDF = "udfs";

    // ==================== 存储类型 ====================

    /** AWS S3存储类型 */
    public static final String STORAGE_S3 = "S3";
    /** 阿里云OSS存储类型 */
    public static final String STORAGE_OSS = "OSS";
    /** HDFS存储类型 */
    public static final String STORAGE_HDFS = "HDFS";

    /** 空字符串常量 */
    public static final String EMPTY_STRING = "";

    // ==================== 资源存储与Hadoop配置 ====================

    /** HDFS默认文件系统配置键 */
    public static final String FS_DEFAULT_FS = "resource.hdfs.fs.defaultFS";
    /** HDFS默认文件系统属性名（与hdfs-site.xml中保持一致） */
    public static final String HDFS_DEFAULT_FS = "fs.defaultFS";
    /** ResourceManager活跃状态的标识 */
    public static final String HADOOP_RM_STATE_ACTIVE = "ACTIVE";
    /** ResourceManager HTTP地址端口配置键 */
    public static final String HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT = "resource.manager.httpaddress.port";
    /** YARN ResourceManager HA RM IDs配置键 */
    public static final String YARN_RESOURCEMANAGER_HA_RM_IDS = "yarn.resourcemanager.ha.rm.ids";
    /** YARN应用状态地址配置键 */
    public static final String YARN_APPLICATION_STATUS_ADDRESS = "yarn.application.status.address";
    /** YARN任务历史状态地址配置键 */
    public static final String YARN_JOB_HISTORY_STATUS_ADDRESS = "yarn.job.history.status.address";
    /** HDFS操作用户配置键 */
    public static final String HDFS_ROOT_USER = "resource.hdfs.root.user";
    /** 资源上传基础路径配置键 */
    public static final String RESOURCE_UPLOAD_PATH = "resource.storage.upload.base.path";

    // ==================== 数据与文件路径 ====================

    /** 数据目录基础路径配置键 */
    public static final String DATA_BASEDIR_PATH = "data.basedir.path";
    /** DolphinScheduler环境路径配置键 */
    public static final String DOLPHINSCHEDULER_ENV_PATH = "dolphinscheduler.env.path";
    /** 环境属性文件默认路径 */
    public static final String ENV_PATH = "dolphinscheduler_env.sh";

    // ==================== 资源查看与开发配置 ====================

    /** 资源查看支持的后缀配置键 */
    public static final String RESOURCE_VIEW_SUFFIXES = "resource.view.suffixs";
    /** 资源查看支持的默认后缀列表 */
    public static final String RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE =
            "txt,log,sh,bat,conf,cfg,py,java,sql,xml,hql,properties,json,yml,yaml,ini,js";
    /** 开发状态配置键 */
    public static final String DEVELOPMENT_STATE = "development.state";
    /** 是否启用sudo的配置键 */
    public static final String SUDO_ENABLE = "sudo.enable";
    /** 是否将任务目录设置为租户目录的配置键 */
    public static final String SET_TASK_DIR_TO_TENANT_ENABLE = "setTaskDirToTenant.enable";

    // ==================== 资源存储类型与云存储配置 ====================

    /** 资源存储类型配置键 */
    public static final String RESOURCE_STORAGE_TYPE = "resource.storage.type";
    /** AWS S3 Bucket名称配置键 */
    public static final String AWS_S3_BUCKET_NAME = "resource.aws.s3.bucket.name";
    /** AWS S3端点配置键 */
    public static final String AWS_END_POINT = "resource.aws.s3.endpoint";
    /** 阿里云OSS Bucket名称配置键 */
    public static final String ALIBABA_CLOUD_OSS_BUCKET_NAME = "resource.alibaba.cloud.oss.bucket.name";
    /** 阿里云OSS端点配置键 */
    public static final String ALIBABA_CLOUD_OSS_END_POINT = "resource.alibaba.cloud.oss.endpoint";

    // ==================== 特殊字符常量 ====================

    /** 逗号分隔符 */
    public static final String COMMA = ",";
    /** 冒号分隔符 */
    public static final String COLON = ":";
    /** 句点分隔符 */
    public static final String PERIOD = ".";
    /** 问号分隔符 */
    public static final String QUESTION = "?";
    /** 空格字符 */
    public static final String SPACE = " ";
    /** 单斜杠 */
    public static final String SINGLE_SLASH = "/";
    /** 双斜杠 */
    public static final String DOUBLE_SLASH = "//";
    /** 等号 */
    public static final String EQUAL_SIGN = "=";
    /** AT符号 */
    public static final String AT_SIGN = "@";
    /** 斜杠分隔符 */
    public static final String SLASH = "/";
    /** 分号分隔符 */
    public static final String SEMICOLON = ";";

    // ==================== 数据库连接常用键名 ====================

    public static final String ADDRESS = "address";
    public static final String DATABASE = "database";
    public static final String OTHER = "other";
    public static final String USER = "user";
    public static final String JDBC_URL = "jdbcUrl";

    // ==================== 导入/复制后缀 ====================

    /** 导入数据标识后缀 */
    public static final String IMPORT_SUFFIX = "_import_";
    /** 复制数据标识后缀 */
    public static final String COPY_SUFFIX = "_copy_";

    // ==================== HTTP与网络配置 ====================

    /** HTTP连接超时时间（毫秒） */
    public static final int HTTP_CONNECT_TIMEOUT = 60 * 1000;
    /** HTTP连接请求超时时间（毫秒） */
    public static final int HTTP_CONNECTION_REQUEST_TIMEOUT = 60 * 1000;
    /** HTTP Socket超时时间（毫秒） */
    public static final int SOCKET_TIMEOUT = 60 * 1000;
    /** 注册中心会话超时时间（毫秒） */
    public static final int REGISTRY_SESSION_TIMEOUT = 10 * 1000;
    /** HTTP未知请求头标识 */
    public static final String HTTP_HEADER_UNKNOWN = "unKnown";
    /** HTTP X-Forwarded-For 请求头 */
    public static final String HTTP_X_FORWARDED_FOR = "X-Forwarded-For";
    /** HTTP X-Real-IP 请求头 */
    public static final String HTTP_X_REAL_IP = "X-Real-IP";

    // ==================== 字符编码与校验 ====================

    /** UTF-8 字符编码 */
    public static final String UTF_8 = "UTF-8";
    /** 用户名正则校验规则：3-39位字母数字及._- */
    public static final Pattern REGEX_USER_NAME = Pattern.compile("^[a-zA-Z0-9._-]{3,39}$");

    // ==================== 权限常量 ====================

    /** 读权限位（二进制010） */
    public static final int READ_PERMISSION = 2;
    /** 写权限位（二进制100） */
    public static final int WRITE_PERMISSION = 2 * 2;
    /** 执行权限位（二进制001） */
    public static final int EXECUTE_PERMISSION = 1;
    /** 默认管理员权限（读写执行: 111 = 7） */
    public static final int DEFAULT_ADMIN_PERMISSION = 7;
    /** 默认HashMap初始大小 */
    public static final int DEFAULT_HASH_MAP_SIZE = 16;
    /** 所有权限（读写执行组合） */
    public static final int ALL_PERMISSIONS = READ_PERMISSION | WRITE_PERMISSION | EXECUTE_PERMISSION;

    // ==================== 任务超时与权重 ====================

    /** 任务最大超时时间（秒）: 24小时 */
    public static final int MAX_TASK_TIMEOUT = 24 * 3600;
    /** Worker主机默认权重 */
    public static final int DEFAULT_WORKER_HOST_WEIGHT = 100;
    /** 秒到分钟转换单位 */
    public static final int SEC_2_MINUTES_TIME_UNIT = 60;

    // ==================== RPC与运行标志 ====================

    /** RPC端口配置键 */
    public static final String RPC_PORT = "rpc.port";
    /** 流程节点禁止运行标志 */
    public static final String FLOWNODE_RUN_FLAG_FORBIDDEN = "FORBIDDEN";
    /** 流程节点正常运行标志 */
    public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";

    // ==================== 通用字符串常量 ====================

    /** 通用任务类型 */
    public static final String COMMON_TASK_TYPE = "common";
    public static final String DEFAULT = "default";
    public static final String PASSWORD = "password";
    /** 密码脱敏显示字符串 */
    public static final String XXXXXX = "******";
    public static final String NULL = "NULL";
    /** Master服务器线程名前缀 */
    public static final String THREAD_NAME_MASTER_SERVER = "Master-Server";
    /** Worker服务器线程名前缀 */
    public static final String THREAD_NAME_WORKER_SERVER = "Worker-Server";
    /** Alert服务器线程名前缀 */
    public static final String THREAD_NAME_ALERT_SERVER = "Alert-Server";

    // ==================== 定时与休眠相关 ====================

    /** 补数默认Cron表达式: 每天0点 */
    public static final String DEFAULT_CRON_STRING = "0 0 0 * * ? *";
    /** 休眠时长（1秒，单位毫秒） */
    public static final long SLEEP_TIME_MILLIS = 1_000L;
    /** 短休眠时长（100毫秒） */
    public static final long SLEEP_TIME_MILLIS_SHORT = 100L;
    /** 服务器关闭等待时间（3秒） */
    public static final Duration SERVER_CLOSE_WAIT_TIME = Duration.ofSeconds(3);
    /** 1秒对应的毫秒数 */
    public static final long SECOND_TIME_MILLIS = 1_000L;
    /** Master任务实例缓存刷新间隔（20秒） */
    public static final long CACHE_REFRESH_TIME_MILLIS = 20 * 1_000L;
    /** ZooKeeper心跳信息长度 */
    public static final int HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH = 14;

    // ==================== Java/Hadoop配置 ====================

    /** Jar文件类型标识 */
    public static final String JAR = "jar";
    /** Hadoop标识 */
    public static final String HADOOP = "hadoop";
    /** JVM属性设置前缀 -D */
    public static final String D = "-D";

    // ==================== 退出码与版本 ====================

    /** 程序成功退出码 */
    public static final int EXIT_CODE_SUCCESS = 0;
    /** 程序失败退出码 */
    public static final int EXIT_CODE_FAILURE = -1;
    /** 流程/任务定义失败标识 */
    public static final int DEFINITION_FAILURE = -1;
    /** 相反值标识 */
    public static final int OPPOSITE_VALUE = -1;
    /** 流程/任务定义第一版本号 */
    public static final int VERSION_FIRST = 1;

    // ==================== 工作流与任务状态 ====================

    /** 已接受状态 */
    public static final String ACCEPTED = "ACCEPTED";
    /** 成功状态 */
    public static final String SUCCEEDED = "SUCCEEDED";
    /** 已结束状态 */
    public static final String ENDED = "ENDED";
    /** 新建状态 */
    public static final String NEW = "NEW";
    /** 正在新建状态 */
    public static final String NEW_SAVING = "NEW_SAVING";
    /** 已提交状态 */
    public static final String SUBMITTED = "SUBMITTED";
    /** 失败状态 */
    public static final String FAILED = "FAILED";
    /** 已杀死状态 */
    public static final String KILLED = "KILLED";
    /** 运行中状态 */
    public static final String RUNNING = "RUNNING";

    // ==================== 其他字符串常量 ====================

    /** 下划线字符 */
    public static final String UNDERLINE = "_";
    /** YARN Application ID正则表达式: application_数字_数字 */
    public static final String APPLICATION_REGEX = "application_\\d+_\\d+";
    /** 进程ID标识（Windows下为handle，其他系统为pid） */
    public static final String PID = SystemUtils.IS_OS_WINDOWS ? "handle" : "pid";

    // ==================== 运算符字符常量 ====================

    /** 减号 */
    public static final char SUBTRACT_CHAR = '-';
    /** 加号 */
    public static final char ADD_CHAR = '+';
    /** 乘号 */
    public static final char MULTIPLY_CHAR = '*';
    /** 除号 */
    public static final char DIVISION_CHAR = '/';
    /** 左括号 */
    public static final char LEFT_BRACE_CHAR = '(';
    /** 右括号 */
    public static final char RIGHT_BRACE_CHAR = ')';
    /** 加号字符串 */
    public static final String ADD_STRING = "+";
    /** 星号字符串 */
    public static final String STAR = "*";
    /** 除号字符串 */
    public static final String DIVISION_STRING = "/";
    /** 左括号字符串 */
    public static final String LEFT_BRACE_STRING = "(";
    /** 字符P（用于参数类型标识） */
    public static final char P = 'P';
    /** 字符N（用于参数类型标识） */
    public static final char N = 'N';
    /** 减号字符串 */
    public static final String SUBTRACT_STRING = "-";

    // ==================== 工作流参数字段名 ====================

    /** 全局参数JSON字段名 */
    public static final String GLOBAL_PARAMS = "globalParams";
    /** 局部参数JSON字段名 */
    public static final String LOCAL_PARAMS = "localParams";
    /** 子流程实例ID字段名 */
    public static final String SUBPROCESS_INSTANCE_ID = "subProcessInstanceId";
    /** 流程实例状态字段名 */
    public static final String PROCESS_INSTANCE_STATE = "processInstanceState";
    /** 父工作流实例字段名 */
    public static final String PARENT_WORKFLOW_INSTANCE = "parentWorkflowInstance";
    /** 条件结果字段名 */
    public static final String CONDITION_RESULT = "conditionResult";
    /** Switch结果字段名 */
    public static final String SWITCH_RESULT = "switchResult";
    /** 等待开始超时字段名 */
    public static final String WAIT_START_TIMEOUT = "waitStartTimeout";
    /** 依赖字段名 */
    public static final String DEPENDENCE = "dependence";
    /** 任务列表字段名 */
    public static final String TASK_LIST = "taskList";
    /** 队列字段名 */
    public static final String QUEUE = "queue";
    /** 队列名称字段名 */
    public static final String QUEUE_NAME = "queueName";
    /** 日志查询起始行号 */
    public static final int LOG_QUERY_SKIP_LINE_NUMBER = 0;
    /** 日志查询最大行数 */
    public static final int LOG_QUERY_LIMIT = 4096;
    /** 阻塞条件字段名 */
    public static final String BLOCKING_CONDITION = "blockingCondition";
    /** 阻塞时告警标识字段名 */
    public static final String ALERT_WHEN_BLOCKING = "alertWhenBlocking";

    // ==================== ZooKeeper节点类型与操作 ====================

    /** Master节点类型标识 */
    public static final String MASTER_TYPE = "master";
    /** Worker节点类型标识 */
    public static final String WORKER_TYPE = "worker";
    /** 删除操作标识 */
    public static final String DELETE_OP = "delete";
    /** 添加操作标识 */
    public static final String ADD_OP = "add";
    public static final String ALIAS = "alias";
    public static final String CONTENT = "content";
    /** 依赖分割符 */
    public static final String DEPENDENT_SPLIT = ":||";
    /** 依赖全部任务编码标识 */
    public static final long DEPENDENT_ALL_TASK_CODE = 0;

    // ==================== 调度相关 ====================

    /** 预览调度执行次数 */
    public static final int PREVIEW_SCHEDULE_EXECUTE_COUNT = 5;

    // ==================== Kerberos安全认证 ====================

    /** Kerberos认证类型标识 */
    public static final String KERBEROS = "kerberos";
    /** Kerberos票据过期时间配置键 */
    public static final String KERBEROS_EXPIRE_TIME = "kerberos.expire.time";
    /** Java安全krb5.conf - Kerberos配置 */
    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";
    /** Java安全krb5.conf路径配置键 */
    public static final String JAVA_SECURITY_KRB5_CONF_PATH = "java.security.krb5.conf.path";
    /** Hadoop安全认证类型配置 */
    public static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";
    /** Hadoop安全认证启动状态配置 */
    public static final String HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE =
            "hadoop.security.authentication.startup.state";
    /** AWS S3启用V4签名配置 */
    public static final String AWS_S3_V4 = "com.amazonaws.services.s3.enableV4";
    /** Keytab登录用户名配置键 */
    public static final String LOGIN_USER_KEY_TAB_USERNAME = "login.user.keytab.username";
    /** Keytab文件路径配置键 */
    public static final String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    // ==================== MDC日志上下文键 ====================

    /** MDC中工作流实例ID的键名 */
    public static final String WORKFLOW_INSTANCE_ID_MDC_KEY = "workflowInstanceId";
    /** MDC中任务实例ID的键名 */
    public static final String TASK_INSTANCE_ID_MDC_KEY = "taskInstanceId";

    // ==================== 日志格式 ====================

    /** 任务日志信息格式化模板 */
    public static final String TASK_LOG_INFO_FORMAT = "TaskLogInfo-%s";

    // ==================== 参数占位符 ====================

    /** 双大括号左（参数占位符起始） */
    public static final String DOUBLE_BRACKETS_LEFT = "{{";
    /** 双大括号右（参数占位符结束） */
    public static final String DOUBLE_BRACKETS_RIGHT = "}}";
    /** 带空格双大括号左 */
    public static final String DOUBLE_BRACKETS_LEFT_SPACE = "{ {";
    /** 带空格双大括号右 */
    public static final String DOUBLE_BRACKETS_RIGHT_SPACE = "} }";

    // ==================== API响应字段名 ====================

    /** 状态字段 */
    public static final String STATUS = "status";
    /** 消息字段 */
    public static final String MSG = "msg";
    /** 数据总数 */
    public static final String COUNT = "count";
    /** 每页大小 */
    public static final String PAGE_SIZE = "pageSize";
    /** 当前页码 */
    public static final String PAGE_NUMBER = "pageNo";
    /** 数据列表 */
    public static final String DATA_LIST = "data";
    /** 总列表 */
    public static final String TOTAL_LIST = "totalList";
    /** 当前页 */
    public static final String CURRENT_PAGE = "currentPage";
    /** 总页数 */
    public static final String TOTAL_PAGE = "totalPage";
    /** 总数 */
    public static final String TOTAL = "total";

    // ==================== 工作流列表字段 ====================

    /** 工作流列表 */
    public static final String WORKFLOW_LIST = "workFlowList";
    /** 工作流关系列表 */
    public static final String WORKFLOW_RELATION_LIST = "workFlowRelationList";

    // ==================== 会话相关 ====================

    /** Session中用户信息的键名 */
    public static final String SESSION_USER = "session.user";
    /** Session ID键名 */
    public static final String SESSION_ID = "sessionId";
    /** 语言/区域设置键名 */
    public static final String LOCALE_LANGUAGE = "language";
    /** Session过期时间（秒）: 7200秒 = 2小时 */
    public static final int SESSION_TIME_OUT = 7200;

    // ==================== 文件与UDF ====================

    /** 最大文件大小（1GB） */
    public static final int MAX_FILE_SIZE = 1024 * 1024 * 1024;
    /** UDF类型标识 */
    public static final String UDF = "UDF";
    /** Java类标识 */
    public static final String CLASS = "class";

    // ==================== Worker组与权限 ====================

    /** 默认Worker组名称 */
    public static final String DEFAULT_WORKER_GROUP = "default";
    /** 可写授权权限值（7: 读写执行） */
    public static final int AUTHORIZE_WRITABLE_PERM = 7;
    /** 可读授权权限值（4: 只读） */
    public static final int AUTHORIZE_READABLE_PERM = 4;

    // ==================== 节点状态 ====================

    /** 正常节点状态 */
    public static final int NORMAL_NODE_STATUS = 0;
    /** 异常节点状态 */
    public static final int ABNORMAL_NODE_STATUS = 1;
    /** 繁忙节点状态 */
    public static final int BUSY_NODE_STATUE = 2;

    // ==================== 时间相关字段 ====================

    public static final String START_TIME = "start time";
    public static final String END_TIME = "end time";
    public static final String START_END_DATE = "startDate,endDate";

    // ==================== 系统属性 ====================

    /** 操作系统行分隔符 */
    public static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");
    /** 首选网络接口配置键 */
    public static final String DOLPHIN_SCHEDULER_NETWORK_INTERFACE_PREFERRED =
            "dolphin.scheduler.network.interface.preferred";
    /** 网络IP优先级策略配置键 */
    public static final String DOLPHIN_SCHEDULER_NETWORK_PRIORITY_STRATEGY =
            "dolphin.scheduler.network.priority.strategy";

    // ==================== Shell命令 ====================

    /** Shell解释器命令 */
    public static final String SH = "sh";
    /** pstree命令：获取父进程ID和子进程ID */
    public static final String PSTREE = "pstree";

    // ==================== Kubernetes ====================

    /** 判断是否运行在Kubernetes环境中 */
    public static final boolean KUBERNETES_MODE = !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_HOST"))
            && !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_PORT"));
    /** K8s本地测试集群编码 */
    public static final Long K8S_LOCAL_TEST_CLUSTER_CODE = 0L;

    // ==================== 试运行标志 ====================

    /** 非试运行模式 */
    public static final int DRY_RUN_FLAG_NO = 0;
    /** 试运行模式 */
    public static final int DRY_RUN_FLAG_YES = 1;

    /** 数据质量错误输出路径配置键 */
    public static final String DATA_QUALITY_ERROR_OUTPUT_PATH = "data-quality.error.output.path";

    /** Redis缓存键名：获取全部数据的标识 */
    public static final String CACHE_KEY_VALUE_ALL = "'all'";

    /** K8s命名空间 */
    public static final String NAMESPACE = "namespace";
    /** K8s集群 */
    public static final String CLUSTER = "cluster";
    /** CPU资源限制 */
    public static final String LIMITS_CPU = "limitsCpu";
    /** 内存资源限制 */
    public static final String LIMITS_MEMORY = "limitsMemory";

    // ==================== 长度限制 ====================

    /** 调度时区配置键 */
    public static final String SCHEDULE_TIMEZONE = "schedule_timezone";
    /** 资源全名最大长度 */
    public static final int RESOURCE_FULL_NAME_MAX_LENGTH = 128;
    /** 租户全名最大长度 */
    public static final int TENANT_FULL_NAME_MAX_LENGTH = 30;
    /** 调度时间最大长度（防止日期数据过大影响内存） */
    public static final int SCHEDULE_TIME_MAX_LENGTH = 100;
    /** 用户密码最大长度 */
    public static final int USER_PASSWORD_MAX_LENGTH = 20;
    /** 用户密码最小长度 */
    public static final int USER_PASSWORD_MIN_LENGTH = 2;

    // ==================== 函数与默认值 ====================

    /** 函数以$开头标识 */
    public static final String FUNCTION_START_WITH = "$";
    /** 默认队列ID */
    public static final Integer DEFAULT_QUEUE_ID = 1;

    // ==================== 安全认证类型 ====================

    /** 安全配置类型键名（支持PASSWORD和LDAP） */
    public static final String SECURITY_CONFIG_TYPE = "securityConfigType";
    /** 密码认证类型 */
    public static final String SECURITY_CONFIG_TYPE_PASSWORD = "PASSWORD";
    /** LDAP认证类型 */
    public static final String SECURITY_CONFIG_TYPE_LDAP = "LDAP";

    // ==================== 任务类型分类 ====================

    /** 通用类型 */
    public static final String TYPE_UNIVERSAL = "Universal";
    /** 数据集成类型 */
    public static final String TYPE_DATA_INTEGRATION = "DataIntegration";
    /** 云服务类型 */
    public static final String TYPE_CLOUD = "Cloud";
    /** 逻辑类型 */
    public static final String TYPE_LOGIC = "Logic";
    /** 数据质量类型 */
    public static final String TYPE_DATA_QUALITY = "DataQuality";
    /** 其他类型 */
    public static final String TYPE_OTHER = "Other";
    /** 机器学习类型 */
    public static final String TYPE_MACHINE_LEARNING = "MachineLearning";

    // ==================== SPI插件参数字段名 ====================

    /** SPI插件参数字段 */
    public static final String STRING_PLUGIN_PARAM_FIELD = "field";
    /** SPI插件参数名称 */
    public static final String STRING_PLUGIN_PARAM_NAME = "name";
    /** SPI插件参数属性 */
    public static final String STRING_PLUGIN_PARAM_PROPS = "props";
    /** SPI插件参数类型 */
    public static final String STRING_PLUGIN_PARAM_TYPE = "type";
    /** SPI插件参数标题 */
    public static final String STRING_PLUGIN_PARAM_TITLE = "title";
    /** SPI插件参数值 */
    public static final String STRING_PLUGIN_PARAM_VALUE = "value";
    /** SPI插件参数校验 */
    public static final String STRING_PLUGIN_PARAM_VALIDATE = "validate";
    /** SPI插件参数选项 */
    public static final String STRING_PLUGIN_PARAM_OPTIONS = "options";
    /** SPI插件参数emit（触发事件） */
    public static final String STRING_PLUGIN_PARAM_EMIT = "emit";

    // ==================== 布尔字符串常量 ====================

    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";
    public static final String STRING_YES = "YES";
    public static final String STRING_NO = "NO";

    // ==================== 其他配置 ====================

    public static final String SMALL = "small";
    public static final String CHANGE = "change";

    /** 支持Hive数据源单Session模式配置 */
    public static final String SUPPORT_HIVE_ONE_SESSION = "support.hive.oneSession";

    /** Kerberos认证主体 */
    public static final String PRINCIPAL = "principal";
    /** Oracle数据库连接类型 */
    public static final String ORACLE_DB_CONNECT_TYPE = "connectType";
    /** Kerberos krb5.conf路径 */
    public static final String KERBEROS_KRB5_CONF_PATH = "javaSecurityKrb5Conf";
    /** Kerberos Keytab用户名 */
    public static final String KERBEROS_KEY_TAB_USERNAME = "loginUserKeytabUsername";
    /** Kerberos Keytab文件路径 */
    public static final String KERBEROS_KEY_TAB_PATH = "loginUserKeytabPath";
}
