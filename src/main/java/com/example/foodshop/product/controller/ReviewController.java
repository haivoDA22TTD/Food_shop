package com.example.foodshop.product.controller;

import com.example.foodshop.product.dto.CreateReviewRequest;
import com.example.foodshop.product.service.AuthContextService;
import com.example.foodshop.product.service.RateLimitService;
import com.example.foodshop.product.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final AuthContextService authContextService;
    private final RateLimitService rateLimitService;

    public ReviewController(ReviewService reviewService,
                            AuthContextService authContextService,
                            RateLimitService rateLimitService) {
        this.reviewService = reviewService;
        this.authContextService = authContextService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReview(@Valid @RequestBody CreateReviewRequest request, HttpServletRequest httpRequest) {
        String sourceIp = httpRequest.getRemoteAddr() != null ? httpRequest.getRemoteAddr() : "unknown";
        if (!rateLimitService.allow("review:create:" + sourceIp, 10, Duration.ofMinutes(1))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many review attempts. Please try again later."));
        }

        try {
            AuthContextService.AuthContext authContext = authContextService.requireAuth(httpRequest);
            reviewService.createReview(request, authContext.userId(), authContext.username());
            return ResponseEntity.ok(Map.of("message", "Đánh giá thành công", "success", true));
        } catch (IllegalArgumentException e) {
            if ("Vui lòng đăng nhập".equals(e.getMessage()) || "Token không hợp lệ".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductReviews(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
