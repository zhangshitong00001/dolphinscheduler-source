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

import java.sql.SQLException;
import java.util.List;

/**
 * Service interface for Hive metadata management.
 * <p>
 * Provides methods to browse Hive Metastore hierarchy:
 * Database -> Table -> Column, with search and filter capabilities.
 * Results are cached in Redis to reduce load on Hive Metastore.
 */
public interface MetadataService {

    /**
     * Retrieves all databases from Hive Metastore.
     *
     * @return list of HiveDatabase objects
     */
    List<HiveDatabase> getDatabases() throws SQLException;

    /**
     * Retrieves all tables in a given database.
     *
     * @param databaseName the database name
     * @return list of HiveTable objects
     */
    List<HiveTable> getTables(String databaseName) throws SQLException;

    /**
     * Retrieves all columns for a given table.
     *
     * @param databaseName the database name
     * @param tableName the table name
     * @return list of HiveColumn objects
     */
    List<HiveColumn> getColumns(String databaseName, String tableName) throws SQLException;

    /**
     * Searches for tables matching the given keyword across all databases.
     *
     * @param keyword search keyword
     * @return list of matching HiveTable objects
     */
    List<HiveTable> searchTables(String keyword) throws SQLException;

    /**
     * Tests the Hive Metastore connection.
     *
     * @return true if connection is successful
     */
    boolean testConnection();
}
