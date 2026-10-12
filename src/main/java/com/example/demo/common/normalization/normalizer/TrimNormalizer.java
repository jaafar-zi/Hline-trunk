package com.example.demo.common.normalization.normalizer;

import com.example.demo.common.normalization.Normalizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Trims whitespace from strings
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Component
@Order(1) // Run first
public class TrimNormalizer implements Normalizer<String> {

    @Override
    public String normalize(String value) {
        return value != null ? value.trim() : null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }
}