// RegisterCustomerRequestValidator.java
package com.example.demo.modules.customer.application.validator;

import com.example.demo.modules.customer.application.dto.request.RegisterCustomerRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Spring Validator for RegisterCustomerRequest
 *
 * Most validation is now declarative via annotations:
 * - @ValidEmail → format validation
 * - @UniqueField → email uniqueness (DB check)
 * - @ValidPhone → E.164 format
 * - @Pattern → name character validation
 *
 * This validator handles any remaining cross-field business rules
 * that cannot be expressed declaratively.
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Component
public class RegisterCustomerRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RegisterCustomerRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // All field-level validation is handled by annotations:
        // - @NotBlank, @Size, @Pattern → basic format
        // - @ValidEmail → RFC email validation
        // - @UniqueField → email uniqueness via DB
        // - @ValidPhone → E.164 format
        //
        // Add cross-field or complex business rules here if needed.
        // Currently no additional rules required.
    }
}