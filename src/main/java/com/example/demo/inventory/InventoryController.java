package com.example.demo.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PutMapping("/update-stock/{productId}")
    public ResponseEntity<Void> updateStock(@PathVariable Long productId, @RequestParam int quantity) {
        inventoryService.updateStock(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve-stock/{productId}")
    public ResponseEntity<Boolean> reserveStock(@PathVariable Long productId, @RequestParam int quantity) {
        boolean reserved = inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok(reserved);
    }

    @GetMapping("/needs-reorder")
    public ResponseEntity<List<Inventory>> getProductsNeedingReorder() {
        List<Inventory> inventories = inventoryService.findProductsNeedingReorder();
        return ResponseEntity.ok(inventories);
    }
}
