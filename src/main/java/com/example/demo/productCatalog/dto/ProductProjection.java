package com.example.demo.productCatalog.dto;

public interface ProductProjection {
    Long getId();
    String getName();
    Integer getStockQuantity();
    Long getCategoryId();
}
