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

/**
 * Represents a Hive column/field with its metadata.
 * <p>
 * This model class is used to represent table column information
 * retrieved from Hive Metastore, including field name, data type,
 * and descriptive comment.
 */
public class HiveColumn {

    /** Column/field name */
    private String name;

    /** Column data type (e.g., STRING, INT, BIGINT, DOUBLE) */
    private String type;

    /** Column description/comment */
    private String comment;

    public HiveColumn() {}

    public HiveColumn(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
