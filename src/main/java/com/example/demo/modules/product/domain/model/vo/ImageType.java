package com.example.demo.modules.product.domain.model.vo;

/**
 * Image Type Enumeration
 *
 * Defines the purpose and usage of product images
 */
public enum ImageType {
    /**
     * Primary product image (hero image)
     * Only one per product
     */
    PRIMARY,

    /**
     * Gallery/carousel images
     * Multiple allowed
     */
    GALLERY,

    /**
     * Thumbnail image
     * Used in lists and search results
     */
    THUMBNAIL,

    /**
     * Lifestyle/in-use images
     * Shows product in context
     */
    LIFESTYLE,

    /**
     * Detail/zoom images
     * High resolution for product details
     */
    DETAIL,

    /**
     * 360-degree view images
     * For interactive product viewing
     */
    SPIN,

    /**
     * Size chart images
     */
    SIZE_CHART,

    /**
     * Packaging images
     */
    PACKAGING;

    public boolean isUnique() {
        return this == PRIMARY || this == THUMBNAIL;
    }
}