package com.example.demo.common.normalization.normalizer;

import com.example.demo.common.normalization.Normalizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Converts empty strings to null
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Component
@Order(2) // Run after trim
public class EmptyToNullNormalizer implements Normalizer<String> {

    @Override
    public String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.isEmpty() ? null : value;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }
}