package com.example.demo.domain.userManagementDomain.user.dto;

import com.example.demo.domain.userManagementDomain.role.dto.RoleDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupDTO {
    private Long id;
    private String name;
    private Set<RoleDTO> roles;
    private Set<UserDTO> users;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
