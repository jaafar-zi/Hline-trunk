package com.example.demo.modules.auth.application.validator;

import com.example.demo.modules.auth.application.dto.request.ChangePasswordRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordRequestValidator implements Validator {

    private static final String[] COMMON_PASSWORDS = {
            "password", "12345678", "password123", "qwerty123",
            "admin123", "welcome123", "letmein"
    };

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangePasswordRequest request = (ChangePasswordRequest) target;

        // Confirmation match
        if (request.getNewPassword() != null
                && request.getConfirmPassword() != null
                && !request.getNewPassword().equals(request.getConfirmPassword())) {
            errors.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Password confirmation does not match"
            );
        }

        // Must differ from current
        if (request.getCurrentPassword() != null
                && request.getNewPassword() != null
                && request.getCurrentPassword().equals(request.getNewPassword())) {
            errors.rejectValue(
                    "newPassword",
                    "password.same",
                    "New password must be different from current password"
            );
        }

        // Common password check
        if (request.getNewPassword() != null) {
            for (String common : COMMON_PASSWORDS) {
                if (request.getNewPassword().equalsIgnoreCase(common)) {
                    errors.rejectValue(
                            "newPassword",
                            "password.common",
                            "Password is too common"
                    );
                    break;
                }
            }
        }
    }
}