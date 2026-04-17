package com.example.foodshop.service;

import com.example.foodshop.entity.Review;
import com.example.foodshop.entity.Product;
import com.example.foodshop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }
    
    public List<Review> getProductReviews(Product product) {
        return reviewRepository.findByProductWithUser(product);
    }
}
