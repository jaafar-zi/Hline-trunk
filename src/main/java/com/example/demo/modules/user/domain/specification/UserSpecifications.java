package com.example.demo.modules.user.domain.specification;

import com.example.demo.modules.user.domain.model.User;
import com.example.demo.modules.user.domain.model.vo.UserStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User Specifications for dynamic query building
 *
 * Uses JPA Criteria API for type-safe, composable queries
 *
 * @author Architecture Team
 * @version 1.0
 */
public class UserSpecifications {

    /**
     * Find by email (case-insensitive)
     */
    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("email").get("value")), email.toLowerCase());
        };
    }

    /**
     * Find by first name (case-insensitive, like)
     */
    public static Specification<User> hasFirstNameLike(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }

    /**
     * Find by last name (case-insensitive, like)
     */
    public static Specification<User> hasLastNameLike(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }

    /**
     * Find by enabled status
     */
    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, cb) -> {
            if (enabled == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("enabled"), enabled);
        };
    }

    /**
     * Find by locked status
     */
    public static Specification<User> isLocked(Boolean locked) {
        return (root, query, cb) -> {
            if (locked == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("accountLocked"), locked);
        };
    }

    /**
     * Find by email verified status
     */
    public static Specification<User> isEmailVerified(Boolean verified) {
        return (root, query, cb) -> {
            if (verified == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("emailVerified"), verified);
        };
    }

    /**
     * Find by user status
     */
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            switch (status) {
                case ACTIVE:
                    predicates.add(cb.equal(root.get("enabled"), true));
                    predicates.add(cb.equal(root.get("accountLocked"), false));
                    predicates.add(cb.equal(root.get("emailVerified"), true));
                    break;
                case INACTIVE:
                    predicates.add(cb.equal(root.get("enabled"), false));
                    break;
                case LOCKED:
                    predicates.add(cb.equal(root.get("accountLocked"), true));
                    break;
                case PENDING_VERIFICATION:
                    predicates.add(cb.equal(root.get("emailVerified"), false));
                    break;
                default:
                    return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Find by role name
     */
    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.isBlank()) {
                return cb.conjunction();
            }
            var roles = root.join("roles", JoinType.LEFT);
            return cb.equal(roles.get("name"), roleName);
        };
    }

    /**
     * Find by any of the roles
     */
    public static Specification<User> hasAnyRole(List<String> roleNames) {
        return (root, query, cb) -> {
            if (roleNames == null || roleNames.isEmpty()) {
                return cb.conjunction();
            }
            var roles = root.join("roles", JoinType.LEFT);
            return roles.get("name").in(roleNames);
        };
    }

    /**
     * Find by user group ID
     */
    public static Specification<User> inUserGroup(UUID groupId) {
        return (root, query, cb) -> {
            if (groupId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("userGroup").get("id"), groupId);
        };
    }

    /**
     * Find users created after date
     */
    public static Specification<User> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Find users created before date
     */
    public static Specification<User> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Find users who logged in after date
     */
    public static Specification<User> loggedInAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("lastLoginAt"), date);
        };
    }

    /**
     * Find users who never logged in
     */
    public static Specification<User> neverLoggedIn() {
        return (root, query, cb) -> cb.isNull(root.get("lastLoginAt"));
    }

    /**
     * Search by keyword (name or email)
     */
    public static Specification<User> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern),
                    cb.like(cb.lower(root.get("lastName")), likePattern),
                    cb.like(cb.lower(root.get("email").get("value")), likePattern)
            );
        };
    }

    /**
     * Combine multiple specifications with AND
     */
    public static Specification<User> combine(Specification<User>... specs) {
        Specification<User> result = Specification.where(null);
        for (Specification<User> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}