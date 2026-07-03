package com.example.foodshop.product.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String username;
    }
}
