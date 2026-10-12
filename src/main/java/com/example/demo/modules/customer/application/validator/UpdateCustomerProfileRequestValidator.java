package com.example.demo.modules.customer.application.validator;

import com.example.demo.modules.customer.application.dto.request.UpdateCustomerProfileRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Spring Validator for UpdateCustomerProfileRequest
 *
 * All validation is declarative:
 * - @NotBlank, @Size, @Pattern → name fields
 * - @ValidPhone → phone format
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Component
public class UpdateCustomerProfileRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateCustomerProfileRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // All field-level validation handled by annotations.
        // Add cross-field rules here if needed in the future.
    }
}