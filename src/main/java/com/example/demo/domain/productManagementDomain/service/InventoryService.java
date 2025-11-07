package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.InventoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Inventory;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.projection.InventoryProjection;
import com.example.demo.domain.productManagementDomain.repository.InventoryRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductRepository productRepository;


    public Inventory addInventory(Long productId, int quantity, int reservedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(quantity);
        inventory.setReservedQuantity(reservedQuantity);
        inventory.setLastUpdated(LocalDateTime.now());

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public boolean updateStock(Long productId, int quantityInStock) {
        int updatedRows = inventoryRepository.updateStock(productId, quantityInStock);

        return updatedRows > 0;
    }

    @Transactional
    public boolean reserveStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.reserveStock(productId, quantity);

        return updatedRows > 0;
    }


    @Transactional
    public void releaseReservedStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.releaseReservedStock(productId, quantity);

    }



    @Transactional
    public void deductStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.deductStock(productId, quantity);

    }



    public List<Long> findProductsNeedingReorder() {
        int reorderThreshold = 10;
        return inventoryRepository.findProductsNeedingReorder(reorderThreshold).stream()
                .map(InventoryProjection::getId).collect(Collectors.toList());
    }


    public boolean isStockAvailable(Long productId, int quantity) {
        return inventoryRepository.getStockByProductId(productId) >= quantity;
    }


    public InventoryDTO getStockDetails(Long productId) {
        return inventoryRepository.findInventoryById(productId).orElseThrow(() -> new RuntimeException("Inventory not found"));
    }



    public String deleteInventory(Long productId) {
        Inventory inventory = inventoryRepository.findInventoryByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventoryRepository.delete(inventory);
        return "Inventory for product " + productId + " deleted successfully";
    }
}

