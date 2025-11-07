package com.example.demo.entityProfile.controller;

import com.example.demo.entityProfile.dto.EntityProfileDTO;
import com.example.demo.entityProfile.service.EntityProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entityProfiles")
public class EntityProfileController {
    @Autowired
    private EntityProfileService entityProfileService;

    @PostMapping
    public EntityProfileDTO createEntityProfile(@RequestBody String className) throws ClassNotFoundException {
        Class<?> entityClass = entityProfileService.resolveClassName(className);
        return entityProfileService.generateEntityProfile(entityClass);
    }

    @GetMapping("/{id}")
    public EntityProfileDTO getEntityProfile(@PathVariable Long id) {
        return entityProfileService.getEntityProfile(id);
    }
}
