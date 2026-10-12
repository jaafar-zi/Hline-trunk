package com.example.demo.common.validator;

import com.example.demo.common.validator.constraints.FieldMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Field match validator (e.g., password confirmation)
 *
 * @author Architecture Team
 * @version 1.0
 */
public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);

            Object firstValue = beanWrapper.getPropertyValue(firstFieldName);
            Object secondValue = beanWrapper.getPropertyValue(secondFieldName);

            boolean valid = (firstValue == null && secondValue == null) ||
                    (firstValue != null && firstValue.equals(secondValue));

            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(secondFieldName)
                        .addConstraintViolation();
            }

            return valid;
        } catch (Exception e) {
            return false;
        }
    }
}
