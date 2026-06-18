# Apache Doris 数据源插件设计文档

> 日期: 2026-06-17
> 类型: 新数据源插件
> 目标: 为 DolphinScheduler 添加 Apache Doris 数据源支持

## 1. 背景

Apache Doris 是一款高性能 MPP 分析型数据库，兼容 MySQL 协议。DolphinScheduler 目前支持 MySQL 等 12 种数据源，但尚未原生支持 Doris。用户希望添加 Doris 数据源插件，以便在 SQL 任务、数据质量检查等场景中直接连接 Doris。

## 2. 设计决策

| 决策        | 选择                              | 理由                                        |
| ----------- | --------------------------------- | ------------------------------------------- |
| JDBC 驱动   | `mysql-connector-java:8.0.16`     | Doris 完全兼容 MySQL 协议，复用项目已有依赖 |
| 默认端口    | 9030                              | Doris FE 标准查询端口                       |
| JDBC URL    | `jdbc:mysql://host:9030/database` | MySQL 协议 URL 格式                         |
| 验证查询    | `select 1`                        | 标准 MySQL 保活查询                         |
| DbType 枚举 | `DORIS(12, "doris")`              | 延续枚举序号                                |

## 3. 架构

### 3.1 类和模块设计

遵循 DolphinScheduler 数据源插件 SPI 模式（参照 MySQL 插件）：

```
dolphinscheduler-datasource-plugin/
└── dolphinscheduler-datasource-doris/
    ├── pom.xml                                          # Maven: parent + mysql-connector-java
    └── src/
        ├── main/java/org/apache/dolphinscheduler/plugin/datasource/doris/
        │   ├── DorisDataSourceChannelFactory.java       # @AutoService, name="doris"
        │   ├── DorisDataSourceChannel.java              # 工厂创建 Client
        │   ├── DorisDataSourceClient.java               # extends CommonDataSourceClient
        │   └── param/
        │       ├── DorisConnectionParam.java            # extends BaseConnectionParam
        │       ├── DorisDataSourceParamDTO.java         # extends BaseDataSourceParamDTO
        │       └── DorisDataSourceProcessor.java        # JDBC URL 组装 + 参数校验
        └── test/java/org/apache/dolphinscheduler/plugin/datasource/doris/
            ├── DorisDataSourceChannelFactoryTest.java
            ├── DorisDataSourceChannelTest.java
            └── param/DorisDataSourceProcessorTest.java
```

### 3.2 类职责

| 类                              | 父类/接口                     | 职责                                 |
| ------------------------------- | ----------------------------- | ------------------------------------ |
| `DorisDataSourceChannelFactory` | `DataSourceChannelFactory`    | SPI 注册点，name="doris"，优先级 SPI |
| `DorisDataSourceChannel`        | `DataSourceChannel`           | 创建 `DorisDataSourceClient`         |
| `DorisDataSourceClient`         | `CommonDataSourceClient`      | HikariCP 连接池管理                  |
| `DorisConnectionParam`          | `BaseConnectionParam`         | 连接参数序列化（无额外字段）         |
| `DorisDataSourceParamDTO`       | `BaseDataSourceParamDTO`      | API 数据传输，getType()=DbType.DORIS |
| `DorisDataSourceProcessor`      | `AbstractDataSourceProcessor` | JDBC URL 构建、参数校验、驱动名      |

### 3.3 插件加载链路

```
@AutoService(DataSourceChannelFactory.class)
    → META-INF/services/...DataSourceChannelFactory (编译时生成)
        → PrioritySPIFactory → DataSourcePluginManager

@AutoService(DataSourceProcessor.class)
    → META-INF/services/...DataSourceProcessor (编译时生成)
        → ServiceLoader → DataSourceProcessorManager
```

## 4. 需要修改的现有文件

### 4.1 后端 (Java)

| 文件                                                             | 修改                                                         |
| ---------------------------------------------------------------- | ------------------------------------------------------------ |
| `dolphinscheduler-spi/.../enums/DbType.java`                     | `DORIS(12, "doris")`                                         |
| `dolphinscheduler-common/.../constants/DataSourceConstants.java` | 添加 `DORIS_DRIVER`、`JDBC_DORIS`、`DORIS_VALIDATION_QUERY`  |
| `dolphinscheduler-datasource-plugin/pom.xml`                     | `<module>dolphinscheduler-datasource-doris</module>`         |
| `dolphinscheduler-datasource-all/pom.xml`                        | `<dependency>dolphinscheduler-datasource-doris</dependency>` |

### 4.2 前端 (Vue/TypeScript)

| 文件                                                           | 修改                                                             |
| -------------------------------------------------------------- | ---------------------------------------------------------------- |
| `dolphinscheduler-ui/src/service/modules/data-source/types.ts` | `IDataBase` 联合类型添加 `'DORIS'`                               |
| `dolphinscheduler-ui/src/views/datasource/list/use-form.ts`    | `datasourceType` 添加 `DORIS: {value, label, defaultPort: 9030}` |
| `dolphinscheduler-ui/src/locales/en_US/datasource.ts`          | 添加 `doris: 'DORIS'`                                            |
| `dolphinscheduler-ui/src/locales/zh_CN/datasource.ts`          | 添加 `doris: 'DORIS'`                                            |

## 5. 编译和构建

### Maven 命令

```bash
# 仅编译 Doris 插件模块
./mvnw clean install -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris -am -DskipTests

# 编译全部 datasource 插件（含新 Doris 模块）
./mvnw clean install -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all -am -DskipTests
```

### 依赖关系

```
dolphinscheduler-datasource-doris
  ├── dolphinscheduler-spi (provided)
  ├── dolphinscheduler-datasource-api
  └── mysql-connector-java (runtime)
```

## 6. 测试

### 单元测试

参照 MySQL 插件的测试模式：

- 验证 `DorisDataSourceChannelFactory` 的 SPI name 返回 "doris"
- 验证 `DorisDataSourceProcessor` 正确构建 JDBC URL
- 验证参数校验逻辑

### 集成测试

- 创建 Doris 数据源 → 测试连接 → 验证连接成功
- 在 SQL 任务中选择 Doris 数据源执行查询

## 7. 迁移和兼容性

- **向后兼容**: 仅新增文件，不修改已有插件行为
- **数据库迁移**: 存量环境的 `t_ds_datasource` 表中可手动插入 Doris 类型(type=12)记录
- **版本要求**: Apache Doris 1.2+ (需开启 MySQL 协议，默认已开启)
