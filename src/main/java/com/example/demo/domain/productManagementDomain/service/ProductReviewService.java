package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.domain.productManagementDomain.dto.CreateReviewRequestDto;
import com.example.demo.domain.productManagementDomain.dto.ReviewResponseDto;
import com.example.demo.domain.productManagementDomain.dto.ReviewSummaryDto;
import com.example.demo.domain.productManagementDomain.entity.Product;
import com.example.demo.domain.productManagementDomain.entity.ProductReview;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductReviewRepository;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitReview(CreateReviewRequestDto dto, Integer userId) {
        if (reviewRepository.existsByProductIdAndUserId(dto.productId(), userId)) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(dto.rating())
                .comment(dto.comment())
                .build();

        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(r -> new ReviewResponseDto(
                        r.getUser().getFirstname() + " " + r.getUser().getLastname(),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDto getReviewSummary(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product not found");
        }
        return reviewRepository.getReviewSummaryByProductId(productId);
    }
}
