package com.example.demo.modules.customer.application.validator;

import com.example.demo.modules.customer.application.dto.request.CustomerFilterRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

/**
 * Spring Validator for CustomerFilterRequest
 *
 * Handles cross-field and semantic validation for search filters.
 * Basic field formats handled by annotations on the DTO.
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Component
public class CustomerFilterRequestValidator implements Validator {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "firstName", "lastName", "email", "status", "createdAt", "updatedAt"
    );

    @Override
    public boolean supports(Class<?> clazz) {
        return CustomerFilterRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CustomerFilterRequest filter = (CustomerFilterRequest) target;

        validateDateRange(filter, errors);
        validatePageSize(filter, errors);
        validateSortField(filter, errors);
        validateSortDirection(filter, errors);
    }

    private void validateDateRange(CustomerFilterRequest filter, Errors errors) {
        if (filter.getCreatedAfter() != null
                && filter.getCreatedBefore() != null
                && filter.getCreatedAfter().isAfter(filter.getCreatedBefore())) {
            errors.rejectValue(
                    "createdAfter",
                    "filter.dateRange.invalid",
                    "'createdAfter' must be before 'createdBefore'"
            );
        }
    }

    private void validatePageSize(CustomerFilterRequest filter, Errors errors) {
        if (filter.getSize() != null && filter.getSize() > MAX_PAGE_SIZE) {
            errors.rejectValue(
                    "size",
                    "filter.pageSize.exceeded",
                    "Page size cannot exceed " + MAX_PAGE_SIZE
            );
        }
    }

    private void validateSortField(CustomerFilterRequest filter, Errors errors) {
        if (filter.getSortBy() != null
                && !filter.getSortBy().isBlank()
                && !VALID_SORT_FIELDS.contains(filter.getSortBy())) {
            errors.rejectValue(
                    "sortBy",
                    "filter.sortField.invalid",
                    "Invalid sort field: '" + filter.getSortBy()
                    + "'. Valid fields: " + VALID_SORT_FIELDS
            );
        }
    }

    private void validateSortDirection(CustomerFilterRequest filter, Errors errors) {
        if (filter.getSortDirection() != null
                && !filter.getSortDirection().isBlank()
                && !filter.getSortDirection().equalsIgnoreCase("ASC")
                && !filter.getSortDirection().equalsIgnoreCase("DESC")) {
            errors.rejectValue(
                    "sortDirection",
                    "filter.sortDirection.invalid",
                    "Sort direction must be 'ASC' or 'DESC'"
            );
        }
    }
}