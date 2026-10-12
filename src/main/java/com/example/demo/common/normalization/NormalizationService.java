package com.example.demo.common.normalization;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Central normalization service
 * 
 * Orchestrates all normalizers
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Component
public class NormalizationService {

    private final List<Normalizer<?>> normalizers;

    public NormalizationService(List<Normalizer<?>> normalizers) {
        this.normalizers = normalizers != null ? normalizers : new ArrayList<>();
    }

    /**
     * Normalize an object by applying all applicable normalizers
     *
     * @param object Object to normalize
     * @param <T>    Object type
     * @return Normalized object
     */
    @SuppressWarnings("unchecked")
    public <T> T normalize(T object) {
        if (object == null) {
            return null;
        }

        T normalized = object;

        for (Normalizer<?> normalizer : normalizers) {
            if (normalizer.supports(object.getClass())) {
                try {
                    normalized = ((Normalizer<T>) normalizer).normalize(normalized);
                } catch (Exception e) {
                    // Log but don't fail - normalization should be lenient
                    System.err.println("Normalization failed: " + e.getMessage());
                }
            }
        }

        return normalized;
    }

    /**
     * Normalize a specific field value
     *
     * @param value      Value to normalize
     * @param fieldClass Field class
     * @param <T>        Value type
     * @return Normalized value
     */
    @SuppressWarnings("unchecked")
    public <T> T normalizeField(T value, Class<T> fieldClass) {
        if (value == null) {
            return null;
        }

        T normalized = value;

        for (Normalizer<?> normalizer : normalizers) {
            if (normalizer.supports(fieldClass)) {
                try {
                    normalized = ((Normalizer<T>) normalizer).normalize(normalized);
                } catch (Exception e) {
                    System.err.println("Field normalization failed: " + e.getMessage());
                }
            }
        }

        return normalized;
    }
}