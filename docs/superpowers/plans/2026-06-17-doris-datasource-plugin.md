# Apache Doris 数据源插件实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**目标:** 为 DolphinScheduler 添加 Apache Doris 数据源插件，支持创建、测试、删除 Doris 连接

**架构:** 遵循现有 SPI 数据源插件模式（参照 MySQL 插件）。Doris 兼容 MySQL 协议（FE 端口 9030），复用 `mysql-connector-java:8.0.16` 驱动

**技术栈:** Java 8+, Maven, MySQL JDBC Driver 8.0.16, TypeScript (Vue 3 前端), Google AutoService (SPI)

## 全局约束

- 遵循 DolphinScheduler ASF 许可证头部模板（Apache License 2.0）
- Java 包名前缀: `org.apache.dolphinscheduler.plugin.datasource.doris`
- 前端类型必须与后端 `DbType` 枚举名称匹配
- 不修改任何现有数据源插件的行为
- 目标分支: `master`, JDBC URL 前缀: `jdbc:mysql://`, 默认端口: `9030`, 验证 SQL: `select 1`

---

### 任务 1: 注册 Doris 到 DbType 枚举和常量

**修改文件:**

- `dolphinscheduler-spi/src/main/java/org/apache/dolphinscheduler/spi/enums/DbType.java`
- `dolphinscheduler-common/src/main/java/org/apache/dolphinscheduler/common/constants/DataSourceConstants.java`

**产出:** `DbType.DORIS` 枚举值和 Doris JDBC 常量可用

- [ ] **Step 1: 在 DbType.java 添加 DORIS 枚举值**

在 `ATHENA(11,"athena"),` 之后，分号之前插入:

```java
    /** Apache Doris数据库 */
    DORIS(12, "doris"),
```

文件位置: `DbType.java` 第 59 行附近，`ATHENA` 枚举值和 `;` 之间。

- [ ] **Step 2: 在 DataSourceConstants.java 添加 Doris 常量**

在 `COM_ATHENA_JDBC_DRIVER` 声明（第 53 行）之后添加:

```java
    /** Apache Doris JDBC驱动 (兼容MySQL协议) */
    public static final String COM_DORIS_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
```

在 `ATHENA_VALIDATION_QUERY` 声明（第 76 行）之后添加:

```java
    /** Apache Doris连接验证SQL */
    public static final String DORIS_VALIDATION_QUERY = "select 1";
```

在 `JDBC_ATHENA` 声明（第 101 行）之后添加:

```java
    /** Apache Doris JDBC URL前缀 (使用MySQL协议) */
    public static final String JDBC_DORIS = "jdbc:mysql://";
```

- [ ] **Step 3: 验证修改编译通过**

```bash
./mvnw clean compile -pl dolphinscheduler-spi,dolphinscheduler-common -am -DskipTests -q
```

预期: BUILD SUCCESS

---

### 任务 2: 创建 Doris 数据源插件模块（7 个生产文件）

**创建文件:**

- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/pom.xml`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceChannelFactory.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceChannel.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceClient.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisConnectionParam.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisDataSourceParamDTO.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisDataSourceProcessor.java`

**产出:** 完整的 Doris 数据源插件 module，实现 SPI 接口，可通过 ServiceLoader 加载

- [ ] **Step 1: 创建 pom.xml**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.apache.dolphinscheduler</groupId>
        <artifactId>dolphinscheduler-datasource-plugin</artifactId>
        <version>3.1.10-SNAPSHOT</version>
    </parent>

    <artifactId>dolphinscheduler-datasource-doris</artifactId>
    <packaging>jar</packaging>
    <name>${project.artifactId}</name>

    <dependencies>

        <dependency>
            <groupId>org.apache.dolphinscheduler</groupId>
            <artifactId>dolphinscheduler-spi</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.dolphinscheduler</groupId>
            <artifactId>dolphinscheduler-datasource-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <type>jar</type>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 创建 DorisDataSourceChannelFactory.java**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceChannelFactory.java`:

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris;

import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;

import com.google.auto.service.AutoService;

/**
 * Apache Doris数据源通道工厂。
 * 通过Google AutoService自动注册为SPI服务，ServiceLoader加载时由getName()返回"doris"标识。
 */
@AutoService(DataSourceChannelFactory.class)
public class DorisDataSourceChannelFactory implements DataSourceChannelFactory {
    @Override
    public String getName() {
        return "doris";
    }

    @Override
    public DataSourceChannel create() {
        return new DorisDataSourceChannel();
    }
}
```

- [ ] **Step 3: 创建 DorisDataSourceChannel.java**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceChannel.java`:

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * Apache Doris数据源通道。
 * 负责创建Doris数据源客户端，封装MySQL协议连接逻辑。
 */
public class DorisDataSourceChannel implements DataSourceChannel {

    @Override
    public DataSourceClient createDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        return new DorisDataSourceClient(baseConnectionParam, dbType);
    }
}
```

- [ ] **Step 4: 创建 DorisDataSourceClient.java**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceClient.java`:

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * Apache Doris数据源客户端。
 * 继承CommonDataSourceClient，使用HikariCP连接池管理Doris连接。
 * Doris兼容MySQL协议，通过FE端口9030连接并使用MySQL JDBC驱动。
 */
public class DorisDataSourceClient extends CommonDataSourceClient {

    public DorisDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

}
```

- [ ] **Step 5: 创建 DorisConnectionParam.java**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisConnectionParam.java`:

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris.param;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;

/**
 * Apache Doris连接参数类。
 * 继承BaseConnectionParam，包含Doris FE连接所需的全部JDBC参数。
 */
public class DorisConnectionParam extends BaseConnectionParam {
    @Override
    public String toString() {
        return "DorisConnectionParam{"
                + "user='" + user + '\''
                + ", password='" + password + '\''
                + ", address='" + address + '\''
                + ", database='" + database + '\''
                + ", jdbcUrl='" + jdbcUrl + '\''
                + ", driverLocation='" + driverLocation + '\''
                + ", driverClassName='" + driverClassName + '\''
                + ", validationQuery='" + validationQuery + '\''
                + ", other='" + other + '\''
                + '}';
    }
}
```

- [ ] **Step 6: 创建 DorisDataSourceParamDTO.java**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisDataSourceParamDTO.java`:

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris.param;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * Apache Doris数据源参数DTO。
 * 用于API层传输Doris数据源配置参数。
 */
public class DorisDataSourceParamDTO extends BaseDataSourceParamDTO {

    @Override
    public String toString() {
        return "DorisDataSourceParamDTO{"
                + "name='" + name + '\''
                + ", note='" + note + '\''
                + ", host='" + host + '\''
                + ", port=" + port
                + ", database='" + database + '\''
                + ", userName='" + userName + '\''
                + ", password='" + password + '\''
                + ", other='" + other + '\''
                + '}';
    }

    @Override
    public DbType getType() {
        return DbType.DORIS;
    }
}
```

- [ ] **Step 7: 创建 DorisDataSourceProcessor.java**

创建 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisDataSourceProcessor.java`:

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Apache Doris数据源处理器。
 * 通过AutoService注解自动注册为SPI服务。
 * 处理Doris数据源的参数校验、JDBC URL构建和连接管理。
 * Doris兼容MySQL协议，使用MySQL JDBC驱动通过FE端口9030连接。
 */
@AutoService(DataSourceProcessor.class)
public class DorisDataSourceProcessor extends AbstractDataSourceProcessor {

    private final Logger logger = LoggerFactory.getLogger(DorisDataSourceProcessor.class);

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    private static final String APPEND_PARAMS = "allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false";

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DorisDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DorisConnectionParam
                connectionParams = (DorisConnectionParam) createConnectionParams(connectionJson);
        DorisDataSourceParamDTO
                dorisDatasourceParamDTO = new DorisDataSourceParamDTO();

        dorisDatasourceParamDTO.setUserName(connectionParams.getUser());
        dorisDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        dorisDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        dorisDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        dorisDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return dorisDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        DorisDataSourceParamDTO dorisDatasourceParam = (DorisDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_DORIS, dorisDatasourceParam.getHost(),
                dorisDatasourceParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, dorisDatasourceParam.getDatabase());

        DorisConnectionParam
                dorisConnectionParam = new DorisConnectionParam();
        dorisConnectionParam.setJdbcUrl(jdbcUrl);
        dorisConnectionParam.setDatabase(dorisDatasourceParam.getDatabase());
        dorisConnectionParam.setAddress(address);
        dorisConnectionParam.setUser(dorisDatasourceParam.getUserName());
        dorisConnectionParam.setPassword(PasswordUtils.encodePassword(dorisDatasourceParam.getPassword()));
        dorisConnectionParam.setDriverClassName(getDatasourceDriver());
        dorisConnectionParam.setValidationQuery(getValidationQuery());
        dorisConnectionParam.setOther(transformOther(dorisDatasourceParam.getOther()));
        dorisConnectionParam.setProps(dorisDatasourceParam.getOther());

        return dorisConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DorisConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DORIS_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DORIS_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DorisConnectionParam
                dorisConnectionParam = (DorisConnectionParam) connectionParam;
        String jdbcUrl = dorisConnectionParam.getJdbcUrl();
        if (!StringUtils.isEmpty(dorisConnectionParam.getOther())) {
            return String.format("%s?%s&%s", jdbcUrl, dorisConnectionParam.getOther(), APPEND_PARAMS);
        }
        return String.format("%s?%s", jdbcUrl, APPEND_PARAMS);
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException {
        DorisConnectionParam dorisConnectionParam = (DorisConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        String user = dorisConnectionParam.getUser();
        if (user.contains(AUTO_DESERIALIZE)) {
            logger.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
            user = user.replace(AUTO_DESERIALIZE, "");
        }
        String password = PasswordUtils.decodePassword(dorisConnectionParam.getPassword());
        if (password.contains(AUTO_DESERIALIZE)) {
            logger.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
            password = password.replace(AUTO_DESERIALIZE, "");
        }
        return DriverManager.getConnection(getJdbcUrl(connectionParam), user, password);
    }

    @Override
    public DbType getDbType() {
        return DbType.DORIS;
    }

    @Override
    public DataSourceProcessor create() {
        return new DorisDataSourceProcessor();
    }

    private String transformOther(Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            return null;
        }
        Map<String, String> otherMap = new HashMap<>();
        paramMap.forEach((k, v) -> {
            if (!checkKeyIsLegitimate(k)) {
                return;
            }
            otherMap.put(k, v);
        });
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s&", key, value)));
        return stringBuilder.toString();
    }

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME)
                && !key.contains(AUTO_DESERIALIZE)
                && !key.contains(ALLOW_LOCAL_IN_FILE_NAME)
                && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split("&")) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }

}
```

- [ ] **Step 8: 编译验证所有新文件**

```bash
mkdir -p dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/main/java/org/apache/dolphinscheduler/plugin/datasource/doris/param
mkdir -p dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/test/java/org/apache/dolphinscheduler/plugin/datasource/doris/param
./mvnw clean compile -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris -am -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 9: Commit 任务 1+2**

```bash
git add dolphinscheduler-spi/src/main/java/org/apache/dolphinscheduler/spi/enums/DbType.java
git add dolphinscheduler-common/src/main/java/org/apache/dolphinscheduler/common/constants/DataSourceConstants.java
git add dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/
git commit -m "feat: add Apache Doris datasource plugin module

