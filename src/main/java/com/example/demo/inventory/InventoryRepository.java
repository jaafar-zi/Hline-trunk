package com.example.demo.inventory;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Finds an Inventory record by its associated Product ID.
     *
     * @param productId The ID of the product.
     * @return An Optional containing the Inventory record if found.
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Finds all Inventory records where the stock level is below the reorder threshold.
     *
     * @return A list of Inventory records that need reordering.
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock < i.reorderThreshold")
    List<Inventory> findInventoriesNeedingReorder();

    /**
     * Updates the lastUpdated timestamp for an Inventory record.
     *
     * @param inventoryId The ID of the inventory record.
     * @param lastUpdated The new timestamp.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.lastUpdated = :lastUpdated WHERE i.id = :inventoryId")
    void updateLastUpdated(@Param("inventoryId") Long inventoryId, @Param("lastUpdated") LocalDateTime lastUpdated);
}
