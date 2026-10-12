package com.example.demo.modules.customer.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value Object representing a phone number
 * 
 * Supports E.164 international phone format
 * Immutable and self-validating
 * 
 * @author Customer Domain Team
 */
@Getter
@EqualsAndHashCode
public final class Phone implements Serializable {
    
    // E.164 format: +[country code][number] (max 15 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private final String value;
    private final String normalized;

    protected Phone() {
        this.value      = null;
        this.normalized = null;
    }

    public Phone(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        
        // Store original value
        this.value = value.trim();
        
        // Normalize by removing common separators
        String cleaned = this.value.replaceAll("[\\s()\\-.]", "");
        
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                "Invalid phone format. Expected E.164 format (e.g., +1234567890): " + value
            );
        }
        
        this.normalized = cleaned;
    }
    
    /**
     * Get normalized phone without formatting
     */
    public String getNormalized() {
        return normalized;
    }
    
    /**
     * Check if phone has country code
     */
    public boolean hasCountryCode() {
        return normalized.startsWith("+");
    }
    
    /**
     * Get country code if present
     */
    public String getCountryCode() {
        if (!hasCountryCode()) {
            return null;
        }
        
        // Extract country code (1-3 digits after +)
        int i = 1;
        while (i <= 3 && i < normalized.length() && Character.isDigit(normalized.charAt(i))) {
            i++;
        }
        return normalized.substring(0, i);
    }

    @Override
    public String toString() {
        return value;
    }
}