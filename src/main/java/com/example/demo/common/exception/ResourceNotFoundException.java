package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a generic resource is not found
 * Different from EntityNotFoundException (which is for JPA entities)
 *
 * @author Architecture Team
 * @version 1.0
 */
public class ResourceNotFoundException extends BusinessException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor with resource type and identifier
   */
  public ResourceNotFoundException(String resourceType, Object identifier) {
    super(
            HttpStatus.NOT_FOUND,
            ErrorCode.RESOURCE_NOT_FOUND,
            String.format("%s not found: %s", resourceType, identifier)
    );
  }

  /**
   * Constructor with custom message
   */
  public ResourceNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message);
  }

  /**
   * File not found exception
   */
  public static ResourceNotFoundException fileNotFound(String filename) {
    return new ResourceNotFoundException("File", filename);
  }

  /**
   * Image not found exception
   */
  public static ResourceNotFoundException imageNotFound(String imageId) {
    return new ResourceNotFoundException("Image", imageId);
  }

  /**
   * Document not found exception
   */
  public static ResourceNotFoundException documentNotFound(String documentId) {
    return new ResourceNotFoundException("Document", documentId);
  }
}