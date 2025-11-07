package com.example.demo.domain.userManagementDomain.user.dto;

import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateUserGroupDto {
    private String name;
    private Set<Long> roleIds;
    private Set<Integer> userIds;
}
