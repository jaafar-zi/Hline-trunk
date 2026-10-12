package com.example.demo.modules.auth.application.validator;

import com.example.demo.modules.auth.application.dto.request.ResetPasswordRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ResetPasswordRequestValidator implements Validator {

    private static final String[] COMMON_PASSWORDS = {
            "password", "12345678", "password123", "qwerty123",
            "admin123", "welcome123", "letmein"
    };

    @Override
    public boolean supports(Class<?> clazz) {
        return ResetPasswordRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ResetPasswordRequest request = (ResetPasswordRequest) target;

        // Password confirmation match
        if (request.getNewPassword() != null
                && request.getConfirmPassword() != null
                && !request.getNewPassword().equals(request.getConfirmPassword())) {
            errors.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Password confirmation does not match"
            );
        }

        // Common password check
        if (request.getNewPassword() != null) {
            for (String common : COMMON_PASSWORDS) {
                if (request.getNewPassword().equalsIgnoreCase(common)) {
                    errors.rejectValue(
                            "newPassword",
                            "password.common",
                            "Password is too common. Please choose a more secure password"
                    );
                    break;
                }
            }
        }
    }
}