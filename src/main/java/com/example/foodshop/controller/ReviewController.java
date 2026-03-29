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
    public String productDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            List<Review> reviews = reviewService.getProductReviews(product);
            
            // Calculate average rating
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            
            // Calculate stars for display
            int fullStars = (int) Math.round(avgRating);
            int emptyStars = 5 - fullStars;
            
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews);
            model.addAttribute("avgRating", avgRating);
            model.addAttribute("reviewCount", reviews.size());
            model.addAttribute("fullStars", fullStars);
            model.addAttribute("emptyStars", emptyStars);
            
            // Check if user can review (must have ordered and delivered)
            if (authentication != null) {
                String username = authentication.getName();
                System.out.println("🔍 DEBUG: User logged in: " + username);
                
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    System.out.println("🔍 DEBUG: User found in DB: " + user.getId());
                    
                    List<Order> userOrders = orderRepository.findByUserOrderByCreatedAtDesc(user);
                    System.out.println("🔍 DEBUG: Total orders: " + userOrders.size());
                    
                    // Count delivered orders with this product
                    long deliveredOrdersWithProduct = userOrders.stream()
                            .filter(order -> {
                                boolean isDelivered = Order.OrderStatus.DELIVERED.equals(order.getStatus());
                                boolean hasProduct = order.getOrderItems() != null &&
                                        order.getOrderItems().stream()
                                                .anyMatch(item -> item.getProduct() != null && 
                                                                item.getProduct().getId().equals(id));
                                
                                if (isDelivered && hasProduct) {
                                    System.out.println("🔍 DEBUG: Found delivered order #" + order.getId() + " with product #" + id);
                                }
                                
                                return isDelivered && hasProduct;
                            })
                            .count();
                    
                    System.out.println("🔍 DEBUG: Delivered orders with product: " + deliveredOrdersWithProduct);
                    
                    boolean canReview = deliveredOrdersWithProduct > 0;
                    
                    // Check if already reviewed
                    boolean hasReviewed = reviews.stream()
                            .anyMatch(review -> review.getUser() != null && 
                                              review.getUser().getId().equals(user.getId()));
                    
                    System.out.println("🔍 DEBUG: canReview=" + canReview + ", hasReviewed=" + hasReviewed);
                    
                    model.addAttribute("canReview", canReview && !hasReviewed);
                    model.addAttribute("hasReviewed", hasReviewed);
                } else {
                    System.out.println("⚠️ DEBUG: User not found in DB");
                    model.addAttribute("canReview", false);
                    model.addAttribute("hasReviewed", false);
                }
            } else {
                System.out.println("⚠️ DEBUG: No authentication");
                model.addAttribute("canReview", false);
                model.addAttribute("hasReviewed", false);
            }
            
            return "product-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }
    
    @PostMapping("/api/reviews/create")
    @ResponseBody
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> reviewData, 
                                         Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Long productId = Long.valueOf(reviewData.get("productId").toString());
            Long orderId = Long.valueOf(reviewData.get("orderId").toString());
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            String comment = reviewData.get("comment").toString();
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify order belongs to user and is delivered
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("Invalid order");
            }
            
            if (!Order.OrderStatus.DELIVERED.equals(order.getStatus())) {
                return ResponseEntity.badRequest().body("Order must be delivered to review");
            }
            
            // Check if already reviewed
            List<Review> existingReviews = reviewRepository.findByProduct(product);
            boolean alreadyReviewed = existingReviews.stream()
                    .anyMatch(r -> r.getUser().getId().equals(user.getId()) && 
                                  r.getOrder().getId().equals(orderId));
            
            if (alreadyReviewed) {
                return ResponseEntity.badRequest().body("Already reviewed this product");
            }
            
            Review review = new Review();
            review.setProduct(product);
            review.setUser(user);
            review.setOrder(order);
            review.setRating(rating);
            review.setComment(comment);
            
            reviewService.createReview(review);
            
            return ResponseEntity.ok().body(Map.of("message", "Review created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
