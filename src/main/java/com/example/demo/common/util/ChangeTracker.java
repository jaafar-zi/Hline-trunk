package com.example.demo.common.util;

import com.example.demo.common.event.EntityUpdatedEvent.FieldChange;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility for tracking entity changes
 *
 * Usage:
 * <pre>
 * ChangeTracker tracker = new ChangeTracker();
 * tracker.track("displayName", oldValue, newValue);
 * tracker.track("priority", 5, 10);
 *
 * Map<String, FieldChange> changes = tracker.getChanges();
 * </pre>
 */
public class ChangeTracker {

    private final Map<String, FieldChange> changes = new HashMap<>();

    /**
     * Track a field change
     * Only adds if values are actually different
     */
    public ChangeTracker track(String fieldName, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.put(fieldName, new FieldChange(oldValue, newValue));
        }
        return this;
    }

    /**
     * Track a field change with custom condition
     */
    public ChangeTracker trackIf(boolean condition, String fieldName, Object oldValue, Object newValue) {
        if (condition) {
            track(fieldName, oldValue, newValue);
        }
        return this;
    }

    /**
     * Get all changes
     */
    public Map<String, FieldChange> getChanges() {
        return changes;
    }

    /**
     * Check if any changes exist
     */
    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    /**
     * Get number of changes
     */
    public int getChangeCount() {
        return changes.size();
    }

    /**
     * Clear all changes
     */
    public void clear() {
        changes.clear();
    }

    /**
     * Auto-detect changes between two objects (using reflection)
     *
     * WARNING: Only compares fields declared in the object's class, not superclasses
     * Use with caution - manual tracking is more reliable
     */
    public static Map<String, FieldChange> autoDetect(Object oldEntity, Object newEntity) {
        if (oldEntity == null || newEntity == null) {
            throw new IllegalArgumentException("Entities cannot be null");
        }
        if (!oldEntity.getClass().equals(newEntity.getClass())) {
            throw new IllegalArgumentException("Entities must be of same type");
        }

        ChangeTracker tracker = new ChangeTracker();
        Field[] fields = oldEntity.getClass().getDeclaredFields();

        for (Field field : fields) {
            // Skip static, transient, and synthetic fields
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isTransient(field.getModifiers()) ||
                    field.isSynthetic()) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);

                tracker.track(field.getName(), oldValue, newValue);
            } catch (IllegalAccessException e) {
                // Skip fields we can't access
            }
        }

        return tracker.getChanges();
    }
}