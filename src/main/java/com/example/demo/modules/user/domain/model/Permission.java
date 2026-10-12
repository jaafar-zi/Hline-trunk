package com.example.demo.modules.user.domain.model;

import com.example.demo.common.base.entity.AuditableAggregateRoot;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.user.domain.model.vo.DefaultPermission;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Permission Aggregate Root
 *
 * Represents a granular security permission
 * Permissions define specific actions that can be performed on resources
 *
 * Format: RESOURCE:ACTION
 * Examples: USER:CREATE, PRODUCT:DELETE, REPORT:VIEW
 *
 * Business Rules:
 * - Permission names must be unique
 * - Permissions are immutable after creation (only description/metadata can change)
 * - System permissions cannot be deleted
 * - Can be hard-deleted (no soft delete)
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "permissions",
        indexes = {
                @Index(name = "idx_permission_name", columnList = "name"),
                @Index(name = "idx_permission_resource", columnList = "resource"),
                @Index(name = "idx_permission_action", columnList = "action"),
                @Index(name = "idx_permission_system", columnList = "is_system_permission"),
                @Index(name = "idx_permission_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_resource_action", columnNames = {"resource", "action"})
        }
)
public class Permission extends AuditableAggregateRoot {

    // ====================================
    // Core Attributes (Immutable)
    // ====================================

    /**
     * Permission name (unique identifier)
     * Format: RESOURCE:ACTION (e.g., USER:CREATE, PRODUCT:DELETE)
     * IMMUTABLE after creation
     */
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    /**
     * Resource type (e.g., USER, PRODUCT, ORDER)
     * IMMUTABLE after creation
     */
    @Column(name = "resource", nullable = false, length = 50)
    private String resource;

    /**
     * Action type (e.g., CREATE, READ, UPDATE, DELETE)
     * IMMUTABLE after creation
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Is this a system permission (cannot be deleted)
     * IMMUTABLE after creation
     */
    @Column(name = "is_system_permission", nullable = false)
    @Builder.Default
    private Boolean systemPermission = false;

    // ====================================
    // Mutable Attributes (Metadata)
    // ====================================

    /**
     * Permission description (can be updated)
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Permission category (for grouping in UI)
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * Display order (for UI)
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    // ====================================
    // Relationships
    // ====================================

    /**
     * Roles that have this permission (inverse side)
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create a new permission
     *
     * Events Published:
     * - EntityCreatedEvent<Permission> (via AbstractCommandService after save)
     */
    public static Permission create(String resource, String action, String description) {
        String name = buildPermissionName(resource, action);
        return Permission.builder()
                .name(name)
                .resource(resource.toUpperCase())
                .action(action.toUpperCase())
                .description(description)
                .systemPermission(false)
                .build();
        // No event registration - handled by AbstractCommandService
    }

    /**
     * Create a system permission
     *
     * Events Published:
     * - EntityCreatedEvent<Permission> (via AbstractCommandService after save)
     */
    public static Permission createSystemPermission(String resource, String action, String description) {
        String name = buildPermissionName(resource, action);
        return Permission.builder()
                .name(name)
                .resource(resource.toUpperCase())
                .action(action.toUpperCase())
                .description(description)
                .systemPermission(true)
                .build();
        // No event registration - handled by AbstractCommandService
    }

    /**
     * Create from DefaultPermission enum
     *
     * Events Published:
     * - EntityCreatedEvent<Permission> (via AbstractCommandService after save)
     */
    public static Permission fromEnum(DefaultPermission defaultPermission) {
        String[] parts = defaultPermission.name().split("_", 2);
        String action = parts[0];
        String resource = parts.length > 1 ? parts[1] : "GENERAL";

        return Permission.builder()
                .name(defaultPermission.name())
                .resource(resource)
                .action(action)
                .description("System permission: " + defaultPermission.name())
                .systemPermission(true)
                .build();
        // No event registration - handled by AbstractCommandService
    }

    // ====================================
    // Domain Logic
    // ====================================

    /**
     * Update permission metadata (only mutable fields)
     *
     * Events Published:
     * - EntityUpdatedEvent<Permission> (with change tracking)
     *
     * NOTE: Cannot update name, resource, action (immutable for integrity)
     */
    public void update(String description, String category, Integer displayOrder) {
        if (this.systemPermission) {
            throw new IllegalStateException("Cannot update system permission: " + this.name);
        }

        ChangeTracker tracker = new ChangeTracker();

        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (category != null && !category.equals(this.category)) {
            tracker.track("category", this.category, category);
            this.category = category;
        }

        if (displayOrder != null && !displayOrder.equals(this.displayOrder)) {
            tracker.track("displayOrder", this.displayOrder, displayOrder);
            this.displayOrder = displayOrder;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update only description (convenience method)
     *
     * Events Published:
     * - EntityUpdatedEvent<Permission> (with change tracking)
     */
    public void updateDescription(String description) {
        if (this.systemPermission) {
            throw new IllegalStateException("Cannot update system permission: " + this.name);
        }

        if (description != null && !description.equals(this.description)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("description", this.description, description);

            this.description = description;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update category (convenience method)
     *
     * Events Published:
     * - EntityUpdatedEvent<Permission> (with change tracking)
     */
    public void updateCategory(String category) {
        if (this.systemPermission) {
            throw new IllegalStateException("Cannot update system permission: " + this.name);
        }

        if (category != null && !category.equals(this.category)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("category", this.category, category);

            this.category = category;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Validate permission can be deleted
     *
     * NOTE: No soft delete for permissions - this is hard delete validation
     */
    public void validateDeletion() {
        if (this.systemPermission) {
            throw new IllegalStateException("Cannot delete system permission: " + this.name);
        }
        if (this.roles != null && !this.roles.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete permission '%s'. It is assigned to %d role(s)",
                            this.name, this.roles.size())
            );
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Check if this permission matches a resource and action
     */
    public boolean matches(String resource, String action) {
        return this.resource.equalsIgnoreCase(resource) &&
                this.action.equalsIgnoreCase(action);
    }

    /**
     * Check if this is a wildcard permission (e.g., *:*, USER:*)
     */
    @Transient
    public boolean isWildcard() {
        return "*".equals(resource) || "*".equals(action);
    }

    /**
     * Check if this permission implies another permission
     * (e.g., USER:* implies USER:READ, USER:WRITE, etc.)
     */
    public boolean implies(Permission other) {
        if (other == null) {
            return false;
        }

        // Exact match
        if (this.equals(other)) {
            return true;
        }

        // Wildcard resource
        if ("*".equals(this.resource)) {
            return "*".equals(this.action) || this.action.equals(other.action);
        }

        // Wildcard action
        if ("*".equals(this.action)) {
            return this.resource.equals(other.resource);
        }

        return false;
    }

    /**
     * Get role count
     */
    @Transient
    public int getRoleCount() {
        return roles != null ? roles.size() : 0;
    }

    /**
     * Get assigned role names
     */
    @Transient
    public String getRoleNames() {
        if (roles == null || roles.isEmpty()) {
            return "None";
        }
        return roles.stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Check if permission is in use
     */
    @Transient
    public boolean isInUse() {
        return roles != null && !roles.isEmpty();
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Build permission name from resource and action
     */
    private static String buildPermissionName(String resource, String action) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Resource cannot be null or empty");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
        return resource.toUpperCase() + ":" + action.toUpperCase();
    }

    // ====================================
    // Equals & HashCode
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Permission[id=%s, name=%s, system=%s, roles=%d]",
                getId(), name, systemPermission, getRoleCount());
    }
}