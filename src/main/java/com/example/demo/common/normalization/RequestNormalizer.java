package com.example.demo.common.normalization;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * Request normalizer using @Normalize annotations
 * 
 * Applies field-level normalization based on annotations
 * 
 * @author Architecture Team
 * @version 1.0
 */
@Component
public class RequestNormalizer {

    /**
     * Normalize a request object based on @Normalize annotations
     *
     * @param request Request to normalize
     * @param <T>     Request type
     * @return Normalized request
     */
    public <T> T normalize(T request) {
        if (request == null) {
            return null;
        }

        Class<?> clazz = request.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Normalize.class)) {
                normalizeField(request, field);
            }
        }

        return request;
    }

    private void normalizeField(Object object, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(object);

            if (value == null) {
                return;
            }

            Normalize annotation = field.getAnnotation(Normalize.class);
            Normalize.Strategy[] strategies = annotation.value();

            Object normalized = value;
            for (Normalize.Strategy strategy : strategies) {
                normalized = applyStrategy(normalized, strategy);
            }

            field.set(object, normalized);

        } catch (IllegalAccessException e) {
            // Log but don't fail
            System.err.println("Failed to normalize field: " + field.getName());
        }
    }

    private Object applyStrategy(Object value, Normalize.Strategy strategy) {
        if (!(value instanceof String)) {
            return value;
        }

        String str = (String) value;

        return switch (strategy) {
            case TRIM -> str.trim();
            case UPPERCASE -> str.toUpperCase();
            case LOWERCASE -> str.toLowerCase();
            case EMPTY_TO_NULL -> str.isEmpty() ? null : str;
            case REMOVE_WHITESPACE -> str.replaceAll("\\s+", "");
            case CAPITALIZE -> capitalize(str);
            case SLUG -> generateSlug(str);
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Arrays.stream(str.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(str);
    }

    private String generateSlug(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .trim();
    }

    /**
     * Normalize map values (for attributes)
     */
    public Map<String, String> normalizeMap(Map<String, String> map, 
                                             Normalize.Strategy keyStrategy,
                                             Normalize.Strategy valueStrategy) {
        if (map == null || map.isEmpty()) {
            return map;
        }

        Map<String, String> normalized = new java.util.HashMap<>();
        
        map.forEach((key, value) -> {
            String normalizedKey = (String) applyStrategy(key, keyStrategy);
            String normalizedValue = (String) applyStrategy(value, valueStrategy);
            normalized.put(normalizedKey, normalizedValue);
        });

        return normalized;
    }
}