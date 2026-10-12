package com.example.demo.modules.user.application.validator;

import com.example.demo.common.validator.CustomValidators;
import com.example.demo.modules.user.application.dto.request.AssignRoleRequest;
import com.example.demo.modules.user.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Validator for AssignRoleRequest
 * Validates role assignment operations
 *
 * @author Architecture Team
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class AssignRoleRequestValidator implements Validator {

    private final RoleRepository roleRepository;

    private static final int MAX_ROLES_PER_REQUEST = 20;
    private static final Set<String> SYSTEM_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_SYSTEM");

    @Override
    public boolean supports(Class<?> clazz) {
        return AssignRoleRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AssignRoleRequest request = (AssignRoleRequest) target;

        // Validate role IDs exist
        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            errors.rejectValue("roleIds", "roleIds.empty",
                    "At least one role ID must be provided");
            return;
        }

        // Validate maximum number of roles
        if (request.getRoleIds().size() > MAX_ROLES_PER_REQUEST) {
            errors.rejectValue("roleIds", "roleIds.tooMany",
                    String.format("Cannot assign more than %d roles at once", MAX_ROLES_PER_REQUEST));
            return;
        }

        // Validate UUID format for all role IDs
        validateRoleIdFormats(request.getRoleIds(), errors);

        if (errors.hasErrors()) {
            return; // Stop if format validation failed
        }

        // Validate roles exist in database
        validateRolesExist(request.getRoleIds(), errors);

        // Validate no duplicate role IDs
        validateNoDuplicates(request.getRoleIds(), errors);

        // Validate replace existing flag
        if (request.getReplaceExisting() == null) {
            errors.rejectValue("replaceExisting", "replaceExisting.null",
                    "Replace existing flag must be specified (true or false)");
        }

        // Additional business logic validations
        validateRoleAssignmentRules(request, errors);
    }

    /**
     * Extended validation with user context
     * Checks if user has permission to assign specific roles
     */
    public void validateWithUserContext(AssignRoleRequest request, UUID userId, String currentUserRole, Errors errors) {
        validate(request, errors);

        if (errors.hasErrors()) {
            return;
        }

        // Validate user cannot assign roles higher than their own
        validateRoleHierarchy(request.getRoleIds(), currentUserRole, errors);

        // Validate system roles can only be assigned by super admins
        validateSystemRoleAssignment(request.getRoleIds(), currentUserRole, errors);
    }

    // ====================================
    // Private Validation Methods
    // ====================================

    private void validateRoleIdFormats(Set<String> roleIds, Errors errors) {
        Set<String> invalidIds = new HashSet<>();

        for (String roleId : roleIds) {
            if (!CustomValidators.isValidUuid(roleId)) {
                invalidIds.add(roleId);
            }
        }

        if (!invalidIds.isEmpty()) {
            errors.rejectValue("roleIds", "roleIds.invalidFormat",
                    String.format("Invalid UUID format for role IDs: %s", String.join(", ", invalidIds)));
        }
    }

    private void validateRolesExist(Set<String> roleIds, Errors errors) {
        Set<UUID> uuidRoleIds = roleIds.stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());

        long existingCount = roleRepository.countByIdIn(uuidRoleIds);

        if (existingCount != roleIds.size()) {
            errors.rejectValue("roleIds", "roleIds.notFound",
                    String.format("One or more roles do not exist. Found %d out of %d roles",
                            existingCount, roleIds.size()));

            // Find which specific roles don't exist
            Set<UUID> existingIds = new HashSet<>();
            roleRepository.findAllById(uuidRoleIds).forEach(role -> existingIds.add(role.getId()));

            Set<String> missingIds = uuidRoleIds.stream()
                    .filter(id -> !existingIds.contains(id))
                    .map(UUID::toString)
                    .collect(java.util.stream.Collectors.toSet());

            errors.rejectValue("roleIds", "roleIds.missing",
                    String.format("Roles not found: %s", String.join(", ", missingIds)));
        }
    }

    private void validateNoDuplicates(Set<String> roleIds, Errors errors) {
        // Convert to lowercase for comparison
        Set<String> lowercaseIds = roleIds.stream()
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        if (lowercaseIds.size() < roleIds.size()) {
            errors.rejectValue("roleIds", "roleIds.duplicates",
                    "Duplicate role IDs detected in the request");
        }
    }

    private void validateRoleAssignmentRules(AssignRoleRequest request, Errors errors) {
        Set<UUID> uuidRoleIds = request.getRoleIds().stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());

        // Get actual role names
        var roles = roleRepository.findAllById(uuidRoleIds);
        Set<String> roleNames = roles.stream()
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet());

        // Check for conflicting roles
        validateNoConflictingRoles(roleNames, errors);

        // Check minimum role requirement
        if (Boolean.TRUE.equals(request.getReplaceExisting()) && roleNames.isEmpty()) {
            errors.rejectValue("roleIds", "roleIds.noRoles",
                    "User must have at least one role");
        }
    }

    private void validateNoConflictingRoles(Set<String> roleNames, Errors errors) {
        // Example: User cannot have both ROLE_ADMIN and ROLE_CUSTOMER
        boolean hasAdmin = roleNames.stream().anyMatch(r -> r.contains("ADMIN"));
        boolean hasCustomer = roleNames.stream().anyMatch(r -> r.contains("CUSTOMER"));

        if (hasAdmin && hasCustomer) {
            errors.rejectValue("roleIds", "roleIds.conflict",
                    "Cannot assign both admin and customer roles to the same user");
        }

        // Add more business-specific conflict rules here
    }

    private void validateRoleHierarchy(Set<String> roleIds, String currentUserRole, Errors errors) {
        if (currentUserRole == null) {
            return;
        }

        Set<UUID> uuidRoleIds = roleIds.stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());

        var rolesToAssign = roleRepository.findAllById(uuidRoleIds);

        // Get current user's highest priority
        var currentUserRoleEntity = roleRepository.findByName(currentUserRole);
        if (currentUserRoleEntity.isEmpty()) {
            return;
        }

        int currentUserPriority = currentUserRoleEntity.get().getPriority();

        // Check if trying to assign higher priority roles
        for (var role : rolesToAssign) {
            if (role.getPriority() > currentUserPriority) {
                errors.rejectValue("roleIds", "roleIds.hierarchyViolation",
                        String.format("Cannot assign role '%s' with higher priority than your own",
                                role.getName()));
                break;
            }
        }
    }

    private void validateSystemRoleAssignment(Set<String> roleIds, String currentUserRole, Errors errors) {
        Set<UUID> uuidRoleIds = roleIds.stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());

        var rolesToAssign = roleRepository.findAllById(uuidRoleIds);

        // Check if any system roles are being assigned
        Set<String> systemRolesBeingAssigned = rolesToAssign.stream()
                .filter(role -> SYSTEM_ROLES.contains(role.getName()))
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet());

        if (!systemRolesBeingAssigned.isEmpty()) {
            // Only ROLE_SUPER_ADMIN can assign system roles
            if (!"ROLE_SUPER_ADMIN".equals(currentUserRole)) {
                errors.rejectValue("roleIds", "roleIds.systemRoleRestricted",
                        String.format("Only super admins can assign system roles: %s",
                                String.join(", ", systemRolesBeingAssigned)));
            }
        }
    }
}