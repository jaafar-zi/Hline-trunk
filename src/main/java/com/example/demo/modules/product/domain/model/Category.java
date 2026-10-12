package com.example.demo.modules.product.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.product.domain.event.CategoryActivatedEvent;
import com.example.demo.modules.product.domain.event.CategoryDeactivatedEvent;
import com.example.demo.modules.product.domain.event.CategoryMovedEvent;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Category Aggregate Root
 *
 * Represents product categorization in PIM system
 * Supports hierarchical structure with parent-child relationships
 *
 * Business Rules:
 * - Category must have unique code
 * - Category name must be unique within same parent
 * - Cannot delete category with active products
 * - Cannot set category as its own parent
 * - Maximum depth limit for hierarchy (configurable)
 *
 * Events Published:
 * - EntityCreatedEvent<Category> (via AbstractCommandService after save)
 * - EntityUpdatedEvent<Category> (with change tracking for all updates)
 * - CategoryMovedEvent (when parent changes)
 * - CategoryActivatedEvent
 * - CategoryDeactivatedEvent
 * - EntityDeletedEvent (soft delete)
 * - EntityRestoredEvent (restore)
 *
 * @author PIM Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_category_code", columnList = "code"),
                @Index(name = "idx_category_parent", columnList = "parent_id"),
                @Index(name = "idx_category_active", columnList = "active"),
                @Index(name = "idx_category_display_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_code", columnNames = "code")
        }
)
public class Category extends SoftDeletableAggregateRoot {

    // ====================================
    // Core Category Information
    // ====================================

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    // ====================================
    // Hierarchy
    // ====================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = false,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<Category> children = new HashSet<>();

    @Column(name = "level")
    @Builder.Default
    private Integer level = 0;

    @Column(name = "path", length = 1000)
    private String path;

    // ====================================
    // SEO and Display
    // ====================================

    @Column(name = "url_slug", unique = true, length = 500)
    private String urlSlug;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "icon", length = 255)
    private String icon;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ====================================
    // Relationships
    // ====================================

    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("displayOrder ASC, key ASC")
    @Builder.Default
    private Set<CategoryAttribute> attributes = new HashSet<>();

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Create root category
     *
     * Events Published:
     * - EntityCreatedEvent<Category> (via AbstractCommandService after save)
     */
    public static Category createRoot(String code, String name) {
        validateCode(code);
        validateName(name);

        return Category.builder()
                .code(code.toUpperCase())
                .name(name)
                .level(0)
                .path("/" + code.toUpperCase())
                .urlSlug(generateSlug(name))
                .active(true)
                .displayOrder(0)
                .build();
    }

    /**
     * Create child category
     */
    public static Category createChild(String code, String name, Category parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent category cannot be null for child category");
        }

        validateCode(code);
        validateName(name);

        Category child = Category.builder()
                .code(code.toUpperCase())
                .name(name)
                .parent(parent)
                .level(parent.getLevel() + 1)
                .path(parent.getPath() + "/" + code.toUpperCase())
                .urlSlug(generateSlug(name))
                .active(true)
                .displayOrder(0)
                .build();

        parent.addChild(child);

        return child;
    }

    // ====================================
    // Domain Logic - Basic Information
    // ====================================

    /**
     * Update basic information
     *
     * Events Published:
     * - EntityUpdatedEvent<Category> (with change tracking)
     */
    public void updateBasicInfo(String name, String description) {
        ChangeTracker tracker = new ChangeTracker();

        if (name != null && !name.equals(this.name)) {
            validateName(name);
            tracker.track("name", this.name, name);
            this.name = name;
            this.urlSlug = generateSlug(name);
        }

        if (description != null && !description.equals(this.description)) {
            tracker.track("description", this.description, description);
            this.description = description;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update display order
     */
    public void updateDisplayOrder(Integer displayOrder) {
        if (!Objects.equals(this.displayOrder, displayOrder)) {
            ChangeTracker tracker = new ChangeTracker();
            tracker.track("displayOrder", this.displayOrder, displayOrder);

            this.displayOrder = displayOrder;

            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update SEO information
     */
    public void updateSEO(String metaTitle, String metaDescription) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("metaTitle", this.metaTitle, metaTitle);
        tracker.track("metaDescription", this.metaDescription, metaDescription);

        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update visual elements
     */
    public void updateVisuals(String icon, String imageUrl) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("icon", this.icon, icon);
        tracker.track("imageUrl", this.imageUrl, imageUrl);

        this.icon = icon;
        this.imageUrl = imageUrl;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Domain Logic - Hierarchy
    // ====================================

    /**
     * Move category to new parent
     *
     * Events Published:
     * - CategoryMovedEvent (domain-specific)
     */
    public void moveToParent(Category newParent) {
        if (newParent != null) {
            validateParentChange(newParent);
        }

        Category oldParent = this.parent;
        String oldPath = this.path;
        Integer oldLevel = this.level;

        // Remove from old parent
        if (this.parent != null) {
            this.parent.removeChild(this);
        }

        // Set new parent
        this.parent = newParent;

        // Update level and path
        if (newParent == null) {
            this.level = 0;
            this.path = "/" + this.code;
        } else {
            this.level = newParent.getLevel() + 1;
            this.path = newParent.getPath() + "/" + this.code;
            newParent.addChild(this);
        }

        // Update all descendants
        updateDescendantsPaths();

        registerEvent(CategoryMovedEvent.builder()
                .categoryId(this.getId())
                .categoryCode(this.code)
                .categoryName(this.name)
                .oldParentId(oldParent != null ? oldParent.getId() : null)
                .oldParentName(oldParent != null ? oldParent.getName() : null)
                .newParentId(newParent != null ? newParent.getId() : null)
                .newParentName(newParent != null ? newParent.getName() : null)
                .oldLevel(oldLevel)
                .newLevel(this.level)
                .oldPath(oldPath)
                .newPath(this.path)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Add child category
     */
    private void addChild(Category child) {
        if (child == null) {
            throw new IllegalArgumentException("Child category cannot be null");
        }
        this.children.add(child);
    }

    /**
     * Remove child category
     */
    private void removeChild(Category child) {
        if (child == null) {
            throw new IllegalArgumentException("Child category cannot be null");
        }
        this.children.remove(child);
    }

    /**
     * Update paths for all descendants recursively
     */
    private void updateDescendantsPaths() {
        for (Category child : children) {
            child.level = this.level + 1;
            child.path = this.path + "/" + child.code;
            child.updateDescendantsPaths();
        }
    }

    /**
     * Validate parent change is allowed
     */
    private void validateParentChange(Category newParent) {
        // Cannot set self as parent
        if (this.equals(newParent)) {
            throw new IllegalStateException("Category cannot be its own parent");
        }

        // Cannot set descendant as parent (circular reference)
        if (isDescendantOf(newParent)) {
            throw new IllegalStateException("Cannot set descendant as parent");
        }

        // Check max depth
        int newDepth = newParent.getLevel() + 1 + getMaxDescendantDepth();
        if (newDepth > getMaxAllowedDepth()) {
            throw new IllegalStateException(
                    String.format("Moving category would exceed maximum depth of %d levels",
                            getMaxAllowedDepth())
            );
        }
    }

    /**
     * Check if category is descendant of another
     */
    @Transient
    public boolean isDescendantOf(Category potentialAncestor) {
        if (potentialAncestor == null) {
            return false;
        }

        Category current = this.parent;
        while (current != null) {
            if (current.equals(potentialAncestor)) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    /**
     * Check if category is ancestor of another
     */
    @Transient
    public boolean isAncestorOf(Category potentialDescendant) {
        return potentialDescendant != null && potentialDescendant.isDescendantOf(this);
    }

    /**
     * Get maximum depth of descendants
     */
    @Transient
    private int getMaxDescendantDepth() {
        if (children.isEmpty()) {
            return 0;
        }

        int maxDepth = 0;
        for (Category child : children) {
            maxDepth = Math.max(maxDepth, 1 + child.getMaxDescendantDepth());
        }
        return maxDepth;
    }

    @Transient
    private int getMaxAllowedDepth() {
        return 5; // Configurable
    }

    @Transient
    public boolean isRoot() {
        return parent == null;
    }

    @Transient
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Transient
    public List<Category> getAncestors() {
        List<Category> ancestors = new ArrayList<>();
        Category current = this.parent;
        while (current != null) {
            ancestors.add(0, current);
            current = current.parent;
        }
        return ancestors;
    }

    @Transient
    public List<Category> getAllDescendants() {
        List<Category> descendants = new ArrayList<>();
        for (Category child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    // ====================================
    // Domain Logic - Attributes
    // ====================================

    /**
     * Add attribute template
     *
     * Events Published:
     * - EntityUpdatedEvent<Category> (with change tracking)
     */
    public void addAttribute(CategoryAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }

        if (hasAttribute(attribute.getKey())) {
            throw new IllegalStateException("Attribute already exists: " + attribute.getKey());
        }

        attribute.setCategory(this);
        this.attributes.add(attribute);

        EntityUpdatedEvent<Category> event = new EntityUpdatedEvent<>(this);
        event.addChange("attributes", null, "Added attribute: " + attribute.getKey());
        registerEvent(event);
    }

    /**
     * Remove attribute
     */
    public void removeAttribute(String key) {
        CategoryAttribute attribute = getAttribute(key);
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute not found: " + key);
        }

        this.attributes.remove(attribute);

        EntityUpdatedEvent<Category> event = new EntityUpdatedEvent<>(this);
        event.addChange("attributes", "Removed attribute: " + key, null);
        registerEvent(event);
    }

    public CategoryAttribute getAttribute(String key) {
        return attributes.stream()
                .filter(attr -> attr.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public boolean hasAttribute(String key) {
        return getAttribute(key) != null;
    }

    @Transient
    public Set<CategoryAttribute> getAllInheritedAttributes() {
        Set<CategoryAttribute> allAttributes = new HashSet<>(this.attributes);

        Category current = this.parent;
        while (current != null) {
            allAttributes.addAll(current.getAttributes());
            current = current.parent;
        }

        return allAttributes;
    }

    // ====================================
    // Domain Logic - Activation
    // ====================================

    /**
     * Activate category
     *
     * Events Published:
     * - CategoryActivatedEvent
     */
    public void activate() {
        if (Boolean.TRUE.equals(this.active)) {
            throw new IllegalStateException("Category is already active");
        }

        this.active = true;

        registerEvent(CategoryActivatedEvent.builder()
                .categoryId(this.getId())
                .categoryCode(this.code)
                .categoryName(this.name)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Deactivate category
     *
     * Events Published:
     * - CategoryDeactivatedEvent
     */
    public void deactivate(String reason) {
        if (Boolean.FALSE.equals(this.active)) {
            throw new IllegalStateException("Category is already inactive");
        }

        this.active = false;

        registerEvent(CategoryDeactivatedEvent.builder()
                .categoryId(this.getId())
                .categoryCode(this.code)
                .categoryName(this.name)
                .reason(reason)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    // ====================================
    // Validation
    // ====================================

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Category code cannot be null or empty");
        }
        if (code.length() > 50) {
            throw new IllegalArgumentException("Category code cannot exceed 50 characters");
        }
        if (!code.matches("^[A-Z0-9_-]+$")) {
            throw new IllegalArgumentException(
                    "Category code must contain only uppercase letters, numbers, hyphens and underscores");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Category name cannot exceed 255 characters");
        }
    }

    // ====================================
    // SoftDeletableAggregateRoot Implementation
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
                .entityClass(Category.class)
                .entityType("Category")
                .entityId(this.getId())
                .deletedBy(deletedBy)
                .softDelete(true)
                .reason("Category soft-deleted: " + this.name)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
                .entityClass(Category.class)
                .entityType("Category")
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

    private static String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return code != null && code.equals(category.code);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Category[id=%s, code=%s, name=%s, level=%d, active=%s]",
                getId(), code, name, level, active);
    }
}