- Add DORIS(12, \"doris\") to DbType enum
- Add DORIS JDBC constants (reuse mysql-connector-java for MySQL-compatible protocol)
- Create 7 production classes: ChannelFactory, Channel, Client, ConnectionParam, ParamDTO, Processor
- Default FE port 9030, validation query 'select 1', driver com.mysql.cj.jdbc.Driver"
```

---

### 任务 3: 注册模块到父 POM 和聚合器

**修改文件:**

- `dolphinscheduler-datasource-plugin/pom.xml`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all/pom.xml`

**产出:** Doris 模块被 Maven 构建系统和新 JAR 包安装过程正确识别和包含

- [ ] **Step 1: 在父 POM 中添加模块声明**

在 `dolphinscheduler-datasource-plugin/pom.xml` 的 `<modules>` 节中，在 `<module>dolphinscheduler-datasource-athena</module>` 之后添加:

```xml
        <module>dolphinscheduler-datasource-doris</module>
```

即在该文件中第 42 行之后插入新模块声明。

- [ ] **Step 2: 在 aggregator POM 中添加依赖**

在 `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all/pom.xml` 的 `<dependencies>` 节中，在 `dolphinscheduler-datasource-presto` 依赖之后添加:

```xml
        <dependency>
            <groupId>org.apache.dolphinscheduler</groupId>
            <artifactId>dolphinscheduler-datasource-doris</artifactId>
            <version>${project.version}</version>
        </dependency>
```

即在该文件中第 83 行之后插入新 dependency 块。

- [ ] **Step 3: 验证 aggregator 构建**

```bash
./mvnw clean install -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all -am -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 4: Commit 任务 3**

```bash
git add dolphinscheduler-datasource-plugin/pom.xml dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all/pom.xml
git commit -m "feat: register Doris datasource module in parent and aggregator POMs"
```

---

### 任务 4: 前端 UI 修改

**修改文件:**

- `dolphinscheduler-ui/src/service/modules/data-source/types.ts`
- `dolphinscheduler-ui/src/views/datasource/list/use-form.ts`
- `dolphinscheduler-ui/src/locales/en_US/datasource.ts`
- `dolphinscheduler-ui/src/locales/zh_CN/datasource.ts`

**产出:** 前端数据源类型下拉列表中出现 DORIS 选项，默认端口 9030

- [ ] **Step 1: 添加 'DORIS' 到 IDataBase 联合类型**

在 `dolphinscheduler-ui/src/service/modules/data-source/types.ts` 的 `IDataBase` 类型声明中，在 `| 'ATHENA'` 之后添加:

```typescript
  | 'DORIS'
```

即在第 29 行之后插入该类型字符串。

- [ ] **Step 2: 将 DORIS 添加到 datasourceType 配置对象**

在 `dolphinscheduler-ui/src/views/datasource/list/use-form.ts` 的 `datasourceType` 常量对象中，在 `ATHENA` 配置块之后添加:

```typescript
  DORIS: {
    value: 'DORIS',
    label: 'DORIS',
    defaultPort: 9030
  },
```

即在第 243 行之后插入该配置块。

- [ ] **Step 3: 添加英文 i18n 标签**

在 `dolphinscheduler-ui/src/locales/en_US/datasource.ts` 的 export default 对象中，在 `jdbc_format_tips` 行之前添加:

```typescript
  doris_connect_params: 'Doris Connect Params',
```

即在该文件第 67 行，`jdbc_format_tips` 之前插入。

- [ ] **Step 4: 添加中文 i18n 标签**

在 `dolphinscheduler-ui/src/locales/zh_CN/datasource.ts` 的 export default 对象中，在 `jdbc_format_tips` 行之前添加:

```typescript
  doris_connect_params: 'Doris连接参数',
```

即在该文件第 64 行，`jdbc_format_tips` 之前插入。

- [ ] **Step 5: 前端手动验证**

在 `dolphinscheduler-ui` 目录中运行:

```bash
npx eslint --quiet dolphinscheduler-ui/src/service/modules/data-source/types.ts dolphinscheduler-ui/src/views/datasource/list/use-form.ts
```

预期: 无错误输出

- [ ] **Step 6: Commit 任务 4**

```bash
git add dolphinscheduler-ui/src/service/modules/data-source/types.ts \
        dolphinscheduler-ui/src/views/datasource/list/use-form.ts \
        dolphinscheduler-ui/src/locales/en_US/datasource.ts \
        dolphinscheduler-ui/src/locales/zh_CN/datasource.ts
git commit -m "feat: add DORIS datasource type to frontend UI

- Add 'DORIS' to IDataBase type union
- Add DORIS option with defaultPort 9030 to datasourceType select
- Add DORIS i18n labels (en_US/zh_CN)"
```

---

### 任务 5: 创建单元测试

**创建文件:**

- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/test/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceChannelFactoryTest.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/test/java/org/apache/dolphinscheduler/plugin/datasource/doris/DorisDataSourceChannelTest.java`
- `dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/test/java/org/apache/dolphinscheduler/plugin/datasource/doris/param/DorisDataSourceProcessorTest.java`

**产出:** 通过测试验证 SPI 注册名、Processor JDBC URL 构建和参数校验的正确性

- [ ] **Step 1: 创建 DorisDataSourceChannelFactoryTest.java**

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris;

import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;

import org.junit.Assert;
import org.junit.Test;

/**
 * Doris数据源通道工厂测试。
 * 验证SPI注册名称是否正确。
 */
public class DorisDataSourceChannelFactoryTest {

    @Test
    public void testGetName() {
        DorisDataSourceChannelFactory factory = new DorisDataSourceChannelFactory();
        Assert.assertEquals("doris", factory.getName());
    }

    @Test
    public void testCreate() {
        DorisDataSourceChannelFactory factory = new DorisDataSourceChannelFactory();
        DataSourceChannel channel = factory.create();
        Assert.assertNotNull(channel);
        Assert.assertTrue(channel instanceof DorisDataSourceChannel);
    }
}
```

- [ ] **Step 2: 创建 DorisDataSourceChannelTest.java**

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris;

import org.apache.dolphinscheduler.plugin.datasource.doris.param.DorisConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.Assert;
import org.junit.Test;

/**
 * Doris数据源通道测试。
 * 验证通道能够正确创建客户端实例。
 */
public class DorisDataSourceChannelTest {

    @Test
    public void testCreateDataSourceClient() {
        DorisDataSourceChannel channel = new DorisDataSourceChannel();
        DorisConnectionParam connectionParam = new DorisConnectionParam();
        connectionParam.setUser("root");
        connectionParam.setPassword("password");
        DataSourceClient client = channel.createDataSourceClient(connectionParam, DbType.DORIS);
        Assert.assertNotNull(client);
    }
}
```

- [ ] **Step 3: 创建 DorisDataSourceProcessorTest.java**

```java
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

package org.apache.dolphinscheduler.plugin.datasource.doris.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Doris数据源处理器测试。
 * 验证JDBC驱动名、验证SQL、DbType和JDBC URL的正确性。
 */
@RunWith(MockitoJUnitRunner.class)
public class DorisDataSourceProcessorTest {

    private final DorisDataSourceProcessor processor = new DorisDataSourceProcessor();

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(DataSourceConstants.COM_DORIS_JDBC_DRIVER, processor.getDatasourceDriver());
    }

    @Test
    public void testGetValidationQuery() {
        Assert.assertEquals(DataSourceConstants.DORIS_VALIDATION_QUERY, processor.getValidationQuery());
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.DORIS, processor.getDbType());
    }

    @Test
    public void testCreateConnectionParams() {
        DorisDataSourceParamDTO dto = new DorisDataSourceParamDTO();
        dto.setHost("127.0.0.1");
        dto.setPort(9030);
        dto.setDatabase("test_db");
        dto.setUserName("root");
        dto.setPassword("password");

        try (MockedStatic<PasswordUtils> passwordUtils = org.mockito.Mockito.mockStatic(PasswordUtils.class)) {
            passwordUtils.when(() -> PasswordUtils.encodePassword("password")).thenReturn("password");

            DorisConnectionParam result = (DorisConnectionParam) processor.createConnectionParams(dto);

            Assert.assertNotNull(result);
            Assert.assertEquals("jdbc:mysql://127.0.0.1:9030", result.getAddress());
            Assert.assertEquals("jdbc:mysql://127.0.0.1:9030/test_db", result.getJdbcUrl());
            Assert.assertEquals("test_db", result.getDatabase());
            Assert.assertEquals("root", result.getUser());
            Assert.assertEquals(DataSourceConstants.COM_DORIS_JDBC_DRIVER, result.getDriverClassName());
            Assert.assertEquals(DataSourceConstants.DORIS_VALIDATION_QUERY, result.getValidationQuery());
        }
    }

    @Test
    public void testGetJdbcUrlWithOtherParams() {
        DorisConnectionParam connectionParam = new DorisConnectionParam();
        connectionParam.setJdbcUrl("jdbc:mysql://127.0.0.1:9030/test_db");
        connectionParam.setOther("useSSL=false&serverTimezone=UTC");

        String jdbcUrl = processor.getJdbcUrl(connectionParam);

        Assert.assertTrue(jdbcUrl.startsWith("jdbc:mysql://127.0.0.1:9030/test_db?"));
        Assert.assertTrue(jdbcUrl.contains("useSSL=false"));
        Assert.assertTrue(jdbcUrl.contains("serverTimezone=UTC"));
        Assert.assertTrue(jdbcUrl.contains("allowLoadLocalInfile=false"));
        Assert.assertTrue(jdbcUrl.contains("autoDeserialize=false"));
    }

    @Test
    public void testGetJdbcUrlWithoutOtherParams() {
        DorisConnectionParam connectionParam = new DorisConnectionParam();
        connectionParam.setJdbcUrl("jdbc:mysql://127.0.0.1:9030/test_db");

        String jdbcUrl = processor.getJdbcUrl(connectionParam);

        Assert.assertEquals(
                "jdbc:mysql://127.0.0.1:9030/test_db?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false",
                jdbcUrl);
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
./mvnw test -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris -am
```

预期: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS

- [ ] **Step 5: Commit 任务 5**

```bash
git add dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/src/test/
git commit -m "test: add unit tests for Doris datasource plugin"
```

---

### 任务 6: 最终构建和验证

**产出:** 包含 Doris 插件的完整 DolphinScheduler 构建成功

- [ ] **Step 1: 完整编译全部数据源模块**

```bash
./mvnw clean install -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all -am -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 2: 运行全部数据源测试**

```bash
./mvnw test -pl dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-all -am
```

预期: 所有已有测试通过，Doris 新测试通过

- [ ] **Step 3: 验证 AutoService SPI 注册文件生成**

```bash
jar -tf dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/target/dolphinscheduler-datasource-doris-*.jar | grep META-INF/services
```

预期: 输出包含 `DataSourceChannelFactory` 和 `DataSourceProcessor` 两个 SPI 服务文件

- [ ] **Step 4: 验证 SPI 服务文件内容**

```bash
cd dolphinscheduler-datasource-plugin/dolphinscheduler-datasource-doris/target/classes/META-INF/services/
echo "=== DataSourceChannelFactory ===" && cat org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory
echo "=== DataSourceProcessor ===" && cat org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor
```

预期:

- DataSourceChannelFactory: `org.apache.dolphinscheduler.plugin.datasource.doris.DorisDataSourceChannelFactory`
- DataSourceProcessor: `org.apache.dolphinscheduler.plugin.datasource.doris.param.DorisDataSourceProcessor`

- [ ] **Step 5: Commit 最终变更**

```bash
git add -A
git commit -m "feat: add Apache Doris datasource plugin

Complete implementation of Doris datasource for DolphinScheduler:
- DbType.DORIS(12) enum entry
- JDBC via mysql-connector-java (Doris is MySQL-protocol compatible, FE port 9030)
- Full SPI registration: ChannelFactory, Channel, Client, Processor, ParamDTO
- Frontend: DORIS type in datasource dropdown, default port 9030
- Unit tests: 6 tests covering SPI name, channel creation, JDBC URL building, param injection"
```

---

## 验证清单

完成所有任务后验证：

1. `DbType.DORIS.getCode() == 12` 和 `DbType.DORIS.getDescp().equals("doris")`
2. Doris 模块编译为 JAR 包并包含 SPI 服务文件
3. 前端数据源类型下拉列表显示 DORIS 选项（端口 9030）
4. 创建 Doris 数据源时默认端口为 9030
5. JDBC URL 格式: `jdbc:mysql://host:9030/database?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false`
6. 所有已有测试继续通过
