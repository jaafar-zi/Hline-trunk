package com.example.demo.domain.userManagementDomain.auth.request;

import com.example.demo.validation.MatchingPasscodes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@MatchingPasscodes
public class ResetPasswordRequest {

    @NotEmpty(message = "email is mandatory")
    @NotBlank(message = "email is mandatory")
    @Email(message = "email should be well formatted")
    private String email;
    @NotEmpty(message = "token is mandatory")
    @NotBlank(message = "token is mandatory")
    private String token;
    @NotEmpty(message = "Password is mandatory")
    @NotBlank(message = "Password is mandatory")
    private String newPassword;
    @NotEmpty(message = "New Password is mandatory")
    @NotBlank(message = "New Password is mandatory")
    private String confirmNewPassword;
}
