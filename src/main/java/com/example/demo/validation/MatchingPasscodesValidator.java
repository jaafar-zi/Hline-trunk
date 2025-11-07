package com.example.demo.validation;

import com.example.demo.domain.userManagementDomain.auth.request.ResetPasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MatchingPasscodesValidator implements ConstraintValidator<MatchingPasscodes, ResetPasswordRequest> {
    @Override
    public void initialize(MatchingPasscodes constraintAnnotation) {
    }

    @Override
    public boolean isValid(ResetPasswordRequest request, ConstraintValidatorContext context) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null) {
            return false;
        }
        return request.getNewPassword().equals(request.getConfirmNewPassword());
    }
}
