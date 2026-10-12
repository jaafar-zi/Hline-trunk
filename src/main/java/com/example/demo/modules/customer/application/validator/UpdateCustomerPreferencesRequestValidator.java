// UpdateCustomerPreferencesRequestValidator.java
package com.example.demo.modules.customer.application.validator;

import com.example.demo.modules.customer.application.dto.request.UpdateCustomerPreferencesRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Currency;
import java.util.Locale;

/**
 * Spring Validator for UpdateCustomerPreferencesRequest
 *
 * Annotations handle format (@Pattern for ISO codes).
 * This validator performs semantic validation:
 * - Is the currency code a real ISO 4217 currency?
 * - Is the language code a real ISO 639-1 language?
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Component
public class UpdateCustomerPreferencesRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateCustomerPreferencesRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateCustomerPreferencesRequest request =
                (UpdateCustomerPreferencesRequest) target;

        validateCurrency(request.getCurrency(), errors);
        validateLanguage(request.getLanguage(), errors);
    }

    private void validateCurrency(String currency, Errors errors) {
        if (currency == null || currency.isBlank()) {
            return; // @NotBlank handles this
        }

        try {
            Currency.getInstance(currency.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.rejectValue(
                    "currency",
                    "preferences.currency.invalid",
                    "Currency code is not a recognized ISO 4217 currency: " + currency
            );
        }
    }

    private void validateLanguage(String language, Errors errors) {
        if (language == null || language.isBlank()) {
            return; 
        }

        Locale locale = Locale.forLanguageTag(language.trim());
        if (locale.getLanguage().isEmpty()) {
            errors.rejectValue(
                    "language",
                    "preferences.language.invalid",
                    "Language code is not a recognized ISO 639-1 language: " + language
            );
        }
    }
}