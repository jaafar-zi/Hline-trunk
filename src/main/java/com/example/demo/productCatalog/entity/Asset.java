package com.example.demo.productCatalog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_assets")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // URL or file path
    //file path should be well formated
    //logic should be implemented in a url handler
    //separate data handling from data access layer
    @Column(nullable = false)
    private String assetUrl; // URL or file path

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

