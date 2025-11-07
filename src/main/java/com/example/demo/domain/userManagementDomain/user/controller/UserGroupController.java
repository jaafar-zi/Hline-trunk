package com.example.demo.domain.userManagementDomain.user.controller;

import com.example.demo.domain.userManagementDomain.user.dto.CreateOrUpdateUserGroupDto;
import com.example.demo.domain.userManagementDomain.user.dto.UserGroupDTO;
import com.example.demo.domain.userManagementDomain.user.service.UserGroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-groups")
public class UserGroupController {

    private final UserGroupService userGroupService;

    @Autowired
    public UserGroupController(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    @GetMapping
    public ResponseEntity<Page<UserGroupDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userGroupService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGroupDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userGroupService.findById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<UserGroupDTO> getByName(@PathVariable String name) {
        return ResponseEntity.ok(userGroupService.findByName(name));
    }

    @PostMapping
    public ResponseEntity<UserGroupDTO> create(@RequestBody @Valid CreateOrUpdateUserGroupDto dto) {
        return ResponseEntity.ok(userGroupService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserGroupDTO> update(@PathVariable Long id,
                                               @RequestBody @Valid CreateOrUpdateUserGroupDto dto) {
        return ResponseEntity.ok(userGroupService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userGroupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<Void> deleteByName(@PathVariable String name) {
        userGroupService.deleteByName(name);
        return ResponseEntity.noContent().build();
    }
}

