package com.example.demo.inventory;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {


    private InventoryRepository inventoryRepository;

    // Update stock levels
    public void updateStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventory.setQuantityInStock(inventory.getQuantityInStock() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    // Reserve stock for an order
    public boolean reserveStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        if (inventory.getQuantityInStock() - inventory.getReservedQuantity() >= quantity) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            return true;
        }
        return false; // Not enough stock available
    }

    // Find products needing reorder
    public List<Inventory> findProductsNeedingReorder() {
        // Fetch all inventory items from the repository
        List<Inventory> allInventories = inventoryRepository.findAll();

        // Define the reorder threshold (you can make this configurable if needed)
        int reorderThreshold = 10; // Example threshold

        // Filter inventories where quantityInStock is less than or equal to the reorderThreshold

        return allInventories.stream()
                .filter(inventory -> inventory.getQuantityInStock() <= reorderThreshold)
                .collect(Collectors.toList());
    }

    // Check stock availability
    public boolean isStockAvailable(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        return inventory.getQuantityInStock() - inventory.getReservedQuantity() >= quantity;
    }
}
