package com.example.demo.common.base.controller;

import com.example.demo.common.validation.result.ValidationException;
import com.example.demo.common.validation.result.ValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

/**
 * Base controller with validation support
 * 
 * @author Architecture Team
 * @version 3.0
 */
@Slf4j
public abstract class BaseController {

    @Autowired
    protected Validator validator;

    /**
     * Validate request with validation groups
     */
    protected <T> void validateRequest(T request, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(request, groups);

        if (!violations.isEmpty()) {
            ValidationResult result = ValidationResult.builder()
                    .valid(false)
                    .context("Request Validation")
                    .build();

            violations.forEach(violation -> {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                Object invalidValue = violation.getInvalidValue();
                
                result.addError(field, message, invalidValue);
            });

            throw new ValidationException(result);
        }
    }

    /**
     * Validate and normalize request
     */
    protected <T extends com.example.demo.common.base.dto.BaseRequest> void validateAndNormalize(T request, Class<?>... groups) {
        // Pre-process
        request.preProcess();

        // Normalize
        request.normalize();

        // Validate
        validateRequest(request, groups);

        // Post-process
        request.postProcess();
    }

    /**
     * Create success response
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Create success response with message
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Create created response
     */
    protected <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Resource created successfully"));
    }

    /**
     * Create no content response
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Create error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message));
    }
}