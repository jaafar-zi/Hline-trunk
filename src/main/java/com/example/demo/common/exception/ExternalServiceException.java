package com.example.demo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when external service call fails
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
public class ExternalServiceException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Name of the external service
     */
    private final String serviceName;

    /**
     * HTTP status code from external service (if applicable)
     */
    private final Integer externalStatusCode;

    /**
     * Constructor with service name and message
     */
    public ExternalServiceException(String serviceName, String message) {
        super(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("External service error: %s - %s", serviceName, message)
        );
        this.serviceName = serviceName;
        this.externalStatusCode = null;
    }

    /**
     * Constructor with service name, message, and cause
     */
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("External service error: %s - %s", serviceName, message),
                null,
                cause
        );
        this.serviceName = serviceName;
        this.externalStatusCode = null;
    }

    /**
     * Constructor with external status code
     */
    public ExternalServiceException(String serviceName, String message, int externalStatusCode) {
        super(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("External service error: %s - %s (Status: %d)",
                        serviceName, message, externalStatusCode)
        );
        this.serviceName = serviceName;
        this.externalStatusCode = externalStatusCode;
    }

    /**
     * Service unavailable exception
     */
    public static ExternalServiceException serviceUnavailable(String serviceName) {
        return new ExternalServiceException(
                serviceName,
                "Service is currently unavailable"
        );
    }

    /**
     * Timeout exception
     */
    public static ExternalServiceException timeout(String serviceName) {
        return new ExternalServiceException(
                serviceName,
                "Request to service timed out"
        );
    }

    /**
     * Invalid response exception
     */
    public static ExternalServiceException invalidResponse(String serviceName, String reason) {
        return new ExternalServiceException(
                serviceName,
                String.format("Invalid response from service: %s", reason)
        );
    }
}