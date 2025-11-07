package com.example.demo.domain.productManagementDomain.dto;

import lombok.*;

public record ReviewSummaryDto(
        Double averageRating,
        Long totalReviews
) {}
