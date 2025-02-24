package com.example.demo.inventory;

import com.example.demo.productCatalog.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Inventory {
    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Link to the Product entity

    private int quantityInStock; // Current stock level
    private int reservedQuantity; // Quantity reserved for orders
    private int reorderThreshold; // Minimum stock level before reordering
    private int reorderQuantity; // Quantity to reorder when stock is low

    // Timestamps for tracking
    private LocalDateTime lastUpdated;
    private LocalDateTime lastRestocked;

    // Constructors, getters, setters
    public Inventory() {}

    public Inventory(Product product, int quantityInStock, int reservedQuantity, int reorderThreshold, int reorderQuantity) {
        this.product = product;
        this.quantityInStock = quantityInStock;
        this.reservedQuantity = reservedQuantity;
        this.reorderThreshold = reorderThreshold;
        this.reorderQuantity = reorderQuantity;
        this.lastUpdated = LocalDateTime.now();
    }

}
