package com.example.demo.config.security;

import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation
 *
 * Loads user details from the database for Spring Security authentication.
 * Since User entity implements UserDetails interface, we can return it directly.
 *
 * Features:
 * - Loads user by email (username in Spring Security context)
 * - Eagerly loads roles and permissions to avoid lazy loading issues
 * - Validates user account status (locked, disabled, expired)
 * - Handles temporary locks automatically
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case)
     *
     * The User entity implements UserDetails interface, so we return it directly.
     * We use findByEmailWithRoles() to eagerly load roles and permissions,
     * preventing N+1 queries and lazy loading exceptions.
     *
     * @param username the email address (User.getUsername() returns email.value)
     * @return UserDetails implementation (User entity)
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username (email): {}", username);

        // Find user by email with roles and permissions eagerly loaded
        User user = userRepository.findByEmailWithRoles(username)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        // Check if temporary lock has expired and unlock if needed
        if (user.isAccountNonLocked() && user.getAccountLocked() && user.isTemporaryLockExpired()) {
            log.info("Temporary lock expired for user: {}", username);
            // The isAccountNonLocked() method in User entity handles this automatically
        }

        log.debug("User loaded successfully: email={}, enabled={}, locked={}, roles={}",
                username,
                user.isEnabled(),
                user.getAccountLocked(),
                user.getRoles().size());

        // User entity implements UserDetails, so return it directly
        return user;
    }

    /**
     * Load user by email (alternative method for clarity)
     *
     * @param email the email address
     * @return User entity (which implements UserDetails)
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    /**
     * Load user by ID
     * Useful for token-based authentication where we have user ID in token
     *
     * @param userId the user ID
     * @return User entity (which implements UserDetails)
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User loadUserById(java.util.UUID userId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });
    }

    /**
     * Check if user exists by email
     *
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }
}