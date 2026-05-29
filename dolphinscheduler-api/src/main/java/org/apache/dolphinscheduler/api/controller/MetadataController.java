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

package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.metadata.HiveColumn;
import org.apache.dolphinscheduler.api.metadata.HiveDatabase;
import org.apache.dolphinscheduler.api.metadata.HiveTable;
import org.apache.dolphinscheduler.api.metadata.MetadataService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller for Hive metadata management.
 * <p>
 * Provides REST APIs to browse Hive Metastore hierarchy:
 * Database -> Table -> Column, with search and filter capabilities.
 * All results are cached in Redis for 5 minutes.
 */
@Api(tags = "METADATA_TAG")
@RestController
@RequestMapping("/metadata")
public class MetadataController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    private MetadataService metadataService;

    /**
     * Get all databases from Hive Metastore.
     *
     * @param loginUser the authenticated user
     * @return list of databases
     */
    @ApiOperation(value = "getDatabases", notes = "GET_DATABASES_NOTES")
    @GetMapping("/databases")
    @ResponseStatus(HttpStatus.OK)
    public Result getDatabases(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        try {
            List<HiveDatabase> databases = metadataService.getDatabases();
            return success(databases);
        } catch (SQLException e) {
            logger.error("Failed to get databases", e);
            return error("Failed to get databases: " + e.getMessage());
        }
    }

    /**
     * Get all tables in a database.
     *
     * @param loginUser the authenticated user
     * @param databaseName the database name
     * @return list of tables
     */
    @ApiOperation(value = "getTables", notes = "GET_TABLES_NOTES")
    @GetMapping("/databases/{databaseName}/tables")
    @ResponseStatus(HttpStatus.OK)
    public Result getTables(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @PathVariable String databaseName) {
        try {
            List<HiveTable> tables = metadataService.getTables(databaseName);
            return success(tables);
        } catch (SQLException e) {
            logger.error("Failed to get tables for database: {}", databaseName, e);
            return error("Failed to get tables: " + e.getMessage());
        }
    }

    /**
     * Get all columns in a table.
     *
     * @param loginUser the authenticated user
     * @param databaseName the database name
     * @param tableName the table name
     * @return list of columns with name, type, and comment
     */
    @ApiOperation(value = "getColumns", notes = "GET_COLUMNS_NOTES")
    @GetMapping("/databases/{databaseName}/tables/{tableName}/columns")
    @ResponseStatus(HttpStatus.OK)
    public Result getColumns(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                            @PathVariable String databaseName,
                            @PathVariable String tableName) {
        try {
            List<HiveColumn> columns = metadataService.getColumns(databaseName, tableName);
            return success(columns);
        } catch (SQLException e) {
            logger.error("Failed to get columns for: {}.{}", databaseName, tableName, e);
            return error("Failed to get columns: " + e.getMessage());
        }
    }

    /**
     * Search tables across all databases.
     *
     * @param loginUser the authenticated user
     * @param keyword the search keyword
     * @return matching tables
     */
    @ApiOperation(value = "searchTables", notes = "SEARCH_TABLES_NOTES")
    @GetMapping("/tables/search")
    @ResponseStatus(HttpStatus.OK)
    public Result searchTables(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam String keyword) {
        try {
            List<HiveTable> tables = metadataService.searchTables(keyword);
            return success(tables);
        } catch (SQLException e) {
            logger.error("Failed to search tables with keyword: {}", keyword, e);
            return error("Failed to search tables: " + e.getMessage());
        }
    }

    /**
     * Test Hive Metastore connection.
     *
     * @param loginUser the authenticated user
     * @return connection test result
     */
    @ApiOperation(value = "testConnection", notes = "TEST_CONNECTION_NOTES")
    @PostMapping("/connection/test")
    @ResponseStatus(HttpStatus.OK)
    public Result testConnection(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        boolean success = metadataService.testConnection();
        if (success) {
            return success("Connection successful");
        } else {
            return error("Connection failed");
        }
    }

    /**
     * Clear metadata cache.
     *
     * @param loginUser the authenticated user
     * @return operation result
     */
    @ApiOperation(value = "clearCache", notes = "CLEAR_CACHE_NOTES")
    @PostMapping("/cache/clear")
    @ResponseStatus(HttpStatus.OK)
    public Result clearCache(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        metadataService.clearCache();
        return success("Metadata cache cleared");
    }
}
