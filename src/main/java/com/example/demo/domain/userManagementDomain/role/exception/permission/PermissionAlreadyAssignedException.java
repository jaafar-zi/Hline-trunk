package com.example.demo.domain.userManagementDomain.role.exception.permission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissionAlreadyAssignedException extends PermissionException {
    public PermissionAlreadyAssignedException(Long permissionId) {
        super("Permission with id " + permissionId + " is already assigned.");
    }

    public PermissionAlreadyAssignedException(String message) {
        super(message);
    }
}
