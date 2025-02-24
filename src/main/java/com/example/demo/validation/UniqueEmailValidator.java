package com.example.demo.validation;

import com.example.demo.auth.RegistrationRequest;
import com.example.demo.auth.ResetPasswordRequest;
import com.example.demo.user.UserRespository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, RegistrationRequest> {
    private final UserRespository userRespository;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(RegistrationRequest registrationRequest, ConstraintValidatorContext constraintValidatorContext) {
        return userRespository.findByEmail(registrationRequest.getEmail()).isEmpty();
    }
}
