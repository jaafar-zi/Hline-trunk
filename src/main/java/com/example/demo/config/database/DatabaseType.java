package com.example.demo.config.database;

/**
 * Enum for database routing types
 * Used to determine which datasource to use
 */
public enum DatabaseType {
    /**
     * Master database - used for write operations
     */
    MASTER,

    /**
     * Slave database - used for read operations
     */
    SLAVE
}