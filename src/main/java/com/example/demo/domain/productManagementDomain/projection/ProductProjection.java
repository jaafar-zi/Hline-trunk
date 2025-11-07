package com.example.demo.domain.productManagementDomain.projection;

public interface ProductProjection {
    Long getId();
    String getName();
    Integer getStockQuantity();
    Long getCategoryId();
}
