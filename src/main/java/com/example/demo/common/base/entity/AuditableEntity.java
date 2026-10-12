package com.example.demo.common.base.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Auditable entity with automatic tracking of creation and modification
 *
 * Features:
 * - Automatic createdAt/updatedAt timestamps
 * - Automatic createdBy/updatedBy user tracking
 * - Immutable creation fields
 * - UTC timezone for all timestamps
 *
 * Prerequisites:
 * - @EnableJpaAuditing in configuration
 * - AuditorAware bean configured
 *
 * Usage: Extend this for entities that need audit trail
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    /**
     * Timestamp when entity was created
     * Automatically set by Spring Data JPA
     * Stored in UTC, immutable after creation
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * User who created this entity
     * Automatically set from SecurityContext via AuditorAware
     * Immutable after creation
     */
    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    /**
     * Timestamp when entity was last updated
     * Automatically updated by Spring Data JPA
     * Stored in UTC
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User who last updated this entity
     * Automatically updated from SecurityContext via AuditorAware
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Pre-persist hook
     * Ensures createdAt/updatedAt are set if not already (fallback)
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * Pre-update hook
     * Ensures updatedAt is always refreshed
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get audit information summary
     * Useful for logging and debugging
     */
    @jakarta.persistence.Transient
    public String getAuditInfo() {
        return String.format("Created by %s at %s, Last modified by %s at %s",
                createdBy, createdAt, updatedBy, updatedAt);
    }
}