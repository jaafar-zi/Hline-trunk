package com.example.demo.config.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;

/**
 * JPA and Hibernate configuration
 *
 * Features:
 * - Entity scanning
 * - Hibernate properties optimization
 * - JPA auditing with security context
 * - Transaction management
 * - Query optimization settings
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
@EnableJpaRepositories(          
        basePackages = "com.example.demo",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class JpaConfig {

    /**
     * Entity Manager Factory configuration
     */
    @Bean(name = "entityManagerFactory")   
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        log.info("========================================");
        log.info("Configuring EntityManagerFactory");
        log.info("========================================");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.demo"); 

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(false); 
        vendorAdapter.setGenerateDdl(false);
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        em.setJpaProperties(hibernateProperties());

        log.info("✓ EntityManagerFactory configured successfully");
        log.info("✓ Scanning packages: com.example.demo.modules");
        log.info("========================================");
        
        return em;
    }

    /**
     * Transaction Manager configuration
     */
    @Bean(name = "transactionManager") 
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {

        log.info("Configuring Transaction Manager");
        
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        
        log.info("✓ Transaction Manager configured");
        
        return transactionManager;
    }

    /**
     * Hibernate properties for performance optimization
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();

        // General settings
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto",        "validate");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");

        // Performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.query.in_clause_parameter_padding", "true");

        // Second-level cache (disabled in favor of Redis)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");

        // Connection pool settings
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");

        // Statistics (disable in production)
        properties.setProperty("hibernate.generate_statistics", "false");

        // Naming strategy
        properties.setProperty("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        // JDBC timezone
        properties.setProperty("hibernate.jdbc.time_zone", "UTC");

        log.debug("Hibernate properties configured");
        
        return properties;
    }

    /**
     * Auditor provider for JPA auditing
     * Extracts current user from Security Context
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            return Optional.of(authentication.getName());
        };
    }
}