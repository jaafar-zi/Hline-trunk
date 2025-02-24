package com.example.demo.productCatalog.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name cannot be empty")
    @Column(nullable = false)
    private String name;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean published = false;

    @ManyToMany
    @JoinTable(
            name = "pricing",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    private List<Pricing> pricingList;

    @ManyToMany
    @JoinTable(
            name = "product_attribute",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    private List<ProductAttribute> attributes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Asset> assets = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean canBePublished() {

            if (this.attributes == null || this.attributes.isEmpty()) {
                return false;
            }
            for (ProductAttribute attribute : this.attributes) {
                if (attribute.getAttributeValue() == null || attribute.getAttributeValue().isEmpty()) {
                    return false;
                }
            }

            if (this.pricingList == null || this.pricingList.isEmpty()) {
                return false;
            }
            for (Pricing pricing : this.pricingList) {
                if (pricing.getPrice() == null || pricing.getPrice() <= 0) {
                    return false;
                }
            }


        return this.assets != null && !this.assets.isEmpty();

    }
}

