package com.example.demo.domain.productManagementDomain.controller;

import com.example.demo.domain.productManagementDomain.dto.CreateReviewRequestDto;
import com.example.demo.domain.productManagementDomain.dto.ReviewResponseDto;
import com.example.demo.domain.productManagementDomain.dto.ReviewSummaryDto;
import com.example.demo.domain.productManagementDomain.service.ProductReviewService;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;



    @GetMapping("/{productId}/reviews/summary")
    public ReviewSummaryDto getReviewSummary(@PathVariable Long productId) {
        return reviewService.getReviewSummary(productId);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, pageable));
    }

    @PostMapping
    public ResponseEntity<Void> createReview(
            @RequestBody @Valid CreateReviewRequestDto dto,
            @AuthenticationPrincipal User user) {
        reviewService.submitReview(dto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

