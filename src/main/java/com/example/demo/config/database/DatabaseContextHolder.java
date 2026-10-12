package com.example.demo.config.database;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local context holder for database routing
 * Stores the current database type (MASTER/SLAVE) for the current thread
 *
 * Usage:
 * - Set MASTER for write operations
 * - Set SLAVE for read operations
 * - Clear after operation completes
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
public class DatabaseContextHolder {

    private static final ThreadLocal<DatabaseType> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * Set the database type for current thread
     */
    public static void set(DatabaseType databaseType) {
        if (databaseType == null) {
            log.warn("Attempted to set null database type");
            return;
        }
        log.trace("Setting database type to: {}", databaseType);
        CONTEXT_HOLDER.set(databaseType);
    }

    /**
     * Get the current database type
     * Defaults to MASTER if not set
     */
    public static DatabaseType get() {
        DatabaseType type = CONTEXT_HOLDER.get();
        if (type == null) {
            log.trace("No database type set, defaulting to MASTER");
            return DatabaseType.MASTER;
        }
        return type;
    }

    /**
     * Clear the database type from current thread
     */
    public static void clear() {
        log.trace("Clearing database type from context");
        CONTEXT_HOLDER.remove();
    }

    /**
     * Check if database type is set
     */
    public static boolean isSet() {
        return CONTEXT_HOLDER.get() != null;
    }
}