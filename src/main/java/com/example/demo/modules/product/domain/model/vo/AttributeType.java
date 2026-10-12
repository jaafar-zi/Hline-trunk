package com.example.demo.modules.product.domain.model.vo;

/**
 * Attribute Type Enumeration
 *
 * Defines the data type and UI control for attributes
 */
public enum AttributeType {
    /**
     * Single-line text input
     */
    TEXT,

    /**
     * Multi-line text input
     */
    TEXTAREA,

    /**
     * Numeric value
     */
    NUMBER,

    /**
     * Boolean/checkbox
     */
    BOOLEAN,

    /**
     * Date picker
     */
    DATE,

    /**
     * Date and time picker
     */
    DATETIME,

    /**
     * Single selection dropdown
     */
    SELECT,

    /**
     * Multiple selection
     */
    MULTI_SELECT,

    /**
     * Email input with validation
     */
    EMAIL,

    /**
     * URL input with validation
     */
    URL,

    /**
     * Rich text editor
     */
    RICH_TEXT,

    /**
     * File upload
     */
    FILE,

    /**
     * Image upload
     */
    IMAGE,

    /**
     * Color picker
     */
    COLOR;

    public boolean isSelectType() {
        return this == SELECT || this == MULTI_SELECT;
    }

    public boolean requiresOptions() {
        return isSelectType();
    }

    public boolean isFileType() {
        return this == FILE || this == IMAGE;
    }
}
