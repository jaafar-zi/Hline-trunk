package com.example.demo.modules.customer.application.dto.request;

import com.example.demo.common.base.dto.BaseFilter;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Filter criteria for searching customers
 *
 * @author Customer Domain Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Customer filter criteria")
public class CustomerFilterRequest extends BaseFilter {

    private static final long serialVersionUID = 1L;

    @Size(max = 100, message = "Name search must not exceed 100 characters")
    @Schema(description = "Search by name (first, last, or full)", example = "John")
    private String name;

    @Size(max = 255, message = "Email search must not exceed 255 characters")
    @Schema(description = "Search by email", example = "john@example.com")
    private String email;

    @Size(max = 20, message = "Phone search must not exceed 20 characters")
    @Schema(description = "Search by phone", example = "+1234567890")
    private String phone;

    @Schema(description = "Filter by customer status", example = "ACTIVE")
    private CustomerStatus status;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid ISO 639-1 code")
    @Schema(description = "Filter by language preference", example = "en")
    private String language;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    @Schema(description = "Filter by currency preference", example = "USD")
    private String currency;

    @Schema(description = "Filter by marketing opt-in", example = "true")
    private Boolean marketingOptIn;

    @Schema(description = "Filter by email notifications preference", example = "true")
    private Boolean emailNotifications;

    @Schema(description = "Filter customers created after this date", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAfter;

    @Schema(description = "Filter customers created before this date", example = "2024-12-31T23:59:59")
    private LocalDateTime createdBefore;

    @Schema(description = "Show only soft-deleted customers", example = "false")
    private Boolean onlyDeleted = false;

    @Override
    public boolean hasFilters() {
        return name               != null ||
               email              != null ||
               phone              != null ||
               status             != null ||
               language           != null ||
               currency           != null ||
               marketingOptIn     != null ||
               emailNotifications != null ||
               createdAfter       != null ||
               createdBefore      != null;
    }

    @Override
    public void normalize() {
        super.normalize();
        if (name     != null) name     = name.trim();
        if (email    != null) email    = email.trim().toLowerCase();
        if (phone    != null) phone    = phone.trim();
        if (language != null) language = language.trim().toLowerCase();
        if (currency != null) currency = currency.trim().toUpperCase();
    }
}