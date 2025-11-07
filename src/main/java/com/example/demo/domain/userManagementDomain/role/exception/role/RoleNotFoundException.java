package com.example.demo.domain.userManagementDomain.role.exception.role;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RoleException {
    public RoleNotFoundException(Long roleId) {
        super("Role not found with id: " + roleId);
    }

    public RoleNotFoundException(String message) {
        super(message);
    }
}

