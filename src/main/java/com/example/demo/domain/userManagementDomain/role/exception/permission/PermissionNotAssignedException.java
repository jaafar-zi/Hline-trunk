package com.example.demo.domain.userManagementDomain.role.exception.permission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissionNotAssignedException extends PermissionException {
    public PermissionNotAssignedException(Long permissionId) {
        super("Permission with id " + permissionId + " is not assigned.");
    }

    public PermissionNotAssignedException(String message) {
        super(message);
    }
}
