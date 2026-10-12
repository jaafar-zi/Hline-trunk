package com.example.demo.common.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for soft-deletable entities
 *
 * Features:
 * - Find including deleted entities
 * - Find only deleted entities
 * - Restore deleted entities
 * - Hard delete (permanent removal)
 * - Bulk operations
 *
 * @param <T> Entity type extending SoftDeletableEntity
 * @param <ID> ID type (should be UUID)
 *
 * @author Architecture Team
 * @version 2.0
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends BaseRepository<T, ID> {

    // ====================================
    // Find Including Deleted
    // ====================================

    /**
     * Find by ID including soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdIncludingDeleted(@Param("id") ID id);

    /**
     * Find all including soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e")
    List<T> findAllIncludingDeleted();

    /**
     * Find all including soft-deleted with pagination
     */
    @Query("SELECT e FROM #{#entityName} e")
    Page<T> findAllIncludingDeleted(Pageable pageable);

    /**
     * Count all including deleted
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long countAllIncludingDeleted();

    // ====================================
    // Find Only Deleted
    // ====================================

    /**
     * Find only soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findDeleted();

    /**
     * Find only soft-deleted entities with pagination
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    Page<T> findDeleted(Pageable pageable);

    /**
     * Find deleted within date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true " +
            "AND e.deletedAt BETWEEN :startDate AND :endDate")
    List<T> findDeletedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find deleted by user
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true AND e.deletedBy = :deletedBy")
    List<T> findDeletedByUser(@Param("deletedBy") String deletedBy);

    /**
     * Count deleted entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = true")
    long countDeleted();

    // ====================================
    // Restore Operations
    // ====================================

    /**
     * Restore soft-deleted entity by ID
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null, " +
            "e.deletedBy = null WHERE e.id = :id AND e.deleted = true")
    int restore(@Param("id") ID id);

    /**
     * Restore multiple entities
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null, " +
            "e.deletedBy = null WHERE e.id IN :ids AND e.deleted = true")
    int restoreAll(@Param("ids") List<ID> ids);

    /**
     * Restore all deleted entities
     * Warning: Use with extreme caution
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null, " +
            "e.deletedBy = null WHERE e.deleted = true")
    int restoreAll();

    // ====================================
    // Hard Delete Operations
    // ====================================

    /**
     * Hard delete (permanent deletion) by ID
     * Only works for already soft-deleted entities
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id AND e.deleted = true")
    int hardDelete(@Param("id") ID id);

    /**
     * Hard delete multiple entities
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id IN :ids AND e.deleted = true")
    int hardDeleteAll(@Param("ids") List<ID> ids);

    /**
     * Hard delete all soft-deleted entities older than specified date
     * Useful for cleanup jobs
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true AND e.deletedAt < :beforeDate")
    int hardDeleteOlderThan(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Permanently delete all soft-deleted entities
     * Warning: This is irreversible!
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true")
    int hardDeleteAllDeleted();

    // ====================================
    // Utility Methods
    // ====================================

    /**
     * Check if entity is deleted
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM #{#entityName} e WHERE e.id = :id AND e.deleted = true")
    boolean isDeleted(@Param("id") ID id);

    /**
     * Find IDs of all deleted entities
     */
    @Query("SELECT e.id FROM #{#entityName} e WHERE e.deleted = true")
    List<ID> findAllDeletedIds();
}