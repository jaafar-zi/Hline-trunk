package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class ProductReviewService extends AbstractBaseService<ProductReview, Long> {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewService(ProductReviewRepository reviewRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository) {
        super(reviewRepository);
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ───────────────────────────────────────────────
    //  Base Info
    // ───────────────────────────────────────────────

    @Override
    protected String getCacheName() {
        return "productReviews";
    }

    @Override
    protected Class<?> getEntityClass() {
        return ProductReview.class;
    }

    // ───────────────────────────────────────────────
    //  CREATE (Custom)
    // ───────────────────────────────────────────────

    @Transactional
    public void submitReview(CreateReviewRequestDto dto, Integer userId) {
        log.info("Submitting review for productId={} by userId={}", dto.productId(), userId);

        if (reviewRepository.existsByProductIdAndUserId(dto.productId(), userId)) {
            throw new IllegalStateException("User has already reviewed this product");
        }

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + dto.productId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(dto.rating())
                .comment(dto.comment())
                .build();

        save(review);
    }

    // ───────────────────────────────────────────────
    //  READ
    // ───────────────────────────────────────────────

    @Transactional
    @Cacheable(cacheNames = "productReviews", key = "'product:' + #productId")
    public Page<ReviewResponseDto> getReviewsByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(r -> new ReviewResponseDto(
                        r.getUser().getFirstname() + " " + r.getUser().getLastname(),
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt()
                ));
    }

    @Transactional
    @Cacheable(cacheNames = "productReviews", key = "'summary:' + #productId")
    public ReviewSummaryDto getReviewSummary(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("Product not found with id: " + productId);
        }
        return reviewRepository.getReviewSummaryByProductId(productId);
    }

    // ───────────────────────────────────────────────
    //  Hooks
    // ───────────────────────────────────────────────

    @Override
    protected void onAfterSave(ProductReview review) {
        log.debug("Review saved for product {} by user {}", review.getProduct().getId(), review.getUser().getId());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.debug("Review deleted with id {}", id);
    }
}
