package com.example.demo.common.handler;

import com.example.demo.common.exception.ErrorCode;
import com.example.demo.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated handler for validation-related exceptions
 *
 * Handles:
 * - Bean validation (@Valid)
 * - Method parameter validation
 * - Constraint violations
 * - Custom validation exceptions
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ValidationExceptionHandler {

    private final ErrorResponseBuilder errorResponseBuilder;

    /**
     * Handle ValidationException (custom validation)
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        // log.warn("Validation exception: {} - Errors: {} - Path: {}",
        //         ex.getMessage(), ((BindException) ex).getFieldErrorCount(), request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.buildValidationError(
                ex.getMessage(),
                request
        );

        // Add field errors
        // if (ex.hasFieldErrors()) {
        //     response.setFieldErrors(ex.getFieldErrors());

        //     // Also add as ValidationError objects for more detail
        //     ex.getFieldErrors().forEach((field, message) ->
        //             response.addValidationError(
        //                     ValidationError.of(field, message)
        //             )
        //     );
        // }

        response.addSuggestion("Fix the validation errors and try again");
        response.addSuggestion("Check field formats and constraints");

        log.debug("Validation errors: {}", response.getFieldErrors());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle MethodArgumentNotValidException (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Method argument validation failed: {} errors - Path: {}",
                ex.getErrorCount(), request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.buildValidationError(
                "Validation failed",
                request
        );

        // Process field errors
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();
            String message = error.getDefaultMessage() != null ?
                    error.getDefaultMessage() : "Invalid value";

            fieldErrors.put(field, message);

            // Add detailed validation error
            response.addValidationError(
                    ValidationError.builder()
                            .field(field)
                            .rejectedValue(error.getRejectedValue())
                            .message(message)
                            .code(error.getCode())
                            .objectName(error.getObjectName())
                            .build()
            );
        }

        // Process global errors (non-field-specific)
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            response.addValidationError(
                    ValidationError.builder()
                            .message(error.getDefaultMessage())
                            .code(error.getCode())
                            .objectName(error.getObjectName())
                            .build()
            );
        }

        response.setFieldErrors(fieldErrors);
        response.setDetails(String.format("Validation failed for %d field(s)",
                fieldErrors.size()));

        response.addSuggestion("Review the field errors and correct your input");
        response.addSuggestion("Ensure all required fields are provided");

        log.debug("Validation errors: {}", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle ConstraintViolationException (Method-level validation)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.warn("Constraint violation: {} violations - Path: {}",
                ex.getConstraintViolations().size(), request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.buildValidationError(
                "Constraint violation",
                request
        );

        Map<String, String> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();

            fieldErrors.put(propertyPath, message);

            // Add detailed validation error
            response.addValidationError(
                    ValidationError.builder()
                            .field(propertyPath)
                            .rejectedValue(violation.getInvalidValue())
                            .message(message)
                            .constraint(violation.getConstraintDescriptor()
                                    .getAnnotation().annotationType().getSimpleName())
                            .build()
            );
        }

        response.setFieldErrors(fieldErrors);
        response.setDetails(String.format("Validation failed for %d constraint(s)",
                fieldErrors.size()));

        response.addSuggestion("Fix the constraint violations");
        response.addSuggestion("Check that values meet the required constraints");

        log.debug("Constraint violations: {}", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle MissingServletRequestParameterException
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("Missing request parameter: {} - Path: {}",
                ex.getParameterName(), request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.MISSING_PARAMETER,
                String.format("Missing required parameter: %s", ex.getParameterName()),
                String.format("Parameter '%s' of type '%s' is required",
                        ex.getParameterName(), ex.getParameterType()),
                request
        );

        // Add as field error
        response.addFieldError(ex.getParameterName(), "This parameter is required");

        // Add as validation error
        response.addValidationError(
                ValidationError.builder()
                        .field(ex.getParameterName())
                        .message("This parameter is required")
                        .constraint("Required")
                        .build()
        );

        response.addSuggestion(
                String.format("Provide the '%s' parameter in your request",
                        ex.getParameterName())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle IllegalArgumentException (often from validation)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument: {} - Path: {}",
                ex.getMessage(), request.getRequestURI());

        ApiErrorResponse response = errorResponseBuilder.build(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.BAD_REQUEST,
                "Invalid argument",
                ex.getMessage(),
                request
        );

        response.addSuggestion("Check your request parameters");
        response.addSuggestion("Ensure all values are within valid ranges");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}