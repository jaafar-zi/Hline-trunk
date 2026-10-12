package com.example.demo.modules.user.domain.model.vo;

/**
 * User Status Enum
 *
 * Represents the current status of a user account
 *
 * @author Architecture Team
 * @version 1.0
 */
public enum UserStatus {
    /**
     * Account is active and can be used
     */
    ACTIVE("Active", "User account is active"),

    /**
     * Account is inactive (manually disabled)
     */
    INACTIVE("Inactive", "User account is disabled"),

    /**
     * Account is locked (e.g., due to failed login attempts)
     */
    LOCKED("Locked", "User account is locked"),

    /**
     * Account is pending email verification
     */
    PENDING_VERIFICATION("Pending Verification", "Awaiting email verification"),

    /**
     * Account is expired
     */
    EXPIRED("Expired", "User account has expired"),

    /**
     * Account is suspended (temporary)
     */
    SUSPENDED("Suspended", "User account is temporarily suspended"),

    /**
     * Account is archived (soft deleted)
     */
    ARCHIVED("Archived", "User account is archived");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if status is active (can perform actions)
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Check if status allows login
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Check if status is recoverable (can be reactivated)
     */
    public boolean isRecoverable() {
        return this == INACTIVE || this == SUSPENDED || this == LOCKED;
    }
}