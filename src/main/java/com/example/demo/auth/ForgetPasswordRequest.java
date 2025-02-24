package com.example.demo.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForgetPasswordRequest {

    @NotEmpty(message = "Email is mandatory")
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email is not well formated")
    private final String email;
}
