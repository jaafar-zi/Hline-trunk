package com.example.demo.domain.userManagementDomain.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionDto {
    @NotBlank(message = "Permission name is required")
    private String name;
}
