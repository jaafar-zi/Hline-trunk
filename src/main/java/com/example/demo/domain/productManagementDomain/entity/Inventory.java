package com.example.demo.domain.productManagementDomain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Inventory")
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantityInStock;
    private int reservedQuantity;
    private int reorderThreshold;
    private int reorderQuantity;


    private LocalDateTime lastUpdated;
    private LocalDateTime lastRestocked;


    public Inventory(Product product, int quantity, int reservedQuantity, LocalDateTime now) {
        this.product = product;
        this.quantityInStock = quantity;
        this.reservedQuantity = reservedQuantity;
        this.reorderThreshold = quantity * reservedQuantity;
    }
}
