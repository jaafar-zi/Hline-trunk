// package com.example.demo.modules.user.application.validator;

// import com.example.demo.modules.user.application.dto.request.ChangePasswordRequest;
// import org.springframework.stereotype.Component;
// import org.springframework.validation.Errors;
// import org.springframework.validation.Validator;

// /**
//  * Spring Validator for ChangePasswordRequest
//  *
//  * @author Architecture Team
//  * @version 1.0
//  */
// @Component
// public class ChangePasswordRequestValidator implements Validator {

//     @Override
//     public boolean supports(Class<?> clazz) {
//         return ChangePasswordRequest.class.equals(clazz);
//     }

//     @Override
//     public void validate(Object target, Errors errors) {
//         ChangePasswordRequest request = (ChangePasswordRequest) target;

//         // Password confirmation match
//         if (request.getNewPassword() != null && request.getConfirmPassword() != null) {
//             if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//                 errors.rejectValue("confirmPassword", "password.mismatch",
//                         "Password confirmation does not match");
//             }
//         }

//         // New password must be different from current
//         if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
//             if (request.getCurrentPassword().equals(request.getNewPassword())) {
//                 errors.rejectValue("newPassword", "password.same",
//                         "New password must be different from current password");
//             }
//         }

//         // Check for common weak passwords
//         if (request.getNewPassword() != null) {
//             validateNotCommonPassword(request.getNewPassword(), errors);
//         }
//     }

//     private void validateNotCommonPassword(String password, Errors errors) {
//         String[] commonPasswords = {
//                 "password", "12345678", "password123", "qwerty123",
//                 "admin123", "welcome123", "letmein"
//         };

//         for (String common : commonPasswords) {
//             if (password.equalsIgnoreCase(common)) {
//                 errors.rejectValue("newPassword", "password.common",
//                         "Password is too common. Please choose a more secure password");
//                 break;
//             }
//         }
//     }
// }
