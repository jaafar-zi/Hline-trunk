package com.example.demo.modules.customer.interfaces;

import com.example.demo.modules.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security helper bean for customer ownership checks.
 * Used in @PreAuthorize expressions.
 */
@Component("customerSecurity")
@RequiredArgsConstructor
public class CustomerSecurity {

    private final CustomerRepository customerRepository;

    /**
     * Check if the authenticated user owns the customer profile.
     */
    public boolean isOwner(UUID customerId, UserDetails principal) {
        if (principal == null) return false;

        return customerRepository.findById(customerId)
                .map(c -> c.getEmail().getValue().equalsIgnoreCase(principal.getUsername()))
                .orElse(false);
    }
}