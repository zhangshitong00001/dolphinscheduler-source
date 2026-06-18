# 任务 1: 注册 Doris 到 DbType 枚举和常量

## 状态: DONE

## 摘要

成功将 Apache Doris 数据源类型注册到 DolphinScheduler 的类型系统和 JDBC 常量中，为后续插件模块开发奠定基础。

## 变更内容

### 1. DbType.java — 新增 DORIS 枚举值

- **文件**: `dolphinscheduler-spi/src/main/java/org/apache/dolphinscheduler/spi/enums/DbType.java`
- **变更**: 在 `ATHENA(11,"athena")` 之后、枚举终结符 `;` 之前插入 `DORIS(12, "doris")`
- **编码**: 12（接续 Athena 的 11，保持连续递增）

### 2. DataSourceConstants.java — 新增 Doris JDBC 常量

- **文件**: `dolphinscheduler-common/src/main/java/org/apache/dolphinscheduler/common/constants/DataSourceConstants.java`
- **新增常量**:
  - `COM_DORIS_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"` — Doris 兼容 MySQL 协议，使用 MySQL JDBC 驱动
  - `DORIS_VALIDATION_QUERY = "select 1"` — 连接验证查询
  - `JDBC_DORIS = "jdbc:mysql://"` — JDBC URL 前缀（使用 MySQL 协议）

## 提交

```
fc5c8a4 feat: add DORIS(12) to DbType enum and Doris JDBC constants

- Add DbType.DORIS(12, "doris") enum value
- Add COM_DORIS_JDBC_DRIVER = com.mysql.cj.jdbc.Driver
- Add DORIS_VALIDATION_QUERY = select 1
- Add JDBC_DORIS = jdbc:mysql://
```

分支: `feat/doris-datasource-plugin`
文件变更: 2 files, 8 insertions(+)

## 编译验证

```bash
./mvnw clean compile -pl dolphinscheduler-spi,dolphinscheduler-common -am -DskipTests -q
```

结果: **BUILD SUCCESS** — 无编译错误或警告。

## 关注点

无。变更严格遵循已有模式（Athena 作为参考模板），未修改任何现有行为。
