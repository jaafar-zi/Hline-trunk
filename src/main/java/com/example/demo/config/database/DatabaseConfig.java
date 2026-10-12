package com.example.demo.config.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    // ====================================
    // Master DataSource
    // ====================================

    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "masterDataSource")
    @ConfigurationProperties("spring.datasource.master.hikari")
    public DataSource masterDataSource() {
        log.info("Configuring Master DataSource");

        DataSourceProperties properties = masterDataSourceProperties();
        log.info("Master URL: {}", properties.determineUrl());
        log.info("Master Username: {}", properties.determineUsername());

        HikariDataSource dataSource = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        dataSource.setPoolName("HikariPool-Master-Dev");
        log.info("Master DataSource configured — pool: {}", dataSource.getPoolName());
        return dataSource;
    }

    // ====================================
    // Slave DataSource
    // ====================================

    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSourceProperties slaveDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "slaveDataSource")
    @ConfigurationProperties("spring.datasource.slave.hikari")
    public DataSource slaveDataSource() {
        log.info("Configuring Slave DataSource");

        DataSourceProperties properties = slaveDataSourceProperties();
        log.info("Slave URL: {}", properties.determineUrl());
        log.info("Slave Username: {}", properties.determineUsername());

        HikariDataSource dataSource = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        dataSource.setPoolName("HikariPool-Slave-Dev");
        log.info("Slave DataSource configured — pool: {}", dataSource.getPoolName());
        return dataSource;
    }

    // ====================================
    // Routing DataSource
    // ====================================

    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {

        log.info("Configuring Routing DataSource");

        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DatabaseType.MASTER, masterDataSource);
        dataSourceMap.put(DatabaseType.SLAVE, slaveDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);

        log.info("Routing DataSource configured — MASTER + SLAVE registered");
        return routingDataSource;
    }

    // ====================================
    // Primary DataSource (Lazy Proxy)
    // ====================================

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("routingDataSource") DataSource routingDataSource) {
        log.info("Configuring Primary DataSource with LazyConnectionDataSourceProxy");
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    // ====================================
    // Custom Health Indicator for Actuator
    // ====================================

    /**
     * Custom DB health indicator that checks master datasource directly.
     *
     * Replaces the default DataSourceHealthIndicator which can't introspect
     * the LazyConnectionDataSourceProxy + routing datasource properly.
     *
     * Accessible via: GET /actuator/health
     */
    // @Bean(name = "dbHealthIndicator")
    // public HealthIndicator databaseHealthIndicator(
    //         @Qualifier("masterDataSource") DataSource masterDataSource) {
    //     return () -> {
    //         try (Connection connection = masterDataSource.getConnection()) {
    //             boolean valid = connection.isValid(2); // 2 second timeout
    //             if (valid) {
    //                 return Health.up()
    //                         .withDetail("database", "PostgreSQL")
    //                         .withDetail("pool",     "HikariPool-Master")
    //                         .withDetail("status",   "Connection successful")
    //                         .build();
    //             } else {
    //                 return Health.down()
    //                         .withDetail("database", "PostgreSQL")
    //                         .withDetail("error",    "Connection validation failed")
    //                         .build();
    //             }
    //         } catch (Exception e) {
    //             log.error("Database health check failed", e);
    //             return Health.down()
    //                     .withDetail("database", "PostgreSQL")
    //                     .withDetail("error",    e.getMessage())
    //                     .build();
    //         }
    //     };
    // }
}