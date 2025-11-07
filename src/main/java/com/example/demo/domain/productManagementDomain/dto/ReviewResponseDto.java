package com.example.demo.domain.productManagementDomain.dto;

import java.time.LocalDateTime;


public record ReviewResponseDto(
        String reviewerName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {}
