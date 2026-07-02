package com.example.foodshop.product.service;

import com.example.foodshop.product.dto.CreateReviewRequest;
import com.example.foodshop.product.dto.ReviewResponse;
import com.example.foodshop.product.entity.Product;
import com.example.foodshop.product.entity.Review;
import com.example.foodshop.product.entity.UserRef;
import com.example.foodshop.product.repository.ProductRepository;
import com.example.foodshop.product.repository.ReviewRepository;
import com.example.foodshop.product.repository.UserRefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRefRepository userRefRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRefRepository userRefRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRefRepository = userRefRepository;
    }

    @Transactional
    public void createReview(CreateReviewRequest request, Long authUserId, String authUsername) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1-5 sao");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        UserRef user = userRefRepository.findById(authUserId).orElseGet(() -> {
            UserRef newUser = new UserRef();
            newUser.setId(authUserId);
            newUser.setUsername(authUsername);
            return userRefRepository.save(newUser);
        });

        boolean alreadyReviewed = reviewRepository.findByProduct(product).stream()
                .anyMatch(r -> r.getUser().getId().equals(authUserId)
                        && r.getOrderId().equals(request.getOrderId()));
        if (alreadyReviewed) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);
    }

    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        return reviewRepository.findByProductWithUser(product).stream()
                .map(review -> ReviewResponse.builder()
                        .id(review.getId())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .orderId(review.getOrderId())
                        .user(ReviewResponse.UserSummary.builder()
                                .id(review.getUser().getId())
                                .username(review.getUser().getUsername())
                                .build())
                        .build())
                .toList();
    }
}
