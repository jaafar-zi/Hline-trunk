package com.example.demo.domain.userManagementDomain.user.service;

import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void addFavoriteProduct(Integer userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        user.getFavoriteProducts().add(product);
        userRepository.save(user);
    }

    public void removeFavoriteProduct(Integer userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        user.getFavoriteProducts().remove(product);
        userRepository.save(user);
    }

    public Set<Product> getFavoriteProducts(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user.getFavoriteProducts();
    }

    public boolean isProductFavorited(Integer userId, Long productId) {
        return getFavoriteProducts(userId).stream()
                .anyMatch(p -> p.getId().equals(productId));
    }
}

