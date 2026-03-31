package com.example.foodshop.controller;

import com.example.foodshop.entity.Order;
import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.Review;
import com.example.foodshop.entity.User;
import com.example.foodshop.repository.OrderRepository;
import com.example.foodshop.repository.ProductRepository;
import com.example.foodshop.repository.ReviewRepository;
import com.example.foodshop.repository.UserRepository;
import com.example.foodshop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    
    @GetMapping("/product/{id}")
    @Transactional(readOnly = true)
    public String productDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            List<Review> reviews = reviewService.getProductReviews(product);
            
            // Calculate average rating
            double avgRating = 0.0;
            if (!reviews.isEmpty()) {
                avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
            }
            
            // Calculate stars for display
            int fullStars = (int) Math.round(avgRating);
            int emptyStars = 5 - fullStars;
            
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews);
            model.addAttribute("avgRating", avgRating);
            model.addAttribute("reviewCount", reviews.size());
            model.addAttribute("fullStars", fullStars);
            model.addAttribute("emptyStars", emptyStars);
            
            // Default values
            model.addAttribute("canReview", false);
            model.addAttribute("hasReviewed", false);
            
            // Check if user can review (must have ordered and delivered)
            if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
                try {
                    String username = authentication.getName();
                    User user = userRepository.findByUsername(username).orElse(null);
                    
                    if (user != null) {
                        // Check if already reviewed (simple query)
                        boolean hasReviewed = reviewRepository.findByProduct(product).stream()
                                .anyMatch(r -> r.getUser() != null && r.getUser().getId().equals(user.getId()));
                        
                        // Check if has delivered order with this product
                        boolean canReview = false;
                        if (!hasReviewed) {
                            List<Order> userOrders = orderRepository.findByUserWithItemsOrderByCreatedAtDesc(user);
                            for (Order order : userOrders) {
                                if (Order.OrderStatus.DELIVERED.equals(order.getStatus()) && 
                                    order.getOrderItems() != null) {
                                    
                                    for (var item : order.getOrderItems()) {
                                        if (item != null && item.getProduct() != null && 
                                            item.getProduct().getId().equals(id)) {
                                            canReview = true;
                                            break;
                                        }
                                    }
                                    if (canReview) break;
                                }
                            }
                        }
                        
                        model.addAttribute("canReview", canReview);
                        model.addAttribute("hasReviewed", hasReviewed);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error checking review permissions: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with default values
                }
            }
            
            return "product-detail";
        } catch (Exception e) {
            System.err.println("❌ ERROR in productDetail: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        }
    }
    
    @PostMapping("/api/reviews/create")
    @ResponseBody
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> reviewData, 
                                         Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập"));
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Long productId = Long.valueOf(reviewData.get("productId").toString());
            Long orderId = Long.valueOf(reviewData.get("orderId").toString());
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            String comment = reviewData.get("comment").toString();
            
            // Validate rating
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rating phải từ 1-5 sao"));
            }
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify order belongs to user and is delivered
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng không hợp lệ"));
            }
            
            if (!Order.OrderStatus.DELIVERED.equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng phải được giao thành công mới có thể đánh giá"));
            }
            
            // Check if already reviewed
            List<Review> existingReviews = reviewRepository.findByProduct(product);
            boolean alreadyReviewed = existingReviews.stream()
                    .anyMatch(r -> r.getUser().getId().equals(user.getId()) && 
                                  r.getOrder().getId().equals(orderId));
            
            if (alreadyReviewed) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bạn đã đánh giá sản phẩm này rồi"));
            }
            
            Review review = new Review();
            review.setProduct(product);
            review.setUser(user);
            review.setOrder(order);
            review.setRating(rating);
            review.setComment(comment);
            
            reviewService.createReview(review);
            
            return ResponseEntity.ok().body(Map.of("message", "Đánh giá thành công", "success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
    
    @GetMapping("/api/reviews/product/{productId}")
    @ResponseBody
    public ResponseEntity<?> getProductReviews(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        List<Review> reviews = reviewService.getProductReviews(product);
        
        return ResponseEntity.ok(reviews);
    }
}
