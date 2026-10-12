package com.example.demo.modules.kitchen.application.validator;

import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.common.validation.validator.Validator;
import com.example.demo.modules.kitchen.domain.model.KitchenCollection;
import com.example.demo.modules.kitchen.domain.repository.KitchenCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Kitchen Collection Domain Validator
 */
@Component
@RequiredArgsConstructor
public class KitchenCollectionValidator implements Validator<KitchenCollection> {

    private final KitchenCollectionRepository collectionRepository;

    @Override
    public ValidationResult validate(KitchenCollection collection) {
        return validate(collection, ValidationContext.create());
    }

    @Override
    public ValidationResult validate(KitchenCollection collection,
                                      ValidationContext context) {
        ValidationResult result = ValidationResult.builder()
            .context("KitchenCollection Validation")
            .build();

        if (collection == null) {
            result.addError("collection", "Collection cannot be null");
            return result;
        }

        validateBasic(collection, result);

        if ("ACTIVATE".equals(context.getOperation())) {
            validateForActivation(collection, result);
        }

        if ("DEACTIVATE".equals(context.getOperation())) {
            validateForDeactivation(collection, result);
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return KitchenCollection.class.equals(clazz);
    }

    private void validateBasic(KitchenCollection collection, ValidationResult result) {
        if (collection.getName() == null || collection.getName().isBlank()) {
            result.addError("name", "Collection name is required");
        }

        if (collection.getFinishGroup() == null) {
            result.addError("finishGroup", "Finish group is required");
        }

        if (collection.getColour() == null) {
            result.addError("colour", "Door colour is required");
        }

        // Check slug uniqueness
        if (collection.getSlug() != null) {
            boolean slugExists = collection.getId() == null
                ? collectionRepository.existsBySlug(collection.getSlug())
                : collectionRepository.existsBySlugAndIdNot(
                    collection.getSlug(), collection.getId());

            if (slugExists) {
                result.addError("slug",
                    "Collection name already taken (slug conflict): "
                        + collection.getSlug());
            }
        }

        // Warnings
        if (collection.getHeroImageUrl() == null
                || collection.getHeroImageUrl().isBlank()) {
            result.addWarning("heroImageUrl",
                "Collection should have a hero image");
        }

        if (collection.getDefaultCabinetColour() == null) {
            result.addWarning("defaultCabinetColour",
                "Collection should have a default cabinet colour set");
        }

        if (collection.getLeadTimeDays() == null) {
            result.addWarning("leadTimeDays",
                "Collection should have a lead time specified");
        }
    }

    private void validateForActivation(KitchenCollection collection,
                                        ValidationResult result) {
        if (collection.getFinishGroup() != null
                && !Boolean.TRUE.equals(collection.getFinishGroup().getActive())) {
            result.addError("finishGroup",
                "Cannot activate collection when finish group is inactive");
        }

        if (collection.getDefaultCabinetColour() == null) {
            result.addError("defaultCabinetColour",
                "Collection must have a default cabinet colour before activation");
        }
    }

    private void validateForDeactivation(KitchenCollection collection,
                                          ValidationResult result) {
        long activeProducts = collection.getProducts().stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .count();

        if (activeProducts > 0) {
            result.addError("products",
                "Cannot deactivate collection with " + activeProducts
                    + " active products. Deactivate products first.");
        }
    }
}