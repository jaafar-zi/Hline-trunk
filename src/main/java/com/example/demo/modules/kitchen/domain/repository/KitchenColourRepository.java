package com.example.demo.modules.kitchen.domain.repository;

import com.example.demo.common.base.repository.SoftDeleteRepository;
import com.example.demo.modules.kitchen.domain.model.KitchenColour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Colour Repository
 */
@Repository
public interface KitchenColourRepository extends SoftDeleteRepository<KitchenColour, UUID> {

    // ====================================
    // Find by Code / Name
    // ====================================

    @Query("SELECT c FROM KitchenColour c WHERE c.code = :code AND c.deleted = false")
    Optional<KitchenColour> findByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM KitchenColour c WHERE c.code = :code AND c.deleted = false")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM KitchenColour c WHERE c.code = :code AND c.id <> :id AND c.deleted = false")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") UUID id);

    @Query("SELECT c FROM KitchenColour c " +
           "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.deleted = false")
    List<KitchenColour> findByNameContaining(@Param("name") String name);

    // ====================================
    // Find Active
    // ====================================

    @Query("SELECT c FROM KitchenColour c WHERE c.active = true AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<KitchenColour> findAllActive();

    @Query("SELECT c FROM KitchenColour c WHERE c.active = true AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    Page<KitchenColour> findAllActive(Pageable pageable);

    // ====================================
    // Find by Collection (door colour)
    // ====================================

    @Query("SELECT col.colour FROM KitchenCollection col " +
           "WHERE col.id = :collectionId AND col.deleted = false")
    Optional<KitchenColour> findByCollectionId(@Param("collectionId") UUID collectionId);

    // ====================================
    // Find available cabinet colours for a collection
    // ====================================

    /**
     * Fixed query:
     *
     * The M2M join table is on KitchenCollection.availableCabinetColours.
     * We must start FROM KitchenCollection, JOIN its collection,
     * then SELECT the colour elements.
     *
     * BEFORE (broken):
     *   SELECT c FROM KitchenColour c
     *   JOIN col.availableCabinetColours c   ← alias conflict + wrong FROM
     *   FROM KitchenCollection col
     *
     * AFTER (correct):
     *   SELECT c FROM KitchenCollection col
     *   JOIN col.availableCabinetColours c
     *   WHERE col.id = :collectionId
     */
    @Query("SELECT c FROM KitchenCollection col " +
           "JOIN col.availableCabinetColours c " +
           "WHERE col.id = :collectionId " +
           "AND col.deleted = false " +
           "AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<KitchenColour> findAvailableCabinetColoursByCollectionId(
        @Param("collectionId") UUID collectionId
    );

    // ====================================
    // Statistics
    // ====================================

    @Query("SELECT COUNT(c) FROM KitchenColour c WHERE c.active = true AND c.deleted = false")
    long countActive();
}