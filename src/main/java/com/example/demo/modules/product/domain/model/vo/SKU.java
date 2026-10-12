package com.example.demo.modules.product.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * SKU (Stock Keeping Unit) Value Object
 * 
 * Immutable value object representing unique product identifier
 * 
 * Business Rules:
 * - Must be unique across all products
 * - Must follow format: PREFIX-YYYYMMDD-RANDOM (e.g., PROD-20240115-A1B2C3)
 * - Alphanumeric characters and hyphens only
 * - Maximum 50 characters
 * - Case-insensitive comparison
 * 
 * @author PIM Team
 * @version 1.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class SKU implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_PREFIX = "PROD";
    private static final int RANDOM_PART_LENGTH = 6;
    private static final Pattern VALID_SKU_PATTERN = Pattern.compile("^[A-Z0-9-]{3,50}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Column(name = "value", length = 50)
    private String value;

    /**
     * Private constructor - use factory methods
     */
    private SKU(String value) {
        this.value = validateAndNormalize(value);
    }

    /**
     * Create SKU from existing value
     */
    public static SKU of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU value cannot be null or empty");
        }
        return new SKU(value);
    }

    /**
     * Generate new SKU with default prefix
     */
    public static SKU generate() {
        return generate(DEFAULT_PREFIX);
    }

    /**
     * Generate new SKU with custom prefix
     * Format: PREFIX-YYYYMMDD-RANDOM
     */
    public static SKU generate(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            prefix = DEFAULT_PREFIX;
        }
        
        String normalizedPrefix = prefix.toUpperCase().replaceAll("[^A-Z0-9]", "");
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String randomPart = generateRandomPart();
        
        String skuValue = String.format("%s-%s-%s", normalizedPrefix, datePart, randomPart);
        
        return new SKU(skuValue);
    }

    /**
     * Validate and normalize SKU value
     */
    private String validateAndNormalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU value cannot be null or empty");
        }

        // Normalize: uppercase and trim
        String normalized = value.trim().toUpperCase();

        // Validate length
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("SKU cannot exceed 50 characters");
        }

        if (normalized.length() < 3) {
            throw new IllegalArgumentException("SKU must be at least 3 characters");
        }

        // Validate pattern
        if (!VALID_SKU_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "SKU must contain only alphanumeric characters and hyphens: " + value
            );
        }

        return normalized;
    }

    /**
     * Generate random alphanumeric part
     */
    private static String generateRandomPart() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return uuid.substring(0, RANDOM_PART_LENGTH);
    }

    /**
     * Check if SKU matches pattern
     */
    public boolean hasPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return false;
        }
        return value.startsWith(prefix.toUpperCase());
    }

    /**
     * Extract prefix from SKU
     */
    public String getPrefix() {
        int firstHyphen = value.indexOf('-');
        return firstHyphen > 0 ? value.substring(0, firstHyphen) : value;
    }

    /**
     * Check if SKU is valid format
     */
    public boolean isValid() {
        return VALID_SKU_PATTERN.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
