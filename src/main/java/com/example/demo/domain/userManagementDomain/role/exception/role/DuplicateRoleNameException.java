package com.example.demo.domain.userManagementDomain.role.exception.role;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRoleNameException extends RoleException {
    public DuplicateRoleNameException(String roleName) {
        super("A role with name '" + roleName + "' already exists.");
    }

    public DuplicateRoleNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
