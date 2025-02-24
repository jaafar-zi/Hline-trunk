package com.example.demo.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    //    @NotEmpty(message = "Email is mandatory")
    //    @NotBlank(message = "Email is mandatory")
    //    @Email(message = "Email is not well formated")
    private String email;
    //    @NotEmpty(message = "Password is mandatory")
    //    @NotBlank(message = "Password is mandatory")
    //    @Size(message = "Password should be 8 chars long minimum")
    private String password;
}
