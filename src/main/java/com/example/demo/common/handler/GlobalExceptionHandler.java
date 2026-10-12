package com.example.demo.common.handler;

import com.example.demo.common.base.controller.ApiResponse;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.validation.result.ValidationException;
import com.example.demo.common.validation.result.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler.
 *
 * Handler resolution order (most-specific wins):
 *
 *  1.  NoResourceFoundException        → 404  (static resource not found)
 *  2.  ValidationException             → 400  (custom domain validation)
 *  3.  MethodArgumentNotValidException → 400  (Spring @Valid / @Validated)
 *  4.  ConstraintViolationException    → 400  (method-level constraints)
 *  5.  EntityNotFoundException         → 404  (domain entity not found)
 *  6.  IllegalArgumentException        → 400  (bad caller argument)
 *  7.  IllegalStateException           → 409  (invalid state transition)
 *  8.  AccessDeniedException           → 403  (Spring Security)
 *  9.  HttpMessageNotReadableException → 400  (malformed JSON)
 *  10. MethodArgumentTypeMismatchException → 400 (wrong path/query type)
 *  11. RuntimeException                → 500  (unhandled runtime errors)
 *  12. Exception                       → 500  (absolute catch-all)
 *
 * IMPORTANT — handler ordering rules:
 *  - Spring picks the handler whose declared exception type is the
 *    most specific (closest ancestor) to the thrown exception.
 *  - When two handlers match at the same specificity level within
 *    the same @ControllerAdvice, declaration order in the source
 *    file is used as a tiebreaker.
 *  - RuntimeException is declared BEFORE Exception to ensure it is
 *    preferred over the catch-all for runtime errors.
 *  - NoResourceFoundException is declared FIRST to guarantee it is
 *    never swallowed by the generic handlers and always returns 404.
 *
 * @author Architecture Team
 * @version 4.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 1. NoResourceFoundException — 404
    // =========================================================================

    /**
     * Handles Spring MVC's NoResourceFoundException.
     *
     * Root cause in this project:
     *   The actuator management port (8081) is separate from the application
     *   port (8080). When Prometheus scrapes /actuator/prometheus on port 8080
     *   the DispatcherServlet finds no registered handler and delegates to the
     *   static ResourceHttpRequestHandler, which throws this exception.
     *
     * Fix:
     *   Either point the Prometheus scraper at port 8081, or remove
     *   management.server.port from application.yml so both share port 8080.
     *
     * This handler ensures the exception is:
     *   - Returned as a clean 404 JSON response (not 500)
     *   - Logged at DEBUG (not ERROR) because it is not a server fault
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.debug("Resource not found: {} {}", request.getMethod(), request.getRequestURI());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("NOT_FOUND")
                .globalErrors(List.of("Resource not found: " + request.getRequestURI()))
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Resource not found")
                        .errors(errorDetails)
                        .build());
    }

    // =========================================================================
    // 2. Validation Exceptions — 400
    // =========================================================================

    /**
     * Handles custom domain ValidationException.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            ValidationException ex) {

        log.warn("Validation exception: {}", ex.getMessage());

        ValidationResult result = ex.getValidationResult();

        List<ApiResponse.FieldError> fieldErrors = result.getErrors().stream()
                .map(error -> ApiResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getMessage())
                        .rejectedValue(error.getRejectedValue())
                        .code(error.getCode())
                        .build())
                .collect(Collectors.toList());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .fieldErrors(fieldErrors)
                .errorCode("VALIDATION_ERROR")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Validation failed")
                        .errors(errorDetails)
                        .build());
    }

    /**
     * Handles Spring's MethodArgumentNotValidException (@Valid / @Validated on
     * request body).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        log.warn("Method argument validation failed: {}", ex.getMessage());

        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ApiResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .code(error.getCode())
                        .build())
                .collect(Collectors.toList());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .fieldErrors(fieldErrors)
                .errorCode("VALIDATION_ERROR")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Validation failed")
                        .errors(errorDetails)
                        .build());
    }

    /**
     * Handles ConstraintViolationException (method-level @Validated constraints
     * on service or controller parameters).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {

        log.warn("Constraint violation: {}", ex.getMessage());

        List<ApiResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ApiResponse.FieldError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .fieldErrors(fieldErrors)
                .errorCode("CONSTRAINT_VIOLATION")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Constraint violation")
                        .errors(errorDetails)
                        .build());
    }

    // =========================================================================
    // 3. Domain Exceptions
    // =========================================================================

    /**
     * Handles domain EntityNotFoundException — 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
            EntityNotFoundException ex) {

        log.warn("Entity not found: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("ENTITY_NOT_FOUND")
                .globalErrors(List.of(ex.getMessage()))
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errors(errorDetails)
                        .build());
    }

    /**
     * Handles IllegalArgumentException — 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("ILLEGAL_ARGUMENT")
                .globalErrors(List.of(ex.getMessage()))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errors(errorDetails)
                        .build());
    }

    /**
     * Handles IllegalStateException — 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
            IllegalStateException ex) {

        log.warn("Illegal state: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("ILLEGAL_STATE")
                .globalErrors(List.of(ex.getMessage()))
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errors(errorDetails)
                        .build());
    }

    // =========================================================================
    // 4. Security Exceptions
    // =========================================================================

    /**
     * Handles Spring Security AccessDeniedException — 403.
     *
     * NOTE: Spring Security's ExceptionTranslationFilter normally intercepts
     * AccessDeniedException before it reaches the DispatcherServlet.
     * This handler acts as a safety net for cases where the exception
     * escapes to the MVC layer (e.g. thrown inside a @Service annotated
     * with @PreAuthorize and called from within the controller).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("ACCESS_DENIED")
                .globalErrors(List.of(
                        "You do not have permission to perform this action"
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Access denied")
                        .errors(errorDetails)
                        .build());
    }

    // =========================================================================
    // 5. HTTP Exceptions
    // =========================================================================

    /**
     * Handles HttpMessageNotReadableException — 400.
     * Triggered when the request body cannot be parsed (malformed JSON,
     * wrong content-type, etc.).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        log.warn("HTTP message not readable: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("MALFORMED_REQUEST")
                .globalErrors(List.of("Malformed JSON request"))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Invalid request format")
                        .errors(errorDetails)
                        .build());
    }

    /**
     * Handles MethodArgumentTypeMismatchException — 400.
     * Triggered when a path variable or request parameter cannot be converted
     * to the expected type (e.g. passing "abc" for a Long id).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        log.warn("Method argument type mismatch: {}", ex.getMessage());

        String error = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("TYPE_MISMATCH")
                .globalErrors(List.of(error))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Invalid parameter type")
                        .errors(errorDetails)
                        .build());
    }

    // =========================================================================
    // 6. Generic Catch-All Handlers
    // =========================================================================

    /**
     * Handles RuntimeException — 500.
     *
     * Declared BEFORE Exception.class so Spring prefers this handler for
     * all unchecked exceptions that are not already matched by a more
     * specific handler above.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex) {

        log.error("Runtime exception occurred", ex);

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("RUNTIME_ERROR")
                .globalErrors(List.of(
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Runtime error occurred"
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Runtime error")
                        .errors(errorDetails)
                        .build());
    }

    /**
     * Absolute catch-all — handles any checked exception not matched above — 500.
     *
     * This handler should rarely fire in practice. If it does, it indicates
     * a gap in the more specific handlers above that should be addressed.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {

        log.error("Unexpected exception occurred", ex);

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .globalErrors(List.of(
                        "An unexpected error occurred. Please try again later."
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Internal server error")
                        .errors(errorDetails)
                        .build());
    }
}