package com.example.demo.domain.userManagementDomain.role.exception.permission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends PermissionException {
    public PermissionNotFoundException(Long permissionId) {
        super("Permission not found with id: " + permissionId);
    }

    public PermissionNotFoundException(String message) {
        super(message);
    }
}
