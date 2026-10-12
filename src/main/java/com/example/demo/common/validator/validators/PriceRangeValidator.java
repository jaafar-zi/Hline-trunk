package com.example.demo.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.example.demo.common.validator.constraints.ValidPriceRange;

/**
 * Price range validator implementation
 * 
 * @author Architecture Team
 * @version 1.0
 */
public class PriceRangeValidator implements ConstraintValidator<ValidPriceRange, Object> {

    private String minFieldName;
    private String maxFieldName;
    private boolean allowNull;

    @Override
    public void initialize(ValidPriceRange annotation) {
        this.minFieldName = annotation.min();
        this.maxFieldName = annotation.max();
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            BigDecimal minPrice = getFieldValue(value, minFieldName);
            BigDecimal maxPrice = getFieldValue(value, maxFieldName);

            // Both null - valid if allowed
            if (minPrice == null && maxPrice == null) {
                return allowNull;
            }

            // One null - always valid
            if (minPrice == null || maxPrice == null) {
                return true;
            }

            // Compare prices
            if (minPrice.compareTo(maxPrice) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Minimum price cannot be greater than maximum price"
                ).addPropertyNode(minFieldName).addConstraintViolation();
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private BigDecimal getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (BigDecimal) field.get(object);
    }
}