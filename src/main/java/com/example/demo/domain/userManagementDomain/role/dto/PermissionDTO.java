package com.example.demo.domain.userManagementDomain.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private Long id;

    @NotBlank(message = "Permission name must not be blank")
    private String name;
}
