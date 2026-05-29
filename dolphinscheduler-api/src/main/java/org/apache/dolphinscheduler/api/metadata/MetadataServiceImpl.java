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

package org.apache.dolphinscheduler.api.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of MetadataService for Hive Metastore.
 * <p>
 * Uses JDBC to connect to Hive Metastore (via HiveServer2 or direct Metastore DB).
 * Results are cached in Redis with a 5-minute TTL to reduce Metastore load.
 */
@Service
@CacheConfig(cacheNames = "metadata", keyGenerator = "cacheKeyGenerator")
public class MetadataServiceImpl implements MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceImpl.class);

    @Value("${metadata.hive.jdbc.url:jdbc:hive2://localhost:10000/default}")
    private String hiveJdbcUrl;

    @Value("${metadata.hive.jdbc.user:}")
    private String hiveUser;

    @Value("${metadata.hive.jdbc.password:}")
    private String hivePassword;

    @Value("${metadata.hive.jdbc.driver:org.apache.hive.jdbc.HiveDriver}")
    private String hiveDriver;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String METADATA_CACHE_PREFIX = "dolphinscheduler:metadata:";
    private static final long METADATA_CACHE_TTL_SECONDS = 300L;

    @Override
    @Cacheable(key = "databases", unless = "#result == null or #result.isEmpty()")
    public List<HiveDatabase> getDatabases() throws SQLException {
        String cacheKey = METADATA_CACHE_PREFIX + "databases";
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            logger.debug("Returning cached databases list");
            return parseDatabaseList(cached);
        }
        List<HiveDatabase> databases = new ArrayList<>();
        try (Connection conn = getConnection();
             ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                String dbName = rs.getString("TABLE_CAT");
                if (dbName != null && !dbName.isEmpty()) {
                    databases.add(new HiveDatabase(dbName, ""));
                }
            }
        }
        if (databases.isEmpty()) {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                while (rs.next()) {
                    databases.add(new HiveDatabase(rs.getString(1), ""));
                }
            }
        }
        stringRedisTemplate.opsForValue().set(cacheKey, serializeDatabaseList(databases),
                METADATA_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        logger.info("Retrieved {} databases from Hive Metastore", databases.size());
        return databases;
    }

    @Override
    @Cacheable(key = "tables_ + #databaseName", unless = "#result == null or #result.isEmpty()")
    public List<HiveTable> getTables(String databaseName) throws SQLException {
        String cacheKey = METADATA_CACHE_PREFIX + "tables:" + databaseName;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            logger.debug("Returning cached tables for database: {}", databaseName);
            return parseTableList(cached);
        }
        List<HiveTable> tables = new ArrayList<>();
        String sql = "SHOW TABLES IN " + databaseName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String tableName = rs.getString(1);
                tables.add(new HiveTable(tableName, databaseName, ""));
            }
        }
        for (HiveTable table : tables) {
            try (Connection conn = getConnection();
                 ResultSet rs = conn.getMetaData().getTables(databaseName, null, table.getName(), null)) {
                while (rs.next()) {
                    table.setComment(rs.getString("REMARKS"));
                }
            }
        }
        stringRedisTemplate.opsForValue().set(cacheKey, serializeTableList(tables),
                METADATA_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        logger.info("Retrieved {} tables from database: {}", tables.size(), databaseName);
        return tables;
    }

    @Override
    @Cacheable(key = "columns_ + #databaseName + _ + #tableName", unless = "#result == null or #result.isEmpty()")
    public List<HiveColumn> getColumns(String databaseName, String tableName) throws SQLException {
        String cacheKey = METADATA_CACHE_PREFIX + "columns:" + databaseName + ":" + tableName;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            logger.debug("Returning cached columns for: {}.{}", databaseName, tableName);
            return parseColumnList(cached);
        }
        List<HiveColumn> columns = new ArrayList<>();
        try (Connection conn = getConnection();
             ResultSet rs = conn.getMetaData().getColumns(databaseName, null, tableName, null)) {
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                String colType = rs.getString("TYPE_NAME");
                String colComment = rs.getString("REMARKS");
                columns.add(new HiveColumn(colName, colType, colComment));
            }
        }
        stringRedisTemplate.opsForValue().set(cacheKey, serializeColumnList(columns),
                METADATA_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        logger.info("Retrieved {} columns from: {}.{}", columns.size(), databaseName, tableName);
        return columns;
    }

    @Override
    public List<HiveTable> searchTables(String keyword) throws SQLException {
        List<HiveTable> results = new ArrayList<>();
        List<HiveDatabase> databases = getDatabases();
        for (HiveDatabase db : databases) {
            List<HiveTable> tables = getTables(db.getName());
            for (HiveTable table : tables) {
                if (table.getName().toLowerCase().contains(keyword.toLowerCase())
                        || (table.getComment() != null
                        && table.getComment().toLowerCase().contains(keyword.toLowerCase()))) {
                    results.add(table);
                }
            }
        }
        logger.info("Search for {} returned {} results", keyword, results.size());
        return results;
    }

    @Override
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn.isValid(5);
            logger.info("Hive Metastore connection test: {}", valid ? "SUCCESS" : "FAILED");
            return valid;
        } catch (Exception e) {
            logger.error("Hive Metastore connection test failed", e);
            return false;
        }
    }

    @CacheEvict(allEntries = true)
    public void clearCache() {
        logger.info("Metadata cache cleared");
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName(hiveDriver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Hive JDBC driver not found: " + hiveDriver, e);
        }
        return DriverManager.getConnection(hiveJdbcUrl, hiveUser, hivePassword);
    }

    private String serializeDatabaseList(List<HiveDatabase> databases) {
        StringBuilder sb = new StringBuilder();
        for (HiveDatabase db : databases) {
            sb.append(db.getName()).append("|").append(db.getComment() != null ? db.getComment() : "").append("\n");
        }
        return sb.toString();
    }

    private List<HiveDatabase> parseDatabaseList(String data) {
        List<HiveDatabase> result = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", 2);
            result.add(new HiveDatabase(parts[0], parts.length > 1 ? parts[1] : ""));
        }
        return result;
    }

    private String serializeTableList(List<HiveTable> tables) {
        StringBuilder sb = new StringBuilder();
        for (HiveTable t : tables) {
            sb.append(t.getName()).append("|").append(t.getDatabase()).append("|")
              .append(t.getComment() != null ? t.getComment() : "").append("\n");
        }
        return sb.toString();
    }

    private List<HiveTable> parseTableList(String data) {
        List<HiveTable> result = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", 3);
            result.add(new HiveTable(parts[0], parts[1], parts.length > 2 ? parts[2] : ""));
        }
        return result;
    }

    private String serializeColumnList(List<HiveColumn> columns) {
        StringBuilder sb = new StringBuilder();
        for (HiveColumn c : columns) {
            sb.append(c.getName()).append("|").append(c.getType()).append("|")
              .append(c.getComment() != null ? c.getComment() : "").append("\n");
        }
        return sb.toString();
    }

    private List<HiveColumn> parseColumnList(String data) {
        List<HiveColumn> result = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", 3);
            result.add(new HiveColumn(parts[0], parts[1], parts.length > 2 ? parts[2] : ""));
        }
        return result;
    }
}
