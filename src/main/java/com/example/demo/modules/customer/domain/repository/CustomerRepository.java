package com.example.demo.modules.customer.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.customer.domain.model.Customer;
import com.example.demo.modules.customer.domain.model.vo.CustomerStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Customer aggregate
 * 
 * Extends:
 * - SoftDeleteRepository for soft delete operations
 * - JpaSpecificationExecutor for complex queries using Specifications
 * 
 * All default queries automatically exclude soft-deleted customers
 * Use *IncludingDeleted methods to include soft-deleted entities
 * 
 * @author Customer Domain Team
 */
@Repository
public interface CustomerRepository extends 
        SoftDeleteRepository<Customer, UUID>, 
        JpaSpecificationExecutor<Customer> {

    // ========== Basic Queries (Auto-exclude deleted) ==========

    /**
     * Find customer by email (case-insensitive)
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.email.value) = LOWER(:email) AND c.deleted = false")
    Optional<Customer> findByEmail(@Param("email") String email);

    /**
     * Find customer by email including deleted
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.email.value) = LOWER(:email)")
    Optional<Customer> findByEmailIncludingDeleted(@Param("email") String email);

    /**
     * Check if email exists (case-insensitive)
     * Excludes soft-deleted customers
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE LOWER(c.email.value) = LOWER(:email) AND c.deleted = false")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Check if email exists including deleted customers
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE LOWER(c.email.value) = LOWER(:email)")
    boolean existsByEmailIncludingDeleted(@Param("email") String email);

    /**
     * Find customer by phone
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.phone.value = :phone AND c.deleted = false")
    Optional<Customer> findByPhone(@Param("phone") String phone);

    // ========== Status Queries (Auto-exclude deleted) ==========

    /**
     * Find all customers by status
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.status = :status AND c.deleted = false")
    List<Customer> findByStatus(@Param("status") CustomerStatus status);

    /**
     * Count customers by status
     * Excludes soft-deleted customers
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status AND c.deleted = false")
    long countByStatus(@Param("status") CustomerStatus status);

    /**
     * Find active customers only
     */
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' AND c.deleted = false")
    List<Customer> findAllActive();

    /**
     * Find inactive customers only
     */
    @Query("SELECT c FROM Customer c WHERE c.status = 'INACTIVE' AND c.deleted = false")
    List<Customer> findAllInactive();

    /**
     * Find suspended customers only
     */
    @Query("SELECT c FROM Customer c WHERE c.status = 'SUSPENDED' AND c.deleted = false")
    List<Customer> findAllSuspended();

    /**
     * Find blocked customers only
     */
    @Query("SELECT c FROM Customer c WHERE c.status = 'BLOCKED' AND c.deleted = false")
    List<Customer> findAllBlocked();

    // ========== Name Search Queries (Auto-exclude deleted) ==========

    /**
     * Find customers by name containing (searches first and last name)
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(LOWER(c.name.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.name.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(CONCAT(c.name.firstName, ' ', c.name.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND c.deleted = false")
    List<Customer> findByNameContaining(@Param("name") String name);

    /**
     * Find customers by first name
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) " +
           "AND c.deleted = false")
    List<Customer> findByFirstNameContaining(@Param("firstName") String firstName);

    /**
     * Find customers by last name
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) " +
           "AND c.deleted = false")
    List<Customer> findByLastNameContaining(@Param("lastName") String lastName);

    // ========== Date Range Queries (Auto-exclude deleted) ==========

    /**
     * Find customers registered after specific date
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :date AND c.deleted = false")
    List<Customer> findByCreatedAtAfter(@Param("date") LocalDateTime date);

    /**
     * Find customers registered between dates
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.deleted = false")
    List<Customer> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recently registered customers (last N days)
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :sinceDate AND c.deleted = false")
    List<Customer> findRecentlyRegistered(@Param("sinceDate") LocalDateTime sinceDate);

    // ========== Preferences Queries (Auto-exclude deleted) ==========

    /**
     * Find customers with specific language preference
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.preferences.language = :language AND c.deleted = false")
    List<Customer> findByLanguage(@Param("language") String language);

    /**
     * Find customers with specific currency preference
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.preferences.currency = :currency AND c.deleted = false")
    List<Customer> findByCurrency(@Param("currency") String currency);

    /**
     * Find customers with marketing emails enabled
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.preferences.marketingEmails = true AND c.deleted = false")
    List<Customer> findMarketingOptIns();

    /**
     * Find customers with email notifications enabled
     * Excludes soft-deleted customers
     */
    @Query("SELECT c FROM Customer c WHERE c.preferences.emailNotifications = true AND c.deleted = false")
    List<Customer> findEmailNotificationOptIns();

    // ========== Statistics Queries ==========

    /**
     * Count total active customers (excluding deleted)
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.deleted = false")
    long countActive();

    /**
     * Count customers by status (excluding deleted)
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status AND c.deleted = false")
    long countActiveByStatus(@Param("status") CustomerStatus status);

    /**
     * Count customers registered in date range
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.deleted = false")
    long countRegisteredBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count customers by status group
     */
    @Query("SELECT c.status, COUNT(c) FROM Customer c WHERE c.deleted = false GROUP BY c.status")
    List<Object[]> countByStatusGroup();

    // ========== Business Logic Queries ==========

    /**
     * Find customers who can place orders (active, not deleted, has addresses)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.status = 'ACTIVE' AND " +
           "c.deleted = false AND " +
           "SIZE(c.addresses) > 0")
    List<Customer> findEligibleForOrders();

    /**
     * Find customers eligible for reactivation campaign
     * (inactive but recently active, within last N days)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.status = 'INACTIVE' AND " +
           "c.deleted = false AND " +
           "c.updatedAt >= :cutoffDate")
    List<Customer> findEligibleForReactivation(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find customers without any addresses
     */
    @Query("SELECT c FROM Customer c WHERE c.deleted = false AND SIZE(c.addresses) = 0")
    List<Customer> findWithoutAddresses();

    /**
     * Find customers with multiple addresses
     */
    @Query("SELECT c FROM Customer c WHERE c.deleted = false AND SIZE(c.addresses) > 1")
    List<Customer> findWithMultipleAddresses();

    // ========== Soft Delete Specific Queries ==========

    /**
     * Find customers deleted by specific user
     */
    @Query("SELECT c FROM Customer c WHERE c.deleted = true AND c.deletedBy = :deletedBy")
    List<Customer> findDeletedByUser(@Param("deletedBy") String deletedBy);

    /**
     * Find customers deleted within date range
     */
    @Query("SELECT c FROM Customer c WHERE c.deleted = true " +
           "AND c.deletedAt BETWEEN :startDate AND :endDate")
    List<Customer> findDeletedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find customers eligible for permanent deletion (deleted > N days ago)
     */
    @Query("SELECT c FROM Customer c WHERE c.deleted = true AND c.deletedAt < :beforeDate")
    List<Customer> findEligibleForPermanentDeletion(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Count customers deleted in the last N days
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.deleted = true AND c.deletedAt >= :sinceDate")
    long countRecentlyDeleted(@Param("sinceDate") LocalDateTime sinceDate);
}