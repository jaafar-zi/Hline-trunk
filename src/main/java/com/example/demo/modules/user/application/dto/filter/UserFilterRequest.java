package com.example.demo.modules.user.application.dto.filter;

import com.example.demo.common.base.dto.BaseRequest;
import com.example.demo.modules.user.domain.model.vo.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Filter request for searching users
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User filter/search request")
public class UserFilterRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Search keyword (searches in name, email)", example = "john")
    private String keyword;

    @Schema(description = "Filter by email", example = "john@example.com")
    private String email;

    @Schema(description = "Filter by first name", example = "John")
    private String firstName;

    @Schema(description = "Filter by last name", example = "Doe")
    private String lastName;

    @Schema(description = "Filter by enabled status")
    private Boolean enabled;

    @Schema(description = "Filter by locked status")
    private Boolean locked;

    @Schema(description = "Filter by email verified status")
    private Boolean emailVerified;

    @Schema(description = "Filter by user status")
    private UserStatus status;

    @Schema(description = "Filter by role names")
    private Set<String> roleNames;

    @Schema(description = "Filter by user group ID")
    private String userGroupId;

    @Schema(description = "Created after this date")
    private LocalDateTime createdAfter;

    @Schema(description = "Created before this date")
    private LocalDateTime createdBefore;

    @Schema(description = "Last login after this date")
    private LocalDateTime lastLoginAfter;

    @Schema(description = "Never logged in", example = "false")
    private Boolean neverLoggedIn;

    // Pagination
    @Schema(description = "Page number (0-based)", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "20")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "Sort field", example = "createdAt")
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction (ASC/DESC)", example = "DESC")
    @Builder.Default
    private String sortDirection = "DESC";

    /**
     * Convert to Spring Pageable
     */
    public Pageable toPageable() {
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );
        return PageRequest.of(page, size, sort);
    }

    /**
     * Check if any filter is applied
     */
    public boolean hasFilters() {
        return keyword != null || email != null || firstName != null || lastName != null ||
                enabled != null || locked != null || emailVerified != null || status != null ||
                (roleNames != null && !roleNames.isEmpty()) || userGroupId != null ||
                createdAfter != null || createdBefore != null || lastLoginAfter != null ||
                Boolean.TRUE.equals(neverLoggedIn);
    }
}