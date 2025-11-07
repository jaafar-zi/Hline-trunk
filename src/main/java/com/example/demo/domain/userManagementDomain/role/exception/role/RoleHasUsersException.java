package com.example.demo.domain.userManagementDomain.role.exception.role;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoleHasUsersException extends RoleException {
    public RoleHasUsersException(Long roleId) {
        super("Cannot delete or modify role with id " + roleId + " because it is assigned to existing users.");
    }

    public RoleHasUsersException(String message) {
        super(message);
    }
}
