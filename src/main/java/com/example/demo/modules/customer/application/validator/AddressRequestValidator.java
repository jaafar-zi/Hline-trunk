// AddressRequestValidator.java
package com.example.demo.modules.customer.application.validator;

import com.example.demo.modules.customer.application.dto.request.AddressRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Spring Validator for AddressRequest
 *
 * Most validation is declarative:
 * - @NotBlank → required fields
 * - @Size → length limits
 * - @Pattern(^[A-Z]{2}$) → ISO country code format
 * - @ValidPhone → phone format
 * - @NotNull → address type
 *
 * This validator handles cross-field rules only.
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Component
public class AddressRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return AddressRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // All field-level validation handled by annotations.
        // Add cross-field rules here if needed, e.g.:
        // - Certain countries requiring state/province
        // - Postal code format per country
    }
}