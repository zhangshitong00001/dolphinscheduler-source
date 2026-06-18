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

package org.apache.dolphinscheduler.dao.datasource;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;

/**
 * Spring 数据源连接工厂配置类，负责配置 MyBatis-Plus 的 SqlSessionFactory、分页插件、
 * 多数据库方言支持（MySQL、PostgreSQL、H2）及事务管理器等数据访问基础设施。
 */
@Configuration
public class SpringConnectionFactory {

    /**
     * 注入数据库初始化器，确保在数据库表结构初始化完成后再创建 MyBatis Session，
     * 解决表不存在的问题（issue #8432）。
     */
    @Autowired(required = false)
    public DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer;

    /**
     * 配置 MyBatis-Plus 分页拦截器，根据数据库类型启用分页功能。
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor(DbType dbType) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    /**
     * 配置数据源事务管理器。
     */
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 配置 MyBatis-Plus 的 SqlSessionFactory，包括下划线转驼峰映射、Mapper XML 位置、
     * 类型别名包和数据库厂商 ID 提供者等设置。
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, GlobalConfig globalConfig,
                                               DbType dbType) throws Exception {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.setCallSettersOnNulls(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.addInterceptor(paginationInterceptor(dbType));

        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setDataSource(dataSource);

        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setTypeAliasesPackage("org.apache.dolphinscheduler.dao.entity");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean
                .setMapperLocations(resolver.getResources("org/apache/dolphinscheduler/dao/mapper/*Mapper.xml"));
        sqlSessionFactoryBean.setDatabaseIdProvider(databaseIdProvider());
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * 配置 MyBatis-Plus 全局配置，设置主键自增策略并关闭 Banner。
     */
    @Bean
    public GlobalConfig globalConfig() {
        return new GlobalConfig().setDbConfig(new GlobalConfig.DbConfig()
                .setIdType(IdType.AUTO)).setBanner(false);
    }

    /**
     * 配置 MyBatis 数据库厂商 ID 提供者，用于根据数据库类型（MySQL、PostgreSQL、H2）加载不同的 SQL 映射语句。
     */
    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("PostgreSQL", "pg");
        properties.setProperty("h2", "h2");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }

    /**
     * MySQL 数据库方言 Bean，当 Spring Profile 为 mysql 时作为主数据库类型。
     */
    @Bean
    @Primary
    @Profile("mysql")
    public DbType mysql() {
        return DbType.MYSQL;
    }

    /**
     * H2 数据库方言 Bean，默认启用。
     */
    @Bean
    public DbType h2() {
        return DbType.H2;
    }

    /**
     * PostgreSQL 数据库方言 Bean，当 Spring Profile 为 postgresql 时作为主数据库类型。
     */
    @Bean
    @Primary
    @Profile("postgresql")
    public DbType postgresql() {
        return DbType.POSTGRE_SQL;
    }
}
