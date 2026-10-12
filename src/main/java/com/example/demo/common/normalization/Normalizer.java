package com.example.demo.common.normalization;

/**
 * Interface for field normalizers
 * 
 * Normalizers transform/clean data BEFORE validation
 * They should NOT throw exceptions - only transform data
 * 
 * @param <T> Type to normalize
 * @author Architecture Team
 * @version 1.0
 */
public interface Normalizer<T> {

    /**
     * Normalize the given value
     *
     * @param value Value to normalize
     * @return Normalized value
     */
    T normalize(T value);

    /**
     * Check if this normalizer supports the given type
     *
     * @param clazz Class to check
     * @return true if supported
     */
    boolean supports(Class<?> clazz);
}