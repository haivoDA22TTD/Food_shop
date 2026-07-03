package com.example.foodshop.product.service;

import com.example.foodshop.product.dto.ChatResponse;
import com.example.foodshop.product.entity.Product;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {
    private final ProductService productService;

    public ChatbotService(ProductService productService) {
        this.productService = productService;
    }

    public ChatResponse processMessage(String message) {
        String query = message == null ? "" : message.trim();
        List<Product> matchedProducts = productService.searchProducts(query).stream()
                .limit(5)
                .toList();
        Map<String, Object> data = new HashMap<>();
        data.put("products", matchedProducts);

        if (matchedProducts.isEmpty()) {
            return ChatResponse.builder()
                    .message("Mình chưa tìm thấy món phù hợp. Bạn thử tên món hoặc danh mục khác nhé.")
                    .type("product_list")
                    .data(data)
                    .suggestions(List.of("Gợi ý món hot", "Hamburger", "Combo cho 2 người"))
                    .build();
        }

        return ChatResponse.builder()
                .message("Mình gợi ý vài món phù hợp cho bạn đây.")
                .type("product_list")
                .data(data)
                .suggestions(List.of("Xem chi tiết", "Món dưới 50k", "Gợi ý thêm"))
                .build();
    }
}
