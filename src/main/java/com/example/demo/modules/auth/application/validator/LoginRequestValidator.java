package com.example.demo.modules.auth.application.validator;

import com.example.demo.modules.auth.application.dto.request.LoginRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

@Component
public class LoginRequestValidator implements Validator {

    private static final Set<String> VALID_SUBJECT_TYPES = Set.of("USER", "CUSTOMER");

    @Override
    public boolean supports(Class<?> clazz) {
        return LoginRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LoginRequest request = (LoginRequest) target;

        if (request.getSubjectType() != null
                && !VALID_SUBJECT_TYPES.contains(request.getSubjectType().toUpperCase())) {
            errors.rejectValue(
                    "subjectType",
                    "auth.subjectType.invalid",
                    "Subject type must be USER or CUSTOMER"
            );
        }
    }
}