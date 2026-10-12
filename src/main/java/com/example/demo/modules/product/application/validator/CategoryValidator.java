package com.example.demo.modules.product.application.validator;

import com.example.demo.common.validation.result.ValidationResult;
import com.example.demo.common.validation.validator.ValidationContext;
import com.example.demo.common.validation.validator.Validator;
import com.example.demo.modules.product.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Category domain validator
 * 
 * @author PIM Team
 * @version 1.0
 */
@Component
public class CategoryValidator implements Validator<Category> {

    @Override
    public ValidationResult validate(Category category) {
        return validate(category, ValidationContext.create());
    }

    @Override
    public ValidationResult validate(Category category, ValidationContext context) {
        ValidationResult result = ValidationResult.builder()
                .context("Category Validation")
                .build();

        if (category == null) {
            result.addError("category", "Category cannot be null");
            return result;
        }

        // Basic validation
        if (category.getName() == null || category.getName().isBlank()) {
            result.addError("name", "Category name is required");
        }

        if (category.getCode() == null || category.getCode().isBlank()) {
            result.addError("code", "Category code is required");
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Category.class.equals(clazz);
    }

    /**
     * Validate category hierarchy (no circular references)
     */
    public ValidationResult validateHierarchy(Category category) {
        ValidationResult result = ValidationResult.success("Category Hierarchy Check");

        if (category.getParent() == null) {
            return result; // Root category - no hierarchy issues
        }

        // Check for circular reference
        Set<Long> visitedIds = new HashSet<>();
        Category current = category;

        while (current != null) {
            if (current.getId() != null && !visitedIds.add( Long.valueOf(current.getId().toString()))) {
                result.addError("parent",
                        "Circular reference detected in category hierarchy");
                break;
            }
            current = current.getParent();
        }

        // Check depth
        int depth = visitedIds.size();
        if (depth > 10) {
            result.addWarning("parent",
                    String.format("Category hierarchy is very deep (%d levels) - consider flattening", depth));
        }

        return result;
    }

    /**
     * Validate that category can be deleted
     */
    public ValidationResult validateDeletion(Category category, boolean hasProducts, boolean hasChildren) {
        ValidationResult result = ValidationResult.success("Category Deletion Check");

        if (hasProducts) {
            result.addError("category",
                    "Cannot delete category - it has associated products");
        }

        if (hasChildren) {
            result.addError("category",
                    "Cannot delete category - it has child categories");
        }

        return result;
    }
}