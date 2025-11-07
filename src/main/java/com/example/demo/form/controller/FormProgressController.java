package com.example.demo.form.controller;

import com.example.demo.form.request.FormProgressRequest;
import com.example.demo.form.service.FormProgressService;
import com.example.demo.domain.userManagementDomain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/form-progress")
public class FormProgressController {

    @Autowired
    private FormProgressService service;
    @Autowired
    private UserService userService;

    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody FormProgressRequest request, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal); // you’ll need a method for this
        service.saveOrUpdate(userId, request.getFormName(), request.getFormData());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/load")
    public ResponseEntity<?> loadProgress(@RequestParam String formName, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return service.getProgress(userId, formName)
                .map(fp -> ResponseEntity.ok(fp.getFormData()))
                .orElse(ResponseEntity.noContent().build());
    }

    //should be handled later
    //throw an exception if the user is not found
    private Long getUserIdFromPrincipal(Principal principal) {
        // Use your user service or token parsing to extract the ID
        Optional<?> byUsername = userService.findByFirstName(principal.getName());
         if (byUsername.isPresent()) {
             return (Long) byUsername.get();
         }
         else  return -1L;
    }
}

