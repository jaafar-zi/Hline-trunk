package com.example.demo.config.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

/**
 * Custom routing datasource that routes queries to master or slave database
 * based on the current transaction context
 *
 * Routing logic:
 * - Write operations (INSERT, UPDATE, DELETE) → MASTER
 * - Read operations (SELECT) → SLAVE
 * - Transactional methods → MASTER
 * - @Transactional(readOnly = true) methods → SLAVE
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Determine which datasource to use based on ThreadLocal context
     */
    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        DatabaseType databaseType = DatabaseContextHolder.get();
        log.trace("Routing to database: {}", databaseType);
        return databaseType;
    }

    /**
     * Determine target datasource with fallback to master
     */
    @Override
    protected Object resolveSpecifiedLookupKey(Object lookupKey) {
        if (lookupKey == null) {
            log.debug("No lookup key specified, defaulting to MASTER");
            return DatabaseType.MASTER;
        }
        return lookupKey;
    }
}