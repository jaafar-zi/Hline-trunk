package com.example.demo.domain.userManagementDomain.user.controller;

import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.user.dto.UserDTO;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.entity.UserGroup;
import com.example.demo.domain.userManagementDomain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<UserDTO>> getUsersPaged(Pageable pageable) {
        Page<UserDTO> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public UserDTO getCurrentUser(Principal principal) {
        return userService.findByPrincipal(principal);
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Integer id, @RequestBody UserDTO updatedUser) {
        return userService.update(id, updatedUser);
    }

    @PutMapping("/{id}/enable")
    public void enableUser(@PathVariable Integer id) {
        userService.enableUser(id);
    }

    @PutMapping("/{id}/disable")
    public void disableUser(@PathVariable Integer id) {
        userService.disableUser(id);
    }

    @PutMapping("/{id}/lock")
    public void lockUser(@PathVariable Integer id) {
        userService.lockUser(id);
    }

    @PutMapping("/{id}/unlock")
    public void unlockUser(@PathVariable Integer id) {
        userService.unlockUser(id);
    }

    @PutMapping("/{id}/password")
    public void updatePassword(@PathVariable Integer id, @RequestBody String newPassword) {
        userService.updatePassword(id, newPassword);
    }

    @PutMapping("/{id}/email")
    public void updateEmail(@PathVariable Integer id, @RequestBody String newEmail) {
        userService.updateEmail(id, newEmail);
    }

    @PutMapping("/{id}/group")
    public void assignUserGroup(@PathVariable Integer id, @RequestBody UserGroup group) {
        userService.assignUserGroup(id, group);
    }

    @DeleteMapping("/{id}/group")
    public void removeUserGroup(@PathVariable Integer id) {
        userService.removeUserGroup(id);
    }

    @PutMapping("/{id}/roles")
    public void assignRole(@PathVariable Integer id, @RequestBody Role role) {
        userService.assignRole(id, role);
    }

    @DeleteMapping("/{id}/roles")
    public void removeRole(@PathVariable Integer id, @RequestBody Role role) {
        userService.removeRole(id, role);
    }

    @GetMapping("/{id}/roles")
    public Set<Role> getUserRoles(@PathVariable Integer id) {
        return userService.getUserRoles(id);
    }

    @PutMapping("/{id}/profile")
    public void updateProfile(
            @PathVariable Integer id,
            @RequestParam String firstname,
            @RequestParam String lastname,
            @RequestParam String dateOfBirth // Expecting ISO format
    ) {
        userService.updateProfile(id, firstname, lastname, LocalDate.parse(dateOfBirth));
    }
}
