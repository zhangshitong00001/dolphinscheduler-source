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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MetadataService for Hive Metastore metadata management.
 * <p>
 * Uses JDBC to connect to Hive Metastore (via HiveServer2 or direct Metastore DB).
 * Results are cached in Redis via Spring {@link Cacheable} annotation with a 5-minute TTL,
 * reducing load on Hive Metastore and improving response times for repeated queries.
 * <p>
 * Cache configuration is defined in {@link org.apache.dolphinscheduler.service.redis.RedisConfig}
 * under the "metadata" cache namespace with a 300-second TTL.
 */
@Service
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

    @Override
    @Cacheable(value = "metadata", key = "'dolphinscheduler:metadata:databases'",
            unless = "#result == null or #result.isEmpty()")
    public List<HiveDatabase> getDatabases() throws SQLException {
        logger.debug("Fetching databases from Hive Metastore (cache miss)");
        List<HiveDatabase> databases = new ArrayList<>();

        // Attempt to retrieve via JDBC metadata API (catalogs)
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getCatalogs()) {
                while (rs.next()) {
                    String dbName = rs.getString("TABLE_CAT");
                    if (dbName != null && !dbName.isEmpty()) {
                        databases.add(new HiveDatabase(dbName, ""));
                    }
                }
            }
        }

        // Fallback: if catalogs returned empty, use SHOW DATABASES (Hive-specific)
        if (databases.isEmpty()) {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                while (rs.next()) {
                    databases.add(new HiveDatabase(rs.getString(1), ""));
                }
            }
        }

        logger.info("Retrieved {} databases from Hive Metastore", databases.size());
        return databases;
    }

    @Override
    @Cacheable(value = "metadata", key = "'dolphinscheduler:metadata:tables:' + #databaseName",
            unless = "#result == null or #result.isEmpty()")
    public List<HiveTable> getTables(String databaseName) throws SQLException {
        logger.debug("Fetching tables for database '{}' from Hive Metastore (cache miss)", databaseName);
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

        // Enrich each table with its comment from JDBC metadata, reusing a single connection
        if (!tables.isEmpty()) {
            try (Connection conn = getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                for (HiveTable table : tables) {
                    try (ResultSet rs = metaData.getTables(databaseName, null, table.getName(), null)) {
                        while (rs.next()) {
                            table.setComment(rs.getString("REMARKS"));
                        }
                    }
                }
            }
        }

        logger.info("Retrieved {} tables from database: {}", tables.size(), databaseName);
        return tables;
    }

    @Override
    @Cacheable(value = "metadata", key = "'dolphinscheduler:metadata:columns:' + #databaseName + ':' + #tableName",
            unless = "#result == null or #result.isEmpty()")
    public List<HiveColumn> getColumns(String databaseName, String tableName) throws SQLException {
        logger.debug("Fetching columns for {}.{} from Hive Metastore (cache miss)", databaseName, tableName);
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

        logger.info("Retrieved {} columns from: {}.{}", columns.size(), databaseName, tableName);
        return columns;
    }

    @Override
    public List<HiveTable> searchTables(String keyword) throws SQLException {
        logger.debug("Searching tables with keyword: {}", keyword);
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
        logger.info("Search for '{}' returned {} results", keyword, results.size());
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

    /**
     * Clears all metadata cache entries from Redis.
     * Typically called after schema changes in Hive to force a refresh.
     */
    @CacheEvict(value = "metadata", allEntries = true)
    public void clearCache() {
        logger.info("Metadata cache cleared successfully");
    }

    /**
     * Establishes a JDBC connection to Hive Metastore (or HiveServer2).
     *
     * @return a JDBC connection
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName(hiveDriver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Hive JDBC driver not found: " + hiveDriver, e);
        }
        return DriverManager.getConnection(hiveJdbcUrl, hiveUser, hivePassword);
    }
}
