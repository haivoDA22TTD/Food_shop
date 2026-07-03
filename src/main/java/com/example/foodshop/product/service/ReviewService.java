package com.example.foodshop.product.service;

import com.example.foodshop.product.dto.CreateReviewRequest;
import com.example.foodshop.product.dto.ReviewResponse;
import com.example.foodshop.product.entity.OrderRef;
import com.example.foodshop.product.entity.Product;
import com.example.foodshop.product.entity.Review;
import com.example.foodshop.product.entity.UserRef;
import com.example.foodshop.product.repository.OrderRefRepository;
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
    private final OrderRefRepository orderRefRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRefRepository userRefRepository,
                         OrderRefRepository orderRefRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRefRepository = userRefRepository;
        this.orderRefRepository = orderRefRepository;
    }

    @Transactional
    public void createReview(CreateReviewRequest request, Long authUserId, String authUsername) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1-5 sao");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        UserRef user = userRefRepository.findById(authUserId)
                .filter(u -> u.getUsername().equals(authUsername))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        OrderRef order = orderRefRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUserId().equals(authUserId)) {
            throw new IllegalArgumentException("Đơn hàng không hợp lệ");
        }

        if (order.getStatus() != OrderRef.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Đơn hàng phải được giao thành công mới có thể đánh giá");
        }

        boolean alreadyReviewed = reviewRepository.findByProduct(product).stream()
                .anyMatch(r -> r.getUser().getId().equals(authUserId)
                        && r.getOrder().getId().equals(request.getOrderId()));
        if (alreadyReviewed) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setOrder(order);
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
                        .user(ReviewResponse.UserSummary.builder()
                                .id(review.getUser().getId())
                                .username(review.getUser().getUsername())
                                .build())
                        .build())
                .toList();
    }
}
