package com.example.demo.domain.productManagementDomain.entity;


import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.*;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean published = false;

    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private List<Pricing> pricingList = new ArrayList<>();

    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private List<ProductAttribute> attributes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Asset> assets = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "favoriteProducts")
    @JsonIgnore
    private Set<User> favoritedByUsers = new HashSet<>();

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

