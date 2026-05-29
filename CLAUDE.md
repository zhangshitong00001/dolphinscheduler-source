# Apache DolphinScheduler

分布式易扩展的可视化 DAG 工作流调度系统。

## 技术栈

- **语言：** Java 8 (JDK 1.8)、Vue 3 + Vite（前端）
- **框架：** Spring Boot 2.6.1、Spring MVC
- **构建：** Maven（多模块），前端 npm/vite
- **数据库：** MySQL（主库），MyBatis/MyBatis-Plus
- **缓存：** Redis（Spring Cache + RedisTemplate + StringRedisTemplate）
- **注册中心：** Zookeeper（Curator）
- **调度：** Quartz
- **RPC：** Netty（自定义远程通信）
- **部署：** Docker Compose、Kubernetes

## 模块结构

| 模块 | 说明 |
|------|------|
| `dolphinscheduler-api` | API 层，REST 接口（端口 12345，context-path: `/dolphinscheduler/`）|
| `dolphinscheduler-service` | 业务服务层（缓存、Session、告警、队列、日志、存储等）|
| `dolphinscheduler-dao` | 数据访问层（MyBatis Mapper + Entity）|
| `dolphinscheduler-common` | 公共工具类和常量 |
| `dolphinscheduler-master` | Master 节点，负责任务分发和调度 |
| `dolphinscheduler-worker` | Worker 节点，负责任务执行 |
| `dolphinscheduler-alert` | 告警模块（邮件、钉钉、飞书等插件） |
| `dolphinscheduler-remote` | Netty 远程通信层 |
| `dolphinscheduler-registry` | 注册中心（Zookeeper 等插件） |
| `dolphinscheduler-scheduler-plugin` | 调度插件（Quartz） |
| `dolphinscheduler-task-plugin` | 任务插件（Shell、SQL、Spark、Flink、DataX 等 30+） |
| `dolphinscheduler-datasource-plugin` | 数据源插件（MySQL、PostgreSQL、Hive、ClickHouse 等） |
| `dolphinscheduler-ui` | 前端 UI（Vue 3 + AntV X6 流程图） |
| `dolphinscheduler-standalone-server` | 单机模式启动入口 |
| `dolphinscheduler-tools` | 数据库升级/迁移工具 |

## 核心架构

```
API (12345) → Service → DAO → MySQL
                  ↓
             Redis Cache
                  ↓
        Master → Worker (Netty RPC)
                  ↓
          Zookeeper (Registry)
```

- API 层负责认证、鉴权、请求路由
- Service 层处理业务逻辑，集成 Redis 缓存和 Session 管理
- Master 通过 Quartz 调度任务，通过 Netty 下发到 Worker
- Worker 执行具体任务（Shell/SQL/Spark 等），通过任务插件扩展
- 注册中心用 Zookeeper 做服务发现和分布式协调

## 包名规范

```
org.apache.dolphinscheduler.{module}
```

## 关键配置

- **API 端口：** 12345，context-path: `/dolphinscheduler/`
- **Redis：** `spring.redis.host=192.168.2.110:6379`
- **MySQL：** `jdbc:mysql://192.168.2.110:3306/dolphinscheduler`
- **Session 超时：** 120 分钟（Servlet），10 分钟（Redis Session TTL）
- **Session 前缀：** `dolphinscheduler:session:` 和 `dolphinscheduler:user_session:`

## 当前工作（Session 管理改造）

正在将 Session 管理从传统方式改造为 Redis 缓存方案：
- `SessionManager` 接口定义 CRUD（create/getUserId/refresh/destroy/isValid）
- `RedisConfig` 配置 RedisTemplate/StringRedisTemplate/CacheManager
- 缓存区分：session(10min)、metadata(5min)、default(30min)
- 各业务缓存：user、tenant、queue、processDefinition、taskDefinition 等（30min）

## 构建与测试

```bash
# 编译整个项目
mvn clean install -DskipTests

# 编译特定模块
mvn clean install -pl dolphinscheduler-service -DskipTests

# 运行测试
mvn test -pl dolphinscheduler-service

# 代码检查（SpotBugs + Spotless）
mvn verify

# 单机模式启动
cd dolphinscheduler-standalone-server && mvn spring-boot:run
```

## 编码规范

- Google Java Style（Spotless 检查）
- 类文件需包含 Apache License 2.0 头部注释
- Javadoc 写类级别和核心公共方法的注释
- 配置文件用 YAML 格式
- 避免 System.out.println，使用 SLF4J Logger
- 事务注解 @Transactional 在 Service 层使用
- 新增依赖先在 `dolphinscheduler-bom/pom.xml` 声明版本
