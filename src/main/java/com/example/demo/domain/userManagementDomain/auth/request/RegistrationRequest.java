package com.example.demo.domain.userManagementDomain.auth.request;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
//@UniqueEmail
public class RegistrationRequest {

//    @NotEmpty(message = "FirstName is mandatory")
//    @NotBlank(message = "FirstName is mandatory")
    private String firstName;
//    @NotEmpty(message = "LastName is mandatory")
//    @NotBlank(message = "LastName is mandatory")
    private String lastName;
//    @NotEmpty(message = "Email is mandatory")
//    @NotBlank(message = "Email is mandatory")
//    @Email(message = "Email is not well formated")
    private String email;
//    @NotEmpty(message = "LastName is mandatory")
//    @NotBlank(message = "LastName is mandatory")
//    @Size(message = "Password should be 8 chars long minimum", min = 8)
    private String password;

}
