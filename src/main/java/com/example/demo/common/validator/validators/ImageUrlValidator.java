package com.example.demo.common.validator.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.common.validator.constraints.ValidImageUrl;

/**
 * Image URL validator implementation
 * 
 * @author Architecture Team
 * @version 1.0
 */
public class ImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {

    private Set<String> allowedExtensions;
    private boolean requireHttps;
    private boolean allowNull;
    private int maxLength;

    @Override
    public void initialize(ValidImageUrl annotation) {
        this.allowedExtensions = Arrays.stream(annotation.allowedExtensions())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.requireHttps = annotation.requireHttps();
        this.allowNull = annotation.allowNull();
        this.maxLength = annotation.maxLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (value.isBlank()) {
            return false;
        }

        String url = value.trim();

        // Length check
        if (url.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("URL length cannot exceed %d characters", maxLength)
            ).addConstraintViolation();
            return false;
        }

        // URL format check
        try {
            URL urlObj = new URL(url);
            
            // HTTPS check
            if (requireHttps && !"https".equalsIgnoreCase(urlObj.getProtocol())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Image URL must use HTTPS protocol"
                ).addConstraintViolation();
                return false;
            }

            // Protocol check
            String protocol = urlObj.getProtocol().toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Image URL must use HTTP or HTTPS protocol"
                ).addConstraintViolation();
                return false;
            }

        } catch (MalformedURLException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid URL format"
            ).addConstraintViolation();
            return false;
        }

        // Extension check
        String lowerUrl = url.toLowerCase();
        boolean hasValidExtension = allowedExtensions.stream()
                .anyMatch(ext -> lowerUrl.endsWith("." + ext));

        if (!hasValidExtension) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Image URL must end with one of: %s", 
                            String.join(", ", allowedExtensions))
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}