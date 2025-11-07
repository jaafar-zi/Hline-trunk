package com.example.demo.domain.productManagementDomain.controller;

import com.example.demo.domain.productManagementDomain.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PutMapping("/update")
    public ResponseEntity<String> updateStock(@RequestParam Long productId, @RequestParam int quantity) {
        inventoryService.updateStock(productId, quantity);
        return ResponseEntity.ok("Stock updated successfully!");
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveStock(@RequestParam Long productId, @RequestParam int quantity) {
        boolean reserved = inventoryService.reserveStock(productId, quantity);
        if (reserved) {
            return ResponseEntity.ok("Stock reserved successfully!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough stock available!");
    }

    @PostMapping("/deduct")
    public ResponseEntity<String> deductStock(@RequestParam Long productId, @RequestParam int quantity) {
        try {
            inventoryService.deductStock(productId, quantity);
            return ResponseEntity.ok("Stock deducted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkStock(@RequestParam Long productId, @RequestParam int quantity) {
        boolean isAvailable = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/reorder")
    public ResponseEntity<List<Long>> getProductsNeedingReorder() {
        List<Long> productsNeedingReorder = inventoryService.findProductsNeedingReorder();
        return ResponseEntity.ok(productsNeedingReorder);
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseReservedStock(@RequestParam Long productId, @RequestParam int quantity) {
        try {
            inventoryService.releaseReservedStock(productId, quantity);
            return ResponseEntity.ok("Reserved stock released successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

