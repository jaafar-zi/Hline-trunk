package com.example.demo.domain.userManagementDomain.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;

    @NotBlank(message = "Role name is required")
    private String name;

    @NotEmpty(message = "At least one permission is required")
    private Set<Long> permissionIds;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;
}

