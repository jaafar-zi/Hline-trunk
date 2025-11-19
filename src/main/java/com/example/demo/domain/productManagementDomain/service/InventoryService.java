package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.domain.productManagementDomain.dto.InventoryDTO;
import com.example.demo.domain.productManagementDomain.entity.Inventory;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.projection.InventoryProjection;
import com.example.demo.domain.productManagementDomain.repository.InventoryRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService extends AbstractBaseService<Inventory, Long> {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository) {
        super(inventoryRepository);
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    protected String getCacheName() {
        return "inventories";
    }

    @Override
    protected Class<?> getEntityClass() {
        return Inventory.class;
    }

    // -----------------------
    // DOMAIN-SPECIFIC METHODS
    // -----------------------

    public Inventory addInventory(Long productId, int quantity, int reservedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(Product.class, productId));

        Inventory inventory = new Inventory(product,quantity,reservedQuantity,LocalDateTime.now());
        inventory.setProduct(product);
        inventory.setQuantityInStock(quantity);
        inventory.setReservedQuantity(reservedQuantity);
        inventory.setLastUpdated(LocalDateTime.now());

        return save(inventory);
    }

    public boolean updateStock(Long productId, int quantityInStock) {
        int updatedRows = inventoryRepository.updateStock(productId, quantityInStock);
        return updatedRows > 0;
    }

    public boolean reserveStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.reserveStock(productId, quantity);
        return updatedRows > 0;
    }

    public void releaseReservedStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.releaseReservedStock(productId, quantity);
        if (updatedRows == 0) {
            throw new RuntimeException("No reserved stock to release for product ID: " + productId);
        }
    }

    public void deductStock(Long productId, int quantity) {
        int updatedRows = inventoryRepository.deductStock(productId, quantity);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to deduct stock for product ID: " + productId);
        }
    }

    public List<Long> findProductsNeedingReorder() {
        int reorderThreshold = 10;
        return inventoryRepository.findProductsNeedingReorder(reorderThreshold)
                .stream()
                .map(InventoryProjection::getId)
                .collect(Collectors.toList());
    }

    public boolean isStockAvailable(Long productId, int quantity) {
        Long available = inventoryRepository.getStockByProductId(productId);
        return available != null && available >= quantity;
    }

    public InventoryDTO getStockDetails(Long productId) {
        return inventoryRepository.findInventoryById(productId)
                .orElseThrow(() -> new EntityNotFoundException(Inventory.class, productId));
    }

    public String deleteInventory(Long productId) {
        Inventory inventory = inventoryRepository.findInventoryByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException(Inventory.class, productId));
        delete(inventory.getId());
        return "Inventory for product " + productId + " deleted successfully";
    }

    @Override
    protected void onAfterSave(Inventory entity) {
        entity.setLastUpdated(LocalDateTime.now());
    }
}


