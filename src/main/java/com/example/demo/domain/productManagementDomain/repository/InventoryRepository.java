package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.dto.InventoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Inventory;
import com.example.demo.domain.productManagementDomain.projection.InventoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing inventory-related database operations.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Retrieves an inventory record based on the associated product ID.
     *
     * @param productId The ID of the product.
     * @return An Optional containing the Inventory record if found.
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findInventoryByProductId(@Param("productId") Long productId);

    /**
     * Retrieves an InventoryDTO for a given product ID.
     *
     * @param productId The ID of the product.
     * @return An Optional containing the InventoryDTO if found.
     */
    @Query("SELECT new com.example.demo.productManagementDomain.dto.InventoryDTO(i.id, i.product.id, i.quantityInStock, i.reservedQuantity, i.reorderThreshold, i.reorderQuantity, i.lastUpdated, i.lastRestocked) " +
            "FROM Inventory i WHERE i.product.id = :productId")
    Optional<InventoryDTO> findByProductId(@Param("productId") Long productId);

    /**
     * Retrieves the total quantity of all products in stock.
     *
     * @return The total quantity in stock.
     */
    @Query("SELECT SUM(i.quantityInStock) FROM Inventory i")
    Long getAllProductsInStock();

    /**
     * Retrieves the stock quantity for a specific product.
     *
     * @param productId The ID of the product.
     * @return The quantity in stock for the given product ID.
     */
    @Query("SELECT i.quantityInStock FROM Inventory i WHERE i.product.id = :productId")
    Long getStockByProductId(@Param("productId") Long productId);

    /**
     * Retrieves an InventoryDTO by inventory ID.
     *
     * @param id The ID of the inventory record.
     * @return An Optional containing the InventoryDTO if found.
     */
    @Query("SELECT new com.example.demo.productManagementDomain.dto.InventoryDTO(i.id, i.product.id, i.quantityInStock, i.reservedQuantity, i.reorderThreshold, i.reorderQuantity, i.lastUpdated, i.lastRestocked) " +
            "FROM Inventory i WHERE i.id = :inventoryId")
    Optional<InventoryDTO> findInventoryById(@Param("inventoryId") long id);

    /**
     * Retrieves all inventory records as InventoryDTOs.
     *
     * @return A list of InventoryDTOs.
     */
    @Query("SELECT new com.example.demo.productManagementDomain.dto.InventoryDTO(i.id, i.product.id, i.quantityInStock, i.reservedQuantity, i.reorderThreshold, i.reorderQuantity, i.lastUpdated, i.lastRestocked) FROM Inventory i")
    List<InventoryDTO> findAllInventories();

    /**
     * Updates the stock quantity for a given product ID and updates the last updated timestamp.
     *
     * @param productId       The ID of the product.
     * @param quantityInStock The quantity to add to the existing stock.
     * @return The number of affected rows.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityInStock = i.quantityInStock + :quantityInStock, i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId")
    int updateStock(@Param("productId") long productId, @Param("quantityInStock") int quantityInStock);

    /**
     * Reserves stock for a given product if enough stock is available.
     *
     * @param productId The ID of the product.
     * @param quantity  The quantity to reserve.
     * @return The number of affected rows.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity, i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId AND (i.quantityInStock - i.reservedQuantity) >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Releases reserved stock for a given product if enough stock is reserved.
     *
     * @param productId The ID of the product.
     * @param quantity  The quantity to release.
     * @return The number of affected rows.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity, i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId AND i.reservedQuantity >= :quantity")
    int releaseReservedStock(@Param("productId") long productId, @Param("quantity") int quantity);

    /**
     * Deducts stock and reserved quantity for a given product if enough stock is reserved.
     *
     * @param productId The ID of the product.
     * @param quantity  The quantity to deduct.
     * @return The number of affected rows.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityInStock = i.quantityInStock - :quantity, i.reservedQuantity = i.reservedQuantity - :quantity, i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId AND i.reservedQuantity >= :quantity")
    int deductStock(@Param("productId") long productId, @Param("quantity") int quantity);

    /**
     * Finds products that need reordering based on the reorder threshold.
     *
     * @param reorderThreshold The threshold value to check against.
     * @return A list of InventoryProjection containing product IDs and stock quantities.
     */
    @Query("SELECT i.id AS id, i.quantityInStock AS stockQuantity FROM Inventory i WHERE i.quantityInStock <= :reorderThreshold")
    List<InventoryProjection> findProductsNeedingReorder(@Param("reorderThreshold") int reorderThreshold);

    /**
     * Updates the lastUpdated timestamp for a given inventory record.
     *
     * @param inventoryId The ID of the inventory record.
     * @param lastUpdated The new timestamp.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.lastUpdated = :lastUpdated WHERE i.id = :inventoryId")
    void updateLastUpdated(@Param("inventoryId") Long inventoryId, @Param("lastUpdated") LocalDateTime lastUpdated);
}
