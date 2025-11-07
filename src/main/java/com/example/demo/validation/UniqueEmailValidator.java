package com.example.demo.validation;

import com.example.demo.domain.userManagementDomain.auth.request.RegistrationRequest;
import com.example.demo.domain.userManagementDomain.user.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, RegistrationRequest> {
    private final UserRepository userRepository;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(RegistrationRequest registrationRequest, ConstraintValidatorContext constraintValidatorContext) {
        return userRepository.findByEmail(registrationRequest.getEmail()).isEmpty();
    }
}
