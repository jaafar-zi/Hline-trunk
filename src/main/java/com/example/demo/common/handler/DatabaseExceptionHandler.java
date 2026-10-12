package com.example.demo.common.handler;

import com.example.demo.common.exception.ConcurrentModificationException;
import com.example.demo.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Dedicated handler for database-related exceptions
 *
 * Handles:
 * - Data integrity violations
 * - Optimistic locking failures
 * - Database access errors
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class DatabaseExceptionHandler {

    private final ErrorResponseBuilder errorResponseBuilder;

    /**
     * Handle DataIntegrityViolationException
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("Data integrity violation - Path: {}", request.getRequestURI(), ex);

        String message = "Data integrity violation";
        String details = "The operation violates database constraints";
        String errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;

        // Provide more specific messages based on exception details
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") ||
                    ex.getMessage().contains("duplicate key")) {
                message = "Duplicate entry";
                details = "A record with this value already exists";
                errorCode = ErrorCode.DUPLICATE_ENTITY;
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Cannot delete record";
                details = "This record is referenced by other records and cannot be deleted";
            } else if (ex.getMessage().contains("not-null constraint")) {
                message = "Required field missing";
                details = "One or more required fields are missing";
            }
        }

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.CONFLICT.value(),
                errorCode,
                message,
                details,
                request
        );

        response.addSuggestion("Check for duplicate values in unique fields");
        response.addSuggestion("Ensure all required fields are provided");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    /**
     * Handle ConcurrentModificationException
     */
    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ApiErrorResponse> handleConcurrentModification(
            ConcurrentModificationException ex,
            HttpServletRequest request) {

        log.warn("Concurrent modification - Path: {}", request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.CONFLICT.value(),
                ErrorCode.CONCURRENT_MODIFICATION,
                ex.getMessage(),
                ex.getDetails(),
                request
        );

        response.addSuggestion("Refresh the data and try again");
        response.addSuggestion("Check if another user has modified this record");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    /**
     * Handle ObjectOptimisticLockingFailureException
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {

        log.warn("Optimistic locking failure - Path: {}", request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.CONFLICT.value(),
                ErrorCode.CONCURRENT_MODIFICATION,
                "The record was modified by another user",
                "Please refresh the page and try again",
                request
        );

        response.addSuggestion("Refresh your data to get the latest version");
        response.addSuggestion("Apply your changes to the updated record");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    /**
     * Handle EmptyResultDataAccessException
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleEmptyResultDataAccess(
            EmptyResultDataAccessException ex,
            HttpServletRequest request) {

        log.warn("Empty result data access - Path: {}", request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.NOT_FOUND.value(),
                ErrorCode.ENTITY_NOT_FOUND,
                "Resource not found",
                "The requested resource does not exist or has been deleted",
                request
        );

        response.addSuggestion("Verify the resource ID");
        response.addSuggestion("Check if the resource was deleted");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}