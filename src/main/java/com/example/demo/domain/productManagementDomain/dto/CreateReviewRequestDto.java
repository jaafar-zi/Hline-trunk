package com.example.demo.domain.productManagementDomain.dto;

public record CreateReviewRequestDto(
        Long productId,
        Integer rating,
        String comment
) {}
