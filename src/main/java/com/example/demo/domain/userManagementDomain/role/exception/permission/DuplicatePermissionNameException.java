package com.example.demo.domain.userManagementDomain.role.exception.permission;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePermissionNameException extends PermissionException {
    public DuplicatePermissionNameException(String permissionName) {
        super("A permission with name '" + permissionName + "' already exists.");
    }

    public DuplicatePermissionNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
