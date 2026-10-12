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
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserGroup Aggregate Root
 *
 * Represents a group of users with shared roles and permissions
 * Groups simplify user management by allowing role assignment at group level
 *
 * Business Rules:
 * - Group names must be unique
 * - Groups can have multiple roles
 * - Users can belong to one group
 * - Soft-deleting a group removes users from group but doesn't delete users
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
        name = "user_groups",
        indexes = {
                @Index(name = "idx_user_group_name", columnList = "name"),
                @Index(name = "idx_user_group_code", columnList = "code"),
                @Index(name = "idx_user_group_active", columnList = "is_active"),
                @Index(name = "idx_user_group_created_at", columnList = "created_at")
        }
)
public class UserGroup extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Attributes
    // ====================================

    /**
     * Group name (unique, human-readable)
     */
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    /**
     * Group code (unique, system identifier)
     */
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    /**
     * Group description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Is group active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Group priority (for ordering)
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    /**
     * Group metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // ====================================
    // Relationships
    // ====================================

    /**
     * Parent group (for hierarchical groups)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id")
    private UserGroup parentGroup;

    /**
     * Child groups
     */
    @OneToMany(mappedBy = "parentGroup", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<UserGroup> childGroups = new HashSet<>();

    /**
     * Roles assigned to this group
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_group_roles",
            joinColumns = @JoinColumn(name = "user_group_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Users in this group
     */
    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create a new user group
     *
     * Events Published:
     * - EntityCreatedEvent<UserGroup> (via AbstractCommandService after save)
     */
    public static UserGroup create(String name, String code, String description) {
        return UserGroup.builder()
                .name(name)
                .code(normalizeCode(code))
                .description(description)
                .active(true)
                .priority(0)
                .build();
        // No event registration - handled by AbstractCommandService
    }

    // ====================================
    // Domain Logic - Basic Updates
    // ====================================

    /**
     * Update group details
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void update(String name, String description, Integer priority) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            tracker.track("name", this.name, name);
            this.name = name;
        }

        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (priority != null && !priority.equals(this.priority)) {
            tracker.track("priority", this.priority, priority);
            this.priority = priority;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update metadata
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void updateMetadata(String metadata) {
        if (metadata != null && !metadata.equals(this.metadata)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("metadata", this.metadata, metadata);

            this.metadata = metadata;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Status Management
    // ====================================

    /**
     * Activate group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Group is already active");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", this.active, true);

        this.active = true;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Deactivate group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Group is already inactive");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("active", this.active, false);

        this.active = false;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    /**
     * Check if group is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    // ====================================
    // Domain Logic - Role Management
    // ====================================

    /**
     * Add role to group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        boolean added = this.roles.add(role);

        if (added) {
            EntityUpdatedEvent<UserGroup> event = new EntityUpdatedEvent<>(this);
            event.addChange("roles", null, "Added role: " + role.getName());
            registerEvent(event);
        }
    }

    /**
     * Remove role from group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void removeRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        boolean removed = this.roles.remove(role);

        if (removed) {
            EntityUpdatedEvent<UserGroup> event = new EntityUpdatedEvent<>(this);
            event.addChange("roles", "Removed role: " + role.getName(), null);
            registerEvent(event);
        }
    }

    /**
     * Replace all roles
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void setRoles(Set<Role> newRoles) {
        if (newRoles == null) {
            newRoles = new HashSet<>();
        }

        Set<String> oldRoleNames = this.roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> newRoleNames = newRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        if (!oldRoleNames.equals(newRoleNames)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("roles", oldRoleNames, newRoleNames);

            this.roles.clear();
            this.roles.addAll(newRoles);

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - User Management
    // ====================================

    /**
     * Add user to group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     *
     * NOTE: Service layer should also publish UserAddedToGroupEvent (cross-aggregate)
     */
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean added = this.users.add(user);
        user.setUserGroup(this);

        if (added) {
            EntityUpdatedEvent<UserGroup> event = new EntityUpdatedEvent<>(this);
            event.addChange("users", null, "Added user: " + user.getEmail().getValue());
            registerEvent(event);
        }
    }

    /**
     * Remove user from group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     *
     * NOTE: Service layer should also publish UserRemovedFromGroupEvent (cross-aggregate)
     */
    public void removeUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean removed = this.users.remove(user);
        user.setUserGroup(null);

        if (removed) {
            EntityUpdatedEvent<UserGroup> event = new EntityUpdatedEvent<>(this);
            event.addChange("users", "Removed user: " + user.getEmail().getValue(), null);
            registerEvent(event);
        }
    }

    // ====================================
    // Domain Logic - Hierarchy Management
    // ====================================

    /**
     * Add child group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void addChildGroup(UserGroup childGroup) {
        if (childGroup == null) {
            throw new IllegalArgumentException("Child group cannot be null");
        }
        if (childGroup.equals(this)) {
            throw new IllegalArgumentException("Group cannot be its own child");
        }

        childGroup.setParentGroup(this);
        this.childGroups.add(childGroup);

        EntityUpdatedEvent<UserGroup> event = new EntityUpdatedEvent<>(this);
        event.addChange("childGroups", null, "Added child group: " + childGroup.getName());
        registerEvent(event);
    }

    /**
     * Remove child group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void removeChildGroup(UserGroup childGroup) {
        if (childGroup == null) {
            throw new IllegalArgumentException("Child group cannot be null");
        }

        childGroup.setParentGroup(null);
        this.childGroups.remove(childGroup);

        EntityUpdatedEvent<UserGroup> event = new EntityUpdatedEvent<>(this);
        event.addChange("childGroups", "Removed child group: " + childGroup.getName(), null);
        registerEvent(event);
    }

    /**
     * Set parent group
     *
     * Events Published:
     * - EntityUpdatedEvent<UserGroup> (with change tracking)
     */
    public void setParent(UserGroup parentGroup) {
        if (parentGroup != null && parentGroup.equals(this)) {
            throw new IllegalArgumentException("Group cannot be its own parent");
        }

        String oldParent = this.parentGroup != null ? this.parentGroup.getName() : null;
        String newParent = parentGroup != null ? parentGroup.getName() : null;

        if (!Objects.equals(oldParent, newParent)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("parentGroup", oldParent, newParent);

            this.parentGroup = parentGroup;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Deletion
    // ====================================

    /**
     * Override delete to add validation
     *
     * Events Published:
     * - EntityDeletedEvent (via SoftDeletableAggregateRoot.delete())
     */
    @Override
    public void delete(String deletedBy) {
        validateDeletion();

        // Remove all users from group before soft delete
        if (this.users != null && !this.users.isEmpty()) {
            new HashSet<>(this.users).forEach(this::removeUser);
        }

        super.delete(deletedBy);
    }

    /**
     * Validate group can be deleted
     */
    public void validateDeletion() {
        // Note: We allow deletion even with users, but we remove them first
        // If you want to prevent deletion when users exist, uncomment:
        /*
        if (this.users != null && !this.users.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete group '%s'. It has %d user(s)",
                            this.name, this.users.size())
            );
        }
        */

        if (this.childGroups != null && !this.childGroups.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete group '%s'. It has %d child group(s)",
                            this.name, this.childGroups.size())
            );
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    /**
     * Get all roles including inherited from parent
     */
    @Transient
    public Set<Role> getAllRoles() {
        Set<Role> allRoles = new HashSet<>(this.roles);

        // Add parent roles (inheritance)
        if (parentGroup != null && parentGroup.getRoles() != null) {
            allRoles.addAll(parentGroup.getAllRoles());
        }

        return allRoles;
    }

    /**
     * Get all permissions from all roles
     */
    @Transient
    public Set<Permission> getAllPermissions() {
        return getAllRoles().stream()
                .flatMap(role -> role.getAllPermissions().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Check if group has specific permission
     */
    public boolean hasPermission(String permissionName) {
        return getAllPermissions().stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Get user count
     */
    @Transient
    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * Get active user count
     */
    @Transient
    public long getActiveUserCount() {
        if (users == null) {
            return 0;
        }
        return users.stream()
                .filter(User::isEnabled)
                .count();
    }

    /**
     * Get role names as comma-separated string
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
     * Get child group names
     */
    @Transient
    public String getChildGroupNames() {
        if (childGroups == null || childGroups.isEmpty()) {
            return "None";
        }
        return childGroups.stream()
                .map(UserGroup::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Check if group has children
     */
    @Transient
    public boolean hasChildren() {
        return childGroups != null && !childGroups.isEmpty();
    }

    /**
     * Check if group has parent
     */
    @Transient
    public boolean hasParent() {
        return parentGroup != null;
    }

    /**
     * Get hierarchy level (0 = root, 1 = first level child, etc.)
     */
    @Transient
    public int getHierarchyLevel() {
        int level = 0;
        UserGroup current = this.parentGroup;
        while (current != null) {
            level++;
            current = current.getParentGroup();
        }
        return level;
    }

    // ====================================
    // SoftDeletableAggregateRoot Implementation
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
                .entityClass(UserGroup.class)
                .entityType("UserGroup")
                .entityId(this.getId())
                .deletedBy(deletedBy)
                .softDelete(true)
                .reason(String.format("Group '%s' soft-deleted", this.name))
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
                .entityClass(UserGroup.class)
                .entityType("UserGroup")
                .entityId(this.getId())
                .restoredBy(restoredBy)
                .previousDeletedBy(previousDeletedBy)
                .previousDeletedAt(this.getDeletedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Helper Methods
    // ====================================

    /**
     * Normalize group code (uppercase, no spaces)
     */
    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Group code cannot be null or empty");
        }
        return code.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    // ====================================
    // Equals & HashCode
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGroup)) return false;
        UserGroup that = (UserGroup) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("UserGroup[id=%s, name=%s, code=%s, active=%s, members=%d, deleted=%s]",
                getId(), name, code, active, getUserCount(), isDeleted());
    }
}