package com.example.demo.domain.userManagementDomain.auth.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgetPasswordRequest {
    private String email;
}
