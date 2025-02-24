package com.example.demo.productCatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pricing")
@EntityListeners(AuditingEntityListener.class)
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "pricingList")
    private List<Product> products = new ArrayList<>();

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Double oldPrice;  // The original price before a discount

    @Column(nullable = false)
    private Double price;  // The discounted price

    @Column(nullable = false)
    private boolean promoFlag;  // Indicates if the product is on promotion

    public void setProduct(Product product) {
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
    }
}
