package com.example.demo.modules.kitchen.application.validator;

import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.common.validation.validator.Validator;
import com.example.demo.modules.kitchen.domain.model.KitchenProduct;
import com.example.demo.modules.kitchen.domain.model.KitchenProductVariant;
import com.example.demo.modules.kitchen.domain.repository.KitchenProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Kitchen Product Domain Validator
 */
@Component
@RequiredArgsConstructor
public class KitchenProductValidator implements Validator<KitchenProduct> {

    private final KitchenProductRepository productRepository;

    @Override
    public ValidationResult validate(KitchenProduct product) {
        return validate(product, ValidationContext.create());
    }

    @Override
    public ValidationResult validate(KitchenProduct product, ValidationContext context) {
        ValidationResult result = ValidationResult.builder()
            .context("KitchenProduct Validation")
            .build();

        if (product == null) {
            result.addError("product", "Product cannot be null");
            return result;
        }

        validateBasic(product, result);

        if ("ACTIVATE".equals(context.getOperation())) {
            validateForActivation(product, result);
        }

        if ("VARIANT".equals(context.getOperation())) {
            validateVariantConsistency(product, result);
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return KitchenProduct.class.equals(clazz);
    }

    private void validateBasic(KitchenProduct product, ValidationResult result) {
        if (product.getSku() == null || product.getSku().isBlank()) {
            result.addError("sku", "Product SKU is required");
        }

        if (product.getName() == null || product.getName().isBlank()) {
            result.addError("name", "Product name is required");
        }

        if (product.getCollection() == null) {
            result.addError("collection", "Product must belong to a collection");
        }

        if (product.getUnitCategory() == null) {
            result.addError("unitCategory", "Product must have a unit category");
        }

        if (product.getBasePrice() == null) {
            result.addError("basePrice", "Product must have a base price");
        }

        // Check SKU uniqueness
        if (product.getSku() != null) {
            boolean skuExists = product.getId() == null
                ? productRepository.existsBySku(product.getSku())
                : productRepository.existsBySkuAndIdNot(
                    product.getSku(), product.getId());

            if (skuExists) {
                result.addError("sku",
                    "Product SKU already exists: " + product.getSku());
            }
        }

        // Dimension warnings
        if (product.getWidthMm() == null || product.getHeightMm() == null
                || product.getDepthMm() == null) {
            result.addWarning("dimensions",
                "Product should have dimensions specified");
        }

        if (product.getShortDescription() == null
                || product.getShortDescription().isBlank()) {
            result.addWarning("shortDescription",
                "Product should have a short description");
        }
    }

    private void validateForActivation(KitchenProduct product, ValidationResult result) {
        if (product.getCollection() != null
                && !Boolean.TRUE.equals(product.getCollection().getActive())) {
            result.addError("collection",
                "Cannot activate product when collection is inactive");
        }

        if (!product.hasVariants()) {
            result.addError("variants",
                "Product must have at least one variant before activation");
        }

        if (product.getImages() == null || product.getImages().isEmpty()) {
            result.addWarning("images",
                "Product should have at least one image before activation");
        }
    }

    private void validateVariantConsistency(KitchenProduct product,
                                             ValidationResult result) {
        if (!product.hasVariants()) return;

        // Check for default variant
        long defaultCount = product.getVariants().stream()
            .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
            .count();

        if (defaultCount == 0) {
            result.addWarning("variants",
                "Product has variants but no default variant is set");
        } else if (defaultCount > 1) {
            result.addError("variants",
                "Product has multiple default variants");
        }

        // Check SKU uniqueness within variants
        long uniqueSkus = product.getVariants().stream()
            .map(KitchenProductVariant::getSku)
            .distinct()
            .count();

        if (uniqueSkus < product.getVariants().size()) {
            result.addError("variants",
                "Product variants must have unique SKUs");
        }

        // Check for duplicate colour+assembly combinations
        long uniqueCombinations = product.getVariants().stream()
            .map(v -> v.getCabinetColour().getId() + "-" + v.getAssemblyType().name())
            .distinct()
            .count();

        if (uniqueCombinations < product.getVariants().size()) {
            result.addError("variants",
                "Duplicate colour + assembly type combinations found in variants");
        }
    }
}