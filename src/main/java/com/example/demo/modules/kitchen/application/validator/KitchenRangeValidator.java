package com.example.demo.modules.kitchen.application.validator;

import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.common.validation.validator.Validator;
import com.example.demo.modules.kitchen.domain.model.KitchenRange;
import com.example.demo.modules.kitchen.domain.repository.KitchenRangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kitchen Range Domain Validator
 */
@Component
@RequiredArgsConstructor
public class KitchenRangeValidator implements Validator<KitchenRange> {

    private final KitchenRangeRepository rangeRepository;

    @Override
    public ValidationResult validate(KitchenRange range) {
        return validate(range, ValidationContext.create());
    }

    @Override
    public ValidationResult validate(KitchenRange range, ValidationContext context) {
        ValidationResult result = ValidationResult.builder()
            .context("KitchenRange Validation")
            .build();

        if (range == null) {
            result.addError("range", "Range cannot be null");
            return result;
        }

        validateBasic(range, result);

        if ("DEACTIVATE".equals(context.getOperation())) {
            validateForDeactivation(range, result);
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return KitchenRange.class.equals(clazz);
    }

    private void validateBasic(KitchenRange range, ValidationResult result) {
        if (range.getCode() == null || range.getCode().isBlank()) {
            result.addError("code", "Range code is required");
        }

        if (range.getName() == null || range.getName().isBlank()) {
            result.addError("name", "Range name is required");
        }

        // Check code uniqueness
        if (range.getCode() != null) {
            boolean codeExists = range.getId() == null
                ? rangeRepository.existsByCode(range.getCode())
                : rangeRepository.existsByCodeAndIdNot(range.getCode(), range.getId());

            if (codeExists) {
                result.addError("code",
                    "Range code already exists: " + range.getCode());
            }
        }

        // SEO warnings
        if (range.getMetaTitle() == null || range.getMetaTitle().isBlank()) {
            result.addWarning("metaTitle", "Range should have a meta title for SEO");
        }
        if (range.getHeroImageUrl() == null || range.getHeroImageUrl().isBlank()) {
            result.addWarning("heroImageUrl", "Range should have a hero image");
        }
    }

    private void validateForDeactivation(KitchenRange range, ValidationResult result) {
        long activeCollections = range.getFinishGroups().stream()
            .flatMap(fg -> fg.getCollections().stream())
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .count();

        if (activeCollections > 0) {
            result.addError("collections",
                "Cannot deactivate range with " + activeCollections
                    + " active collections. Deactivate collections first.");
        }
    }

    public ValidationResult validateCodeUniqueness(String code, UUID excludeId) {
        ValidationResult result = ValidationResult.success("Code Uniqueness Check");

        boolean exists = excludeId == null
            ? rangeRepository.existsByCode(code)
            : rangeRepository.existsByCodeAndIdNot(code, excludeId);

        if (exists) {
            result.addError("code", "Range code already exists: " + code);
        }

        return result;
    }
}