package com.example.demo.modules.user.domain.model;

import com.example.demo.common.base.entity.SoftDeletableAggregateRoot;
import com.example.demo.common.event.DomainEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.util.ChangeTracker;
import com.example.demo.modules.user.domain.event.*;
import com.example.demo.modules.user.domain.model.vo.Email;
import com.example.demo.modules.user.domain.model.vo.UserProfile;
import com.example.demo.modules.user.domain.model.vo.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Aggregate Root
 *
 * Responsibilities:
 * - User authentication and authorization
 * - User profile management
 * - Role and permission management
 * - Account status management
 * - Domain event publishing
 *
 * Business Rules:
 * - Email must be unique
 * - Password must meet complexity requirements
 * - Users must have at least one role
 * - Locked accounts cannot authenticate
 * - Disabled accounts cannot perform actions
 *
 * @author Architecture Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_status", columnList = "enabled, account_locked"),
                @Index(name = "idx_user_group", columnList = "user_group_id"),
                @Index(name = "idx_user_created_at", columnList = "created_at")
        }
)
public class User extends SoftDeletableAggregateRoot implements UserDetails, Principal {

    // ====================================
    // Profile Information
    // ====================================

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, nullable = false, length = 255))
    private Email email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Embedded
    private UserProfile profile;

    // ====================================
    // Account Status
    // ====================================

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "account_expires_at")
    private LocalDateTime accountExpiresAt;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // ====================================
    // Relationships
    // ====================================

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    // ====================================
    // Factory Methods
    // ====================================

    /**
     * Register new user
     *
     * Events Published:
     * - EntityCreatedEvent<User> (via AbstractCommandService after save)
     */
    public static User register(String firstName, String lastName, Email email, String encodedPassword) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash(encodedPassword)
                .enabled(false)
                .emailVerified(false)
                .accountLocked(false)
                .emailVerificationToken(generateVerificationToken())
                .build();
        // No event registration - handled by AbstractCommandService
    }

    // ====================================
    // Domain Logic - Account Status
    // ====================================

    /**
     * Activate user account
     *
     * Events Published:
     * - UserActivatedEvent (domain-specific for critical business event)
     */
    public void activate() {
        if (this.enabled) {
            throw new IllegalStateException("User is already active");
        }

        this.enabled = true;
        this.emailVerified = true;
        this.emailVerificationToken = null;

        registerEvent(UserActivatedEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .activatedBy(getCurrentUsername())
                .activationMethod("email_verification")
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Lock user account
     *
     * Events Published:
     * - UserLockedEvent (domain-specific for security event)
     */
    public void lock(String reason) {
        if (this.accountLocked) {
            throw new IllegalStateException("User is already locked");
        }

        this.accountLocked = true;
        this.lockedUntil = null;

        registerEvent(UserLockedEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .reason(reason)
                .lockedBy(getCurrentUsername())
                .temporary(false)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Temporarily lock account
     *
     * Events Published:
     * - UserLockedEvent (with temporary flag)
     */
    public void temporaryLock(int durationMinutes) {
        this.accountLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(durationMinutes);

        registerEvent(UserLockedEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .reason("Temporary lock for " + durationMinutes + " minutes")
                .lockedBy("system")
                .temporary(true)
                .durationMinutes(durationMinutes)
                .lockedUntil(this.lockedUntil)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Unlock user account
     *
     * Events Published:
     * - UserUnlockedEvent (domain-specific for security event)
     */
    public void unlock() {
        if (!this.accountLocked) {
            throw new IllegalStateException("User is not locked");
        }

        this.accountLocked = false;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;

        registerEvent(UserUnlockedEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .unlockedBy(getCurrentUsername())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Deactivate user account
     *
     * Events Published:
     * - EntityUpdatedEvent<User> (with change tracking)
     */
    public void deactivate(String reason) {
        if (!this.enabled) {
            throw new IllegalStateException("User is already deactivated");
        }

        ChangeTracker tracker = new ChangeTracker();
        tracker.track("enabled", this.enabled, false);
        tracker.track("deactivationReason", null, reason);

        this.enabled = false;

        registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
    }

    // ====================================
    // Domain Logic - Authentication
    // ====================================

    /**
     * Check if temporary lock has expired
     */
    public boolean isTemporaryLockExpired() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(lockedUntil);
    }

    /**
     * Record successful login
     *
     * Events Published:
     * - UserLoginEvent (domain-specific for audit trail)
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;

        if (this.accountLocked && isTemporaryLockExpired()) {
            unlock();
        }

        registerEvent(UserLoginEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .loginMethod("password")
                .ipAddress("unknown") // Set by service
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Record failed login attempt
     *
     * Events Published:
     * - UserLoginFailedEvent (domain-specific for security monitoring)
     */
    public void recordFailedLogin(int maxAttempts) {
        this.failedLoginAttempts++;

        boolean willLock = this.failedLoginAttempts >= maxAttempts;

        registerEvent(UserLoginFailedEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .failedAttempts(this.failedLoginAttempts)
                .occurredAt(LocalDateTime.now())
                .accountLocked(willLock)
                .reason("invalid_credentials")
                .build());

        if (willLock) {
            temporaryLock(30);
        }
    }

    // ====================================
    // Domain Logic - Password Management
    // ====================================

    /**
     * Change password
     *
     * Events Published:
     * - UserPasswordChangedEvent (domain-specific for security event)
     */
    public void changePassword(String newEncodedPassword) {
        boolean wasExpired = this.passwordExpiresAt != null &&
                LocalDateTime.now().isAfter(this.passwordExpiresAt);

        this.passwordHash = newEncodedPassword;
        this.passwordExpiresAt = LocalDateTime.now().plusDays(90);

        registerEvent(UserPasswordChangedEvent.builder()
                .userId(this.getId())
                .email(this.email.getValue())
                .changedBy(getCurrentUsername())
                .selfService(true)
                .wasExpired(wasExpired)
                .ipAddress("unknown") // Set by service
                .occurredAt(LocalDateTime.now())
                .build());
    }

    /**
     * Change email address
     *
     * Events Published:
     * - UserEmailChangedEvent (domain-specific for security-critical operation)
     */
    public void changeEmail(Email newEmail, String verificationToken) {
        if (newEmail == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (newEmail.equals(this.email)) {
            throw new IllegalStateException("New email is the same as current email");
        }

        String oldEmail = this.email.getValue();
        this.email = newEmail;
        this.emailVerified = false;
        this.emailVerificationToken = verificationToken;

        registerEvent(UserEmailChangedEvent.builder()
                .userId(this.getId())
                .oldEmail(oldEmail)
                .newEmail(newEmail.getValue())
                .changedBy(getCurrentUsername())
                .selfService(true)
                .requiresVerification(true)
                .terminateSessions(true)
                .verificationToken(verificationToken)
                .ipAddress("unknown") // Set by service
                .occurredAt(LocalDateTime.now())
                .build());
    }

    // ====================================
    // Domain Logic - Role Management
    // ====================================

    /**
     * Assign role to user
     *
     * Events Published:
     * - EntityUpdatedEvent<User> (with change tracking)
     */
    public void assignRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (this.roles.contains(role)) {
            throw new IllegalStateException("User already has this role");
        }

        boolean added = this.roles.add(role);

        if (added) {
            EntityUpdatedEvent<User> event = new EntityUpdatedEvent<>(this);
            event.addChange("roles", null, "Added role: " + role.getName());
            registerEvent(event);
        }
    }

    /**
     * Remove role from user
     *
     * Events Published:
     * - EntityUpdatedEvent<User> (with change tracking)
     */
    public void removeRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (this.roles.size() == 1) {
            throw new IllegalStateException("User must have at least one role");
        }

        boolean removed = this.roles.remove(role);

        if (removed) {
            EntityUpdatedEvent<User> event = new EntityUpdatedEvent<>(this);
            event.addChange("roles", "Removed role: " + role.getName(), null);
            registerEvent(event);
        }
    }

    // ====================================
    // Domain Logic - Profile Management
    // ====================================

    /**
     * Update profile information
     *
     * Events Published:
     * - EntityUpdatedEvent<User> (with detailed change tracking)
     */
    public void updateProfile(String firstName, String lastName, UserProfile profile) {
        ChangeTracker tracker = new ChangeTracker();

        if (firstName != null && !firstName.equals(this.firstName)) {
            tracker.track("firstName", this.firstName, firstName);
            this.firstName = firstName;
        }

        if (lastName != null && !lastName.equals(this.lastName)) {
            tracker.track("lastName", this.lastName, lastName);
            this.lastName = lastName;
        }

        if (profile != null && !profile.equals(this.profile)) {
            // Track specific profile fields
            if (this.profile != null) {
                if (!Objects.equals(this.profile.getBio(), profile.getBio())) {
                    tracker.track("profile.bio",
                            this.profile.getBio(),
                            profile.getBio());
                }
                if (!Objects.equals(this.profile.getPhoneNumber(), profile.getPhoneNumber())) {
                    tracker.track("profile.phoneNumber",
                            this.profile.getPhoneNumber(),
                            profile.getPhoneNumber());
                }
                if (!Objects.equals(this.profile.getDisplayName(), profile.getDisplayName())) {
                    tracker.track("profile.displayName",
                            this.profile.getDisplayName(),
                            profile.getDisplayName());
                }
                if (!Objects.equals(this.profile.getTimezone(), profile.getTimezone())) {
                    tracker.track("profile.timezone",
                            this.profile.getTimezone(),
                            profile.getTimezone());
                }
                if (!Objects.equals(this.profile.getLanguage(), profile.getLanguage())) {
                    tracker.track("profile.language",
                            this.profile.getLanguage(),
                            profile.getLanguage());
                }
            }
            this.profile = profile;
        }

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    /**
     * Update basic info (for admin operations)
     *
     * Events Published:
     * - EntityUpdatedEvent<User> (with change tracking)
     */
    public void updateBasicInfo(String firstName, String lastName, LocalDate dateOfBirth) {
        ChangeTracker tracker = new ChangeTracker();

        tracker.track("firstName", this.firstName, firstName);
        tracker.track("lastName", this.lastName, lastName);
        tracker.track("dateOfBirth", this.dateOfBirth, dateOfBirth);

        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;

        if (tracker.hasChanges()) {
            registerEvent(new EntityUpdatedEvent<>(this, tracker.getChanges()));
        }
    }

    // ====================================
    // Query Methods
    // ====================================

    @Transient
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>();

        if (roles != null) {
            roles.forEach(role -> {
                if (role.getPermissions() != null) {
                    allPermissions.addAll(role.getPermissions());
                }
            });
        }

        if (userGroup != null && userGroup.getRoles() != null) {
            userGroup.getRoles().forEach(role -> {
                if (role.getPermissions() != null) {
                    allPermissions.addAll(role.getPermissions());
                }
            });
        }

        return allPermissions;
    }

    public boolean hasPermission(String permissionName) {
        return getAllPermissions().stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    public boolean hasAnyRole(String... roleNames) {
        Set<String> userRoleNames = getAllRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return Arrays.stream(roleNames).anyMatch(userRoleNames::contains);
    }

    public boolean hasAllRoles(String... roleNames) {
        Set<String> userRoleNames = getAllRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return Arrays.stream(roleNames).allMatch(userRoleNames::contains);
    }

    @Transient
    public Set<Role> getAllRoles() {
        Set<Role> allRoles = new HashSet<>(this.roles);

        if (userGroup != null && userGroup.getRoles() != null) {
            allRoles.addAll(userGroup.getRoles());
        }

        return allRoles;
    }

    // ====================================
    // UserDetails Implementation
    // ====================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        getAllRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()))
        );

        getAllPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
        );

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email != null ? this.email.getValue() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        if (accountExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(accountExpiresAt);
    }

    @Override
    public boolean isAccountNonLocked() {
        if (!accountLocked) {
            return true;
        }
        return isTemporaryLockExpired();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (passwordExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(passwordExpiresAt);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled != null && this.enabled;
    }

    // ====================================
    // Principal Implementation
    // ====================================

    @Override
    public String getName() {
        return getUsername();
    }

    // ====================================
    // SoftDeletableAggregateRoot Implementation
    // ====================================

    @Override
    protected DomainEvent createDeletedEvent(String deletedBy) {
        return EntityDeletedEvent.builder()
                .entityClass(User.class)
                .entityType("User")
                .entityId(this.getId())
                .deletedBy(deletedBy)
                .softDelete(true)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    protected DomainEvent createRestoredEvent(String restoredBy, String previousDeletedBy) {
        return EntityRestoredEvent.builder()
                .entityClass(User.class)
                .entityType("User")
                .entityId(this.getId())
                .restoredBy(restoredBy)
                .previousDeletedBy(previousDeletedBy)
                .previousDeletedAt(this.getDeletedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    // ====================================
    // Helper Methods
    // ====================================

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Transient
    public String getDisplayName() {
        if (profile != null && profile.getDisplayName() != null) {
            return profile.getDisplayName();
        }
        return getFullName();
    }

    @Transient
    public UserStatus getStatus() {
        if (accountLocked) {
            return UserStatus.LOCKED;
        }
        if (!enabled) {
            return UserStatus.INACTIVE;
        }
        if (!emailVerified) {
            return UserStatus.PENDING_VERIFICATION;
        }
        return UserStatus.ACTIVE;
    }

    @Transient
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "system";
    }

    private static String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    // ====================================
    // Equals & HashCode
    // ====================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, email=%s, status=%s]",
                getId(),
                email != null ? email.getValue() : "null",
                getStatus()
        );
    }
}