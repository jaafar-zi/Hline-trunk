package com.example.demo.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.example.demo.common.validator.constraints.ConditionalRequired;

/**
 * Conditional required validator implementation
 * 
 * @author Architecture Team
 * @version 1.0
 */
public class ConditionalRequiredValidator implements ConstraintValidator<ConditionalRequired, Object> {

    private String fieldName;
    private String dependsOnFieldName;
    private String[] dependsOnValues;
    private boolean negate;

    @Override
    public void initialize(ConditionalRequired annotation) {
        this.fieldName = annotation.field();
        this.dependsOnFieldName = annotation.dependsOn();
        this.dependsOnValues = annotation.dependsOnValue();
        this.negate = annotation.negate();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Object dependsOnValue = getFieldValue(value, dependsOnFieldName);
            Object fieldValue = getFieldValue(value, fieldName);

            // Check if condition is met
            boolean conditionMet = checkCondition(dependsOnValue);

            // If condition is met, field must not be null
            if (conditionMet && (fieldValue == null || isBlank(fieldValue))) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("%s is required when %s is %s",
                                fieldName,
                                dependsOnFieldName,
                                String.join(" or ", dependsOnValues))
                ).addPropertyNode(fieldName).addConstraintViolation();
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private boolean checkCondition(Object value) {
        if (value == null) {
            return negate;
        }

        String stringValue = value.toString();
        boolean matches = Arrays.asList(dependsOnValues).contains(stringValue);
        
        return negate ? !matches : matches;
    }

    private boolean isBlank(Object value) {
        if (value instanceof String) {
            return ((String) value).isBlank();
        }
        return false;
    }
}