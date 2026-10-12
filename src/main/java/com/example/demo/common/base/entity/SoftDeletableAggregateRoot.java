package com.example.demo.common.base.entity;

import com.example.demo.common.event.DomainEvent;
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
 * Aggregate Root with event management + auditing + soft delete
 *
 * Use when: Your aggregate should never be hard-deleted
 *
 * Features:
 * - Soft delete with automatic event generation
 * - Restore capability with events
 * - Hibernate filters for automatic query filtering
 * - Audit trail of who/when deleted
 *
 * Subclasses can override:
 * - createDeletedEvent() for custom deletion events
 * - createRestoredEvent() for custom restoration events
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
public abstract class SoftDeletableAggregateRoot extends AuditableAggregateRoot {

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /**
     * Soft delete with automatic event generation
     *
     * @param deletedBy user/system performing deletion
     * @throws IllegalStateException if already deleted
     */
    public void delete(String deletedBy) {
        if (isDeleted()) {
            throw new IllegalStateException(
                    String.format("Aggregate %s[id=%s] is already deleted",
                            getAggregateType(), getId())
            );
        }

        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;

        // Register domain event
        registerEvent(createDeletedEvent(deletedBy));
    }

    /**
     * Convenience method using current audit context
     */
    public void delete() {
        // Will use @LastModifiedBy from audit context
        delete(this.getUpdatedBy() != null ? this.getUpdatedBy() : "SYSTEM");
    }

    /**
     * Restore soft-deleted aggregate with event generation
     *
     * @throws IllegalStateException if not deleted
     */
    public void restore() {
        if (!isDeleted()) {
            throw new IllegalStateException(
                    String.format("Aggregate %s[id=%s] is not deleted, cannot restore",
                            getAggregateType(), getId())
            );
        }

        String restoredBy = this.getUpdatedBy() != null ? this.getUpdatedBy() : "SYSTEM";
        String previousDeletedBy = this.deletedBy;

        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;

        // Register domain event
        registerEvent(createRestoredEvent(restoredBy, previousDeletedBy));
    }

    /**
     * Factory method for deletion event
     *
     * Override in domain aggregates for specific event types:
     *
     * Example:
     * @Override
     * protected DomainEvent createDeletedEvent(String deletedBy) {
     *     return new OrderDeletedEvent(getId(), getAggregateVersion(),
     *                                   this.customerId, deletedBy);
     * }
     */
    protected abstract DomainEvent createDeletedEvent(String deletedBy);

    /**
     * Factory method for restoration event
     *
     * Override in domain aggregates for specific event types
     */
    protected abstract DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy);

    /**
     * Check if aggregate is deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if aggregate is active (not deleted)
     */
    public boolean isActive() {
        return !isDeleted();
    }

    /**
     * Prevent hard deletes - force soft delete
     */
    @PreRemove
    private void preRemove() {
        if (!isDeleted()) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot hard delete aggregate %s[id=%s]. " +
                                    "Use delete() for soft delete or repository.hardDelete() if absolutely necessary.",
                            getAggregateType(), getId()
                    )
            );
        }
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, deleted=%s, version=%s]",
                getAggregateType(), getId(), deleted, getVersion());
    }
}