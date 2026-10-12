package com.example.demo.common.base.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Soft-deletable entity
 * Entities are marked as deleted instead of being physically removed
 *
 * Features:
 * - Soft delete instead of hard delete
 * - Track deletion timestamp and user
 * - Automatic filtering of deleted records
 * - Restore capability
 * - Integration with Spring Security for deletedBy
 *
 * Usage: Extend this for entities that should never be permanently deleted
 *
 * Database indexes recommended:
 * - CREATE INDEX idx_deleted ON table_name(deleted)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
@Setter
@MappedSuperclass
@SQLDelete(sql = "UPDATE {h-schema}{h-table} SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@FilterDef(
        name = "deletedFilter",
        parameters = @ParamDef(name = "isDeleted", type = Boolean.class)
)
@Filter(
        name = "deletedFilter",
        condition = "deleted = :isDeleted"
)
public abstract class SoftDeletableEntity extends AuditableEntity {

    /**
     * Soft delete flag
     * true = deleted, false = active
     * Default: false (active)
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    /**
     * Timestamp when entity was soft-deleted
     * Null for active entities
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * User who soft-deleted this entity
     * Null for active entities
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /**
     * Mark entity as deleted
     *
     * @param deletedBy username or system identifier
     */
    public void delete(String deletedBy) {
        if (isDeleted()) {
            throw new IllegalStateException("Entity is already deleted");
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Mark entity as deleted (automatic from SecurityContext)
     * Useful when called from service layer with security context
     */
    public void delete() {
        // deletedBy will be set automatically if Spring Security is configured
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore soft-deleted entity
     * Clears deletion metadata
     */
    public void restore() {
        if (!isDeleted()) {
            throw new IllegalStateException("Entity is not deleted");
        }
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Check if entity is deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if entity is active (not deleted)
     */
    public boolean isActive() {
        return !isDeleted();
    }

    /**
     * Pre-remove hook
     * Prevents hard delete, forces soft delete instead
     */
    @PreRemove
    private void preRemove() {
        if (!isDeleted()) {
            throw new IllegalStateException(
                    "Cannot hard delete entity. Use delete() method for soft delete or hardDelete() in repository."
            );
        }
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, deleted=%s]", getClass().getSimpleName(), getId(), deleted);
    }
}