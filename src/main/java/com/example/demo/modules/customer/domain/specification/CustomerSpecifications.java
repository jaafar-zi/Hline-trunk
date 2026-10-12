package com.example.demo.modules.customer.domain.specification;

import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * JPA Specifications for Customer queries
 * 
 * Provides reusable query predicates for complex customer searches
 * 
 * Usage:
 * <pre>
 * Specification<Customer> spec = CustomerSpecifications
 *     .hasStatus(CustomerStatus.ACTIVE)
 *     .and(CustomerSpecifications.emailContains("@gmail.com"))
 *     .and(CustomerSpecifications.isNotDeleted());
 * 
 * List<Customer> customers = customerRepository.findAll(spec);
 * </pre>
 * 
 * @author Customer Domain Team
 */
public final class CustomerSpecifications {

    /**
     * Private constructor to prevent instantiation
     */
    private CustomerSpecifications() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== Status Specifications ==========

    /**
     * Customers with specific status
     */
    public static Specification<Customer> hasStatus(CustomerStatus status) {
        return (root, query, cb) -> 
            status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * Active customers only
     */
    public static Specification<Customer> isActive() {
        return hasStatus(CustomerStatus.ACTIVE);
    }

    /**
     * Inactive customers only
     */
    public static Specification<Customer> isInactive() {
        return hasStatus(CustomerStatus.INACTIVE);
    }

    /**
     * Suspended customers only
     */
    public static Specification<Customer> isSuspended() {
        return hasStatus(CustomerStatus.SUSPENDED);
    }

    /**
     * Blocked customers only
     */
    public static Specification<Customer> isBlocked() {
        return hasStatus(CustomerStatus.BLOCKED);
    }

    // ========== Soft Delete Specifications ==========

    /**
     * Only non-deleted customers
     */
    public static Specification<Customer> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    /**
     * Only deleted customers
     */
    public static Specification<Customer> isDeleted() {
        return (root, query, cb) -> cb.isTrue(root.get("deleted"));
    }

    /**
     * Include deleted customers (override default filter)
     */
    public static Specification<Customer> includeDeleted() {
        return (root, query, cb) -> cb.conjunction(); // Always true
    }

    // ========== Name & Email Specifications ==========

    /**
     * Customers with email containing text (case-insensitive)
     */
    public static Specification<Customer> emailContains(String email) {
        return (root, query, cb) -> 
            email == null || email.isBlank() 
                ? cb.conjunction() 
                : cb.like(cb.lower(root.get("email").get("value")), 
                         "%" + email.toLowerCase() + "%");
    }

    /**
     * Customers with exact email
     */
    public static Specification<Customer> hasEmail(String email) {
        return (root, query, cb) -> 
            email == null || email.isBlank() 
                ? cb.conjunction() 
                : cb.equal(cb.lower(root.get("email").get("value")), 
                          email.toLowerCase());
    }

    /**
     * Customers with first name containing text (case-insensitive)
     */
    public static Specification<Customer> firstNameContains(String firstName) {
        return (root, query, cb) -> 
            firstName == null || firstName.isBlank() 
                ? cb.conjunction() 
                : cb.like(cb.lower(root.get("name").get("firstName")), 
                         "%" + firstName.toLowerCase() + "%");
    }

    /**
     * Customers with last name containing text (case-insensitive)
     */
    public static Specification<Customer> lastNameContains(String lastName) {
        return (root, query, cb) -> 
            lastName == null || lastName.isBlank() 
                ? cb.conjunction() 
                : cb.like(cb.lower(root.get("name").get("lastName")), 
                         "%" + lastName.toLowerCase() + "%");
    }

    /**
     * Customers with full name containing text (searches both first and last name)
     */
    public static Specification<Customer> fullNameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name").get("firstName")), pattern),
                cb.like(cb.lower(root.get("name").get("lastName")), pattern),
                cb.like(cb.lower(
                    cb.concat(
                        cb.concat(root.get("name").get("firstName"), " "),
                        root.get("name").get("lastName")
                    )
                ), pattern)
            );
        };
    }

    /**
     * Customers with phone containing text
     */
    public static Specification<Customer> phoneContains(String phone) {
        return (root, query, cb) -> 
            phone == null || phone.isBlank() 
                ? cb.conjunction() 
                : cb.like(root.get("phone").get("value"), "%" + phone + "%");
    }

    // ========== Date Range Specifications ==========

    /**
     * Customers created after specific date
     */
    public static Specification<Customer> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> 
            date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    /**
     * Customers created before specific date
     */
    public static Specification<Customer> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> 
            date == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    /**
     * Customers created between two dates
     */
    public static Specification<Customer> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return cb.conjunction();
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("createdAt"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    /**
     * Customers updated after specific date
     */
    public static Specification<Customer> updatedAfter(LocalDateTime date) {
        return (root, query, cb) -> 
            date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("updatedAt"), date);
    }

    // ========== Preferences Specifications ==========

    /**
     * Customers with specific language preference
     */
    public static Specification<Customer> hasLanguage(String language) {
        return (root, query, cb) -> 
            language == null || language.isBlank() 
                ? cb.conjunction() 
                : cb.equal(root.get("preferences").get("language"), language);
    }

    /**
     * Customers with specific currency preference
     */
    public static Specification<Customer> hasCurrency(String currency) {
        return (root, query, cb) -> 
            currency == null || currency.isBlank() 
                ? cb.conjunction() 
                : cb.equal(root.get("preferences").get("currency"), currency);
    }

    /**
     * Customers who opted in for email notifications
     */
    public static Specification<Customer> hasEmailNotificationsEnabled() {
        return (root, query, cb) -> 
            cb.isTrue(root.get("preferences").get("emailNotifications"));
    }

    /**
     * Customers who opted in for marketing emails
     */
    public static Specification<Customer> hasMarketingEmailsEnabled() {
        return (root, query, cb) -> 
            cb.isTrue(root.get("preferences").get("marketingEmails"));
    }

    // ========== Address Specifications ==========

    /**
     * Customers with at least one address
     */
    public static Specification<Customer> hasAddresses() {
        return (root, query, cb) -> 
            cb.isNotEmpty(root.get("addresses"));
    }

    /**
     * Customers without any addresses
     */
    public static Specification<Customer> hasNoAddresses() {
        return (root, query, cb) -> 
            cb.isEmpty(root.get("addresses"));
    }

    // ========== Composite Specifications ==========

    /**
     * Active customers who can place orders
     */
    public static Specification<Customer> canPlaceOrder() {
        return isActive()
            .and(isNotDeleted())
            .and(hasAddresses());
    }

    /**
     * Customers eligible for marketing
     */
    public static Specification<Customer> eligibleForMarketing() {
        return isActive()
            .and(isNotDeleted())
            .and(hasMarketingEmailsEnabled());
    }

    /**
     * Recently registered customers (last 30 days)
     */
    public static Specification<Customer> recentlyRegistered() {
        return createdAfter(LocalDateTime.now().minusDays(30))
            .and(isNotDeleted());
    }

    /**
     * Inactive customers for reactivation campaign
     */
    public static Specification<Customer> eligibleForReactivation() {
        return isInactive()
            .and(isNotDeleted())
            .and(updatedAfter(LocalDateTime.now().minusDays(90))); // Inactive but recent
    }
}