package com.example.demo.modules.kitchen.application.validator;

import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.common.validation.validator.Validator;
import com.example.demo.modules.kitchen.domain.model.KitchenConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Kitchen Configuration Domain Validator
 */
@Component
@RequiredArgsConstructor
public class KitchenConfigurationValidator implements Validator<KitchenConfiguration> {

    @Override
    public ValidationResult validate(KitchenConfiguration configuration) {
        return validate(configuration, ValidationContext.create());
    }

    @Override
    public ValidationResult validate(KitchenConfiguration configuration,
                                      ValidationContext context) {
        ValidationResult result = ValidationResult.builder()
            .context("KitchenConfiguration Validation")
            .build();

        if (configuration == null) {
            result.addError("configuration", "Configuration cannot be null");
            return result;
        }

        validateBasic(configuration, result);

        if ("QUOTE".equals(context.getOperation())) {
            validateForQuoting(configuration, result);
        }

        if ("ORDER".equals(context.getOperation())) {
            validateForOrdering(configuration, result);
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return KitchenConfiguration.class.equals(clazz);
    }

    private void validateBasic(KitchenConfiguration configuration,
                                ValidationResult result) {
        // Must have either customer or session
        if (configuration.getCustomerId() == null
                && (configuration.getSessionId() == null
                    || configuration.getSessionId().isBlank())) {
            result.addError("user",
                "Configuration must be associated with a customer or session");
        }

        if (configuration.getCollection() == null) {
            result.addError("collection", "Configuration must have a collection");
        }

        if (configuration.getSelectedCabinetColour() == null) {
            result.addWarning("selectedCabinetColour",
                "Configuration should have a cabinet colour selected");
        }
    }

    private void validateForQuoting(KitchenConfiguration configuration,
                                     ValidationResult result) {
        if (configuration.getItems() == null || configuration.getItems().isEmpty()) {
            result.addError("items",
                "Cannot generate quote for empty configuration");
        }

        // Check all items have valid variants
        if (configuration.getItems() != null) {
            configuration.getItems().forEach(item -> {
                if (item.getVariant() == null) {
                    result.addError("items",
                        "Item " + item.getPosition() + " has no variant selected");
                } else if (!item.getVariant().isAvailable()) {
                    result.addWarning("items",
                        "Item " + item.getPosition() + " ("
                            + item.getProductName() + ") is not currently available");
                }
            });
        }
    }

    private void validateForOrdering(KitchenConfiguration configuration,
                                      ValidationResult result) {
        validateForQuoting(configuration, result);

        if (configuration.getItems() != null) {
            configuration.getItems().forEach(item -> {
                if (item.getVariant() != null
                        && !item.getVariant().isSellable()) {
                    result.addError("items",
                        "Item " + item.getPosition() + " ("
                            + item.getProductName()
                            + ") cannot be ordered - product is inactive");
                }
            });
        }
    }

    public ValidationResult validateColourChange(
            KitchenConfiguration configuration,
            java.util.UUID newColourId) {

        ValidationResult result = ValidationResult.success("Colour Change Validation");

        if (configuration.getCollection() == null) {
            result.addError("collection", "Configuration has no collection");
            return result;
        }

        boolean colourAvailable = configuration.getCollection()
            .getAvailableCabinetColours().stream()
            .anyMatch(c -> c.getId().equals(newColourId));

        if (!colourAvailable) {
            result.addError("colour",
                "Selected colour is not available for this collection");
        }

        return result;
    }
}