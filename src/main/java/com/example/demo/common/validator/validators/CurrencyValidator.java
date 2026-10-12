package com.example.demo.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.common.validator.constraints.ValidCurrency;

/**
 * Currency validator implementation
 * 
 * @author Architecture Team
 * @version 1.0
 */
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> VALID_CURRENCIES = Currency.getAvailableCurrencies()
            .stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toSet());

    private boolean allowNull;

    @Override
    public void initialize(ValidCurrency annotation) {
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (value.isBlank()) {
            return false;
        }

        String currency = value.trim().toUpperCase();

        if (currency.length() != 3) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Currency code must be exactly 3 characters (ISO 4217)"
            ).addConstraintViolation();
            return false;
        }

        if (!VALID_CURRENCIES.contains(currency)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("'%s' is not a valid ISO 4217 currency code", currency)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}