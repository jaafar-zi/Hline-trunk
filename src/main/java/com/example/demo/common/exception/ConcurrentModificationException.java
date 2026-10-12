package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when optimistic locking fails
 *
 * @author Architecture Team
 * @version 1.0
 */
public class ConcurrentModificationException extends BusinessException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor with entity class
   */
  public ConcurrentModificationException(Class<?> entityClass) {
    super(
            HttpStatus.CONFLICT,
            ErrorCode.CONCURRENT_MODIFICATION,
            String.format("%s was modified by another user", entityClass.getSimpleName()),
            "The record has been updated by another user. Please refresh and try again."
    );
  }

  /**
   * Constructor with entity name
   */
  public ConcurrentModificationException(String entityName) {
    super(
            HttpStatus.CONFLICT,
            ErrorCode.CONCURRENT_MODIFICATION,
            String.format("%s was modified by another user", entityName),
            "The record has been updated by another user. Please refresh and try again."
    );
  }

  /**
   * Constructor with custom message
   */
  public static ConcurrentModificationException withMessage(String message) {
    return new ConcurrentModificationException(message);
  }
}