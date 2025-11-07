package com.example.demo.domain.userManagementDomain.user.controller;

import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.userManagementDomain.user.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("users/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService favoriteService;

    @PostMapping("/{userId}/add/{productId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Integer userId, @PathVariable Long productId) {
        favoriteService.addFavoriteProduct(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Integer userId, @PathVariable Long productId) {
        favoriteService.removeFavoriteProduct(userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Set<Product>> getFavorites(@PathVariable Integer userId) {
        return ResponseEntity.ok(favoriteService.getFavoriteProducts(userId));
    }

    @GetMapping("/{userId}/contains/{productId}")
    public ResponseEntity<Boolean> isFavorited(@PathVariable Integer userId, @PathVariable Long productId) {
        return ResponseEntity.ok(favoriteService.isProductFavorited(userId, productId));
    }
}

