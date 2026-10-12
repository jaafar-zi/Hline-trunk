package com.example.demo.common.base.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity class for all domain entities
 *
 * Features:
 * - UUID-based primary keys for distributed systems
 * - Optimistic locking with @Version
 * - Proper equals/hashCode implementation
 * - Implements Persistable for better new entity detection
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Persistable<UUID>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - UUID for better distribution and security
     * Uses Hibernate 6.x UUID generator
     */
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * Version for optimistic locking
     * Prevents lost updates in concurrent scenarios
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Check if entity is new (not yet persisted)
     * Required by Persistable interface
     * More reliable than checking if ID is null
     */
    @Override
    @Transient
    public boolean isNew() {
        return this.id == null || this.version == null;
    }

    /**
     * Equality based on ID
     * Follows JPA best practices
     * - Returns false if ID is null (transient entities)
     * - Compares by ID if both entities are persisted
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    /**
     * HashCode based on class
     * Consistent across transient and persisted states
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * String representation
     */
    @Override
    public String toString() {
        return String.format("%s[id=%s]", getClass().getSimpleName(), id);
    }
}