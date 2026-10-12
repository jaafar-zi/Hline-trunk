package com.example.demo.modules.auth.application.service;

import com.example.demo.config.security.CustomUserDetailsService;
import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Auth principal loader – delegates to existing infrastructure.
 *
 * User   → CustomUserDetailsService (existing)
 * Customer → builds a lightweight UserDetails from Customer aggregate
 *
 * @author Auth Domain Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthPrincipalService {

    private final CustomUserDetailsService userDetailsService;
    private final CustomerRepository       customerRepository;

    /**
     * Load principal by email and subject type.
     * Called by JwtAuthenticationFilter after token validation.
     */
    public UserDetails loadPrincipal(String email, String subjectType) {
        return switch (subjectType != null ? subjectType.toUpperCase() : "USER") {
            case "CUSTOMER" -> loadCustomerPrincipal(email);
            default         -> userDetailsService.loadUserByUsername(email);
        };
    }

    /**
     * Build a Spring Security UserDetails from a Customer aggregate.
     *
     * Customers authenticate via a linked User account (same email).
     * The ROLE_CUSTOMER authority controls access to customer endpoints.
     */
    public UserDetails loadCustomerPrincipal(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Customer not found with email: " + email));

        if (!customer.canLogin()) {
            throw new UsernameNotFoundException(
                    "Customer account cannot login: " + email);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(customer.getEmail().getValue())
                .password("") // password validated via User aggregate
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accountLocked(!customer.canLogin())
                .disabled(!customer.isActive())
                .build();
    }
}