package com.example.demo.modules.product.application.validator;

import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.common.validation.validator.Validator;
import com.example.demo.modules.product.domain.model.Product;
import com.example.demo.modules.product.domain.model.ProductAttribute;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Product domain validator
 * 
 * Validates business rules that cannot be expressed as simple annotations
 * 
 * @author PIM Team
 * @version 1.0
 */
@Component
public class ProductValidator implements Validator<Product> {

    @Override
    public ValidationResult validate(Product product) {
        return validate(product, ValidationContext.create());
    }

    @Override
    public ValidationResult validate(Product product, ValidationContext context) {
        ValidationResult result = ValidationResult.builder()
                .context("Product Validation")
                .build();

        if (product == null) {
            result.addError("product", "Product cannot be null");
            return result;
        }

        // Validate based on context
        if ("PUBLISH".equals(context.getOperation())) {
            validateForPublishing(product, result);
        } else {
            validateBasic(product, result);
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Product.class.equals(clazz);
    }

    /**
     * Basic validation (for draft/update)
     */
    private void validateBasic(Product product, ValidationResult result) {
        // SKU validation
        if (product.getSku() == null) {
            result.addError("sku", "Product SKU is required");
        }

        // Name validation
        if (product.getName() == null || product.getName().isBlank()) {
            result.addError("name", "Product name is required");
        }

        // Category validation
        if (product.getCategory() == null) {
            result.addError("category", "Product category is required");
        }
    }

    /**
     * Publishing validation (stricter rules)
     */
    private void validateForPublishing(Product product, ValidationResult result) {
        // First run basic validation
        validateBasic(product, result);

        // Price validation
        if (!product.hasValidPrice()) {
            result.addError("prices", "Product must have at least one valid price to be published");
        }

        // Image validation
        if (!product.hasImages()) {
            result.addError("images", "Product must have at least one image to be published");
        } else if (product.getPrimaryImage() == null) {
            result.addWarning("images", "Product should have a primary image");
        }

        // Required attributes validation
        if (!product.hasAllRequiredAttributes()) {
            Set<String> missingAttributes = product.getRequiredAttributes().stream()
                    .filter(attr -> attr.getValue() == null || attr.getValue().isBlank())
                    .map(ProductAttribute::getKey)
                    .collect(Collectors.toSet());

            result.addError("attributes",
                    "Missing required attributes: " + String.join(", ", missingAttributes));
        }

        // Description validation (warning)
        if (product.getShortDescription() == null || product.getShortDescription().isBlank()) {
            result.addWarning("shortDescription",
                    "Product should have a short description for better SEO");
        }

        if (product.getLongDescription() == null || product.getLongDescription().isBlank()) {
            result.addWarning("longDescription",
                    "Product should have a detailed description");
        }

        // SEO validation (warnings)
        if (product.getMetaTitle() == null || product.getMetaTitle().isBlank()) {
            result.addWarning("metaTitle",
                    "Product should have a meta title for SEO");
        }

        if (product.getMetaDescription() == null || product.getMetaDescription().isBlank()) {
            result.addWarning("metaDescription",
                    "Product should have a meta description for SEO");
        }

        // Brand validation (warning)
        if (product.getBrand() == null || product.getBrand().isBlank()) {
            result.addWarning("brand",
                    "Product should have a brand for better categorization");
        }

        // Inventory validation (warning)
        if (product.getInventory() != null && !product.isInStock()) {
            result.addWarning("inventory",
                    "Product is being published but is currently out of stock");
        }
    }

    /**
     * Validate price consistency
     */
    public ValidationResult validatePriceConsistency(Product product) {
        ValidationResult result = ValidationResult.success("Price Consistency Check");

        if (product.getPrices() == null || product.getPrices().isEmpty()) {
            return result;
        }

        // Check for conflicting currencies
        long currencyCount = product.getPrices().stream()
                .map(price -> price.getPrice().getCurrency())
                .distinct()
                .count();

        if (currencyCount > 1) {
            result.addWarning("prices",
                    "Product has prices in multiple currencies - this may cause display issues");
        }

        // Check for overlapping promotional prices
        long activePromotionalPrices = product.getPrices().stream()
                .filter(p -> "PROMOTIONAL".equals(p.getPriceType().name()))
                .filter(p -> p.isActive())
                .count();

        if (activePromotionalPrices > 1) {
            result.addWarning("prices",
                    "Product has multiple active promotional prices");
        }

        return result;
    }

    /**
     * Validate variant consistency
     */
    public ValidationResult validateVariantConsistency(Product product) {
        ValidationResult result = ValidationResult.success("Variant Consistency Check");

        if (!product.hasVariants()) {
            return result;
        }

        // Check for default variant
        long defaultVariantCount = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
                .count();

        if (defaultVariantCount == 0) {
            result.addWarning("variants",
                    "Product has variants but no default variant is set");
        } else if (defaultVariantCount > 1) {
            result.addError("variants",
                    "Product has multiple default variants - only one should be marked as default");
        }

        // Check for SKU uniqueness within variants
        long uniqueSkus = product.getVariants().stream()
                .map(v -> v.getSku().getValue())
                .distinct()
                .count();

        if (uniqueSkus < product.getVariants().size()) {
            result.addError("variants",
                    "Product variants must have unique SKUs");
        }

        return result;
    }
}