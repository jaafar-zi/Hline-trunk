package com.example.demo.common.base.entity;

import java.io.Serializable;

/**
 * Base class for Value Objects (DDD pattern)
 *
 * Value Objects characteristics:
 * - Immutable (no setters)
 * - Equality based on values, not identity
 * - No ID field
 * - Can be shared across entities
 *
 * Examples: Money, Address, DateRange, Email, etc.
 *
 * @author Architecture Team
 * @version 1.0
 */
public abstract class BaseValueObject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Validate value object state
     * Called after construction to ensure invariants
     *
     * @throws IllegalArgumentException if validation fails
     */
    protected abstract void validate();

    /**
     * Value objects must implement equals based on all fields
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Value objects must implement hashCode based on all fields
     */
    @Override
    public abstract int hashCode();

    /**
     * Value objects should have meaningful toString
     */
    @Override
    public abstract String toString();
}