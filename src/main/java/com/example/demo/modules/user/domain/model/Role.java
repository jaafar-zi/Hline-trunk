package com.example.demo.modules.user.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role Entity
 *
 * Represents a security role in the system
 * Roles group permissions and are assigned to users
 *
 * Business Rules:
 * - Role names must be unique
 * - Roles can have hierarchical relationships (parent-child)
 * - System roles (e.g., ADMIN, USER) cannot be deleted
 * - Roles must have at least one permission
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
        name = "roles",
        indexes = {
                @Index(name = "idx_role_name", columnList = "name"),
                @Index(name = "idx_role_system", columnList = "is_system_role"),
                @Index(name = "idx_role_parent", columnList = "parent_id"),
                @Index(name = "idx_role_created_at", columnList = "created_at")
        }
)
public class Role extends SoftDeletableAggregateRoot {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    @Builder.Default
    private Boolean systemRole = false;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Role parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Role> childRoles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    public static Role create(String name, String displayName, String description) {
        return Role.builder()
                .name(normalizeRoleName(name))
                .displayName(displayName)
                .description(description)
                .systemRole(false)
                .priority(0)
                .build();
    }

    public static Role createSystemRole(String name, String displayName, int priority) {
        return Role.builder()
                .name(normalizeRoleName(name))
                .displayName(displayName)
                .systemRole(true)
                .priority(priority)
                .build();
    }

    // ====================================
    // Domain Logic with Change Tracking
    // ====================================

    /**
     * Update role details with change tracking
     */
    public void update(String displayName, String description, Integer priority) {
        ChangeTracker tracker = new ChangeTracker();

        if (displayName != null && !displayName.equals(this.displayName)) {
            tracker.track("displayName", this.displayName, displayName);
            this.displayName = displayName;
        }

        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (priority != null && !priority.equals(this.priority)) {
            if (this.systemRole) {
                throw new IllegalStateException("Cannot modify system role priority");
            }
            tracker.track("priority", this.priority, priority);
            this.priority = priority;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Add permission with change tracking
     */
    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        boolean added = this.permissions.add(permission);

        if (added) {
            // Track permission addition
            EntityUpdatedEvent<Role> event = new EntityUpdatedEvent<>(this);
            event.addChange("permissions", null, permission.getName() + " added");
            registerEvent(event);
        }
    }

    /**
     * Remove permission with change tracking
     */
    public void removePermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        if (this.systemRole) {
            throw new IllegalStateException("Cannot modify system role permissions");
        }

        boolean removed = this.permissions.remove(permission);

        if (removed) {
            // Track permission removal
            EntityUpdatedEvent<Role> event = new EntityUpdatedEvent<>(this);
            event.addChange("permissions", permission.getName() + " removed", null);
            registerEvent(event);
        }
    }

    /**
     * Add child role with change tracking
     */
    public void addChildRole(Role childRole) {
        if (childRole == null) {
            throw new IllegalArgumentException("Child role cannot be null");
        }
        if (childRole.equals(this)) {
            throw new IllegalArgumentException("Role cannot be its own child");
        }

        childRole.setParent(this);
        this.childRoles.add(childRole);

        EntityUpdatedEvent<Role> event = new EntityUpdatedEvent<>(this);
        event.addChange("childRoles", null, "Added child role: " + childRole.getName());
        registerEvent(event);
    }

    /**
     * Remove child role with change tracking
     */
    public void removeChildRole(Role childRole) {
        if (childRole == null) {
            throw new IllegalArgumentException("Child role cannot be null");
        }

        childRole.setParent(null);
        this.childRoles.remove(childRole);

        EntityUpdatedEvent<Role> event = new EntityUpdatedEvent<>(this);
        event.addChange("childRoles", "Removed child role: " + childRole.getName(), null);
        registerEvent(event);
    }

    @Override
    public void delete(String deletedBy) {
        validateDeletion();
        super.delete(deletedBy);
    }

    public void validateDeletion() {
        if (this.systemRole) {
            throw new IllegalStateException("Cannot delete system role: " + this.name);
        }
        if (this.users != null && !this.users.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete role '%s'. It is assigned to %d user(s)",
                            this.name, this.users.size())
            );
        }
        if (this.childRoles != null && !this.childRoles.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete role '%s'. It has %d child role(s)",
                            this.name, this.childRoles.size())
            );
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    @Transient
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>(this.permissions);
        if (parent != null && parent.getPermissions() != null) {
            allPermissions.addAll(parent.getAllPermissions());
        }
        return allPermissions;
    }

    public boolean hasPermission(String permissionName) {
        return getAllPermissions().stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    public boolean hasHigherPriorityThan(Role otherRole) {
        if (otherRole == null) {
            return true;
        }
        return this.priority > otherRole.getPriority();
    }

    @Transient
    public boolean isAdmin() {
        return this.name != null && this.name.contains("ADMIN");
    }

    @Transient
    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    @Transient
    public String getPermissionNames() {
        return permissions.stream()
                .map(Permission::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    // ====================================
    // Infrastructure
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
                .entityClass(Role.class)
                .entityType("Role")
                .entityId(this.getId())
                .deletedBy(deletedBy)
                .softDelete(true)
                .reason(this.systemRole ? "System role archived" : null)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
                .entityClass(Role.class)
                .entityType("Role")
                .entityId(this.getId())
                .restoredBy(restoredBy)
                .previousDeletedBy(previousDeletedBy)
                .previousDeletedAt(this.getDeletedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private static String normalizeRoleName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        String normalized = name.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return getId() != null && getId().equals(role.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Role[id=%s, name=%s, priority=%d, system=%s, deleted=%s]",
                getId(), name, priority, systemRole, isDeleted());
    }
}