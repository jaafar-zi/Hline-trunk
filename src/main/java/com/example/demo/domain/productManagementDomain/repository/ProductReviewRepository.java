package com.example.demo.domain.productManagementDomain.repository;

import com.example.demo.domain.productManagementDomain.dto.ReviewSummaryDto;
import com.example.demo.domain.productManagementDomain.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    @Query("SELECT new com.example.demo.productManagementDomain.dto.ReviewSummaryDto(AVG(r.rating), COUNT(r)) " +
            "FROM ProductReview r WHERE r.product.id = :productId")
    ReviewSummaryDto getReviewSummaryByProductId(@Param("productId") Long productId);

    Page<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    boolean existsByProductIdAndUserId(Long productId, Integer userId);
}