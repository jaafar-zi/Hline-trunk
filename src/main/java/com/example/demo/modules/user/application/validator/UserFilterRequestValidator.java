package com.example.demo.modules.user.application.validator;

import com.example.demo.common.validator.CustomValidators;
import com.example.demo.modules.user.application.dto.filter.UserFilterRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Spring Validator for UserFilterRequest
 *
 * @author Architecture Team
 * @version 1.0
 */
@Component
public class UserFilterRequestValidator implements Validator {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String[] VALID_SORT_FIELDS = {
            "id", "firstName", "lastName", "email", "createdAt",
            "updatedAt", "lastLoginAt", "enabled", "accountLocked"
    };

    @Override
    public boolean supports(Class<?> clazz) {
        return UserFilterRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserFilterRequest request = (UserFilterRequest) target;

        // Validate pagination
        validatePagination(request, errors);

        // Validate sort field
        validateSortField(request.getSortBy(), errors);

        // Validate date ranges
        validateDateRanges(request, errors);

        // Validate UUID format for userGroupId
        if (request.getUserGroupId() != null && !request.getUserGroupId().isBlank()) {
            if (!CustomValidators.isValidUuid(request.getUserGroupId())) {
                errors.rejectValue("userGroupId", "userGroupId.invalidUUID",
                        "Invalid UUID format for user group ID");
            }
        }

        // SQL injection check on keyword
        if (request.getKeyword() != null && !CustomValidators.isSqlInjectionFree(request.getKeyword())) {
            errors.rejectValue("keyword", "keyword.sqlInjection",
                    "Invalid characters detected in search keyword");
        }
    }

    private void validatePagination(UserFilterRequest request, Errors errors) {
        if (request.getPage() != null && request.getPage() < 0) {
            errors.rejectValue("page", "page.negative",
                    "Page number cannot be negative");
        }

        if (request.getSize() != null) {
            if (request.getSize() < 1) {
                errors.rejectValue("size", "size.tooSmall",
                        "Page size must be at least 1");
            } else if (request.getSize() > MAX_PAGE_SIZE) {
                errors.rejectValue("size", "size.tooLarge",
                        String.format("Page size cannot exceed %d", MAX_PAGE_SIZE));
            }
        }
    }

    private void validateSortField(String sortBy, Errors errors) {
        if (sortBy != null && !sortBy.isBlank()) {
            boolean valid = java.util.Arrays.asList(VALID_SORT_FIELDS).contains(sortBy);
            if (!valid) {
                errors.rejectValue("sortBy", "sortBy.invalid",
                        "Invalid sort field. Valid fields: " + String.join(", ", VALID_SORT_FIELDS));
            }
        }
    }

    private void validateDateRanges(UserFilterRequest request, Errors errors) {
        if (request.getCreatedAfter() != null && request.getCreatedBefore() != null) {
            if (request.getCreatedAfter().isAfter(request.getCreatedBefore())) {
                errors.rejectValue("createdAfter", "dateRange.invalid",
                        "createdAfter must be before createdBefore");
            }
        }
    }
}
