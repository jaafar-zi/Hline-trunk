package com.example.demo.domain.productManagementDomain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_attribute")
@EntityListeners(AuditingEntityListener.class)
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "product_attribute_list",
            joinColumns = @JoinColumn(name = "attribute_list_id"),         // FK to Pricing
            inverseJoinColumns = @JoinColumn(name = "product_id")   // FK to Product
    )
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @Column(nullable = false)
    private String lang;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String attributeValue;

    public void setProduct(Product product) {
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
    }
}

