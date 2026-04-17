package com.example.foodshop.service;

import com.example.foodshop.dto.ChatResponse;
import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.User;
import com.example.foodshop.entity.Voucher;
import com.example.foodshop.repository.ProductRepository;
import com.example.foodshop.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
    
    private final GeminiService geminiService;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    
    /**
     * Xử lý tin nhắn từ user
     */
    public ChatResponse processMessage(String message, User user) {
        try {
            // Phân tích intent
            String intent = geminiService.analyzeIntent(message);
            log.info("User intent: {}", intent);
            
            // Xử lý theo intent
            return switch (intent) {
                case "VOUCHER_REQUEST" -> handleVoucherRequest(message, user);
                case "PRODUCT_SEARCH" -> handleProductSearch(message, user);
                case "ORDER_REQUEST" -> handleOrderRequest(message, user);
                case "ORDER_STATUS" -> handleOrderStatus(message, user);
                default -> handleGeneralChat(message, user);
            };
            
        } catch (Exception e) {
            log.error("Error processing message", e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.")
                    .type("error")
                    .build();
        }
    }
    
    /**
     * Xử lý yêu cầu voucher
     */
    private ChatResponse handleVoucherRequest(String message, User user) {
        // Kiểm tra xem user đã có voucher chưa
        List<Voucher> existingVouchers = voucherRepository.findAvailableVouchersForUser(
                user, LocalDateTime.now());
        
        if (!existingVouchers.isEmpty()) {
            Voucher voucher = existingVouchers.get(0);
            return ChatResponse.builder()
                    .message(String.format(
                            "🎁 Bạn đã có mã giảm giá rồi nhé!\n\n" +
                            "Mã: %s\n" +
                            "Giảm: %s\n" +
                            "Áp dụng cho đơn từ: %,.0fđ\n" +
                            "Hết hạn: %s\n\n" +
                            "Dùng ngay khi thanh toán nhé! 😊",
                            voucher.getCode(),
                            formatDiscount(voucher),
                            voucher.getMinOrderValue(),
                            formatDateTime(voucher.getExpiresAt())
                    ))
                    .type("voucher")
                    .data(Map.of("voucher", voucher))
                    .suggestions(List.of("Xem sản phẩm", "Giỏ hàng", "Đặt hàng"))
                    .build();
        }
        
        // Tạo voucher mới cho user
        Voucher newVoucher = voucherService.createWelcomeVoucher(user);
        
        return ChatResponse.builder()
                .message(String.format(
                        "🎉 Tặng bạn mã giảm giá đặc biệt!\n\n" +
                        "Mã: %s\n" +
                        "Giảm: %s\n" +
                        "Áp dụng cho đơn từ: %,.0fđ\n" +
                        "Hết hạn: %s\n\n" +
                        "Nhanh tay đặt hàng nhé! 🛒",
                        newVoucher.getCode(),
                        formatDiscount(newVoucher),
                        newVoucher.getMinOrderValue(),
                        formatDateTime(newVoucher.getExpiresAt())
                ))
                .type("voucher")
                .data(Map.of("voucher", newVoucher))
                .suggestions(List.of("Xem sản phẩm", "Món hot", "Đặt hàng"))
                .build();
    }
    
    /**
     * Xử lý tìm kiếm sản phẩm
     */
    private ChatResponse handleProductSearch(String message, User user) {
        // Tìm sản phẩm phù hợp bằng database query thay vì load all
        List<Product> matchedProducts = productRepository.searchProducts(message);
        
        // Build context chỉ với matched products
        String productsContext = buildProductsContext(matchedProducts);
        
        // Gọi Gemini để tư vấn
        String aiResponse = geminiService.chat(message, productsContext);
        
        Map<String, Object> data = new HashMap<>();
        if (!matchedProducts.isEmpty()) {
            data.put("products", matchedProducts.stream()
                    .limit(5)
                    .collect(Collectors.toList()));
        }
        
        return ChatResponse.builder()
                .message(aiResponse)
                .type("product_list")
                .data(data)
                .suggestions(List.of("Xem chi tiết", "Thêm vào giỏ", "Gợi ý khác"))
                .build();
    }
    
    /**
     * Xử lý yêu cầu đặt hàng
     */
    private ChatResponse handleOrderRequest(String message, User user) {
        String response = "Để đặt hàng, bạn có thể:\n\n" +
                "1. 🛒 Thêm sản phẩm vào giỏ hàng\n" +
                "2. 💳 Chọn phương thức thanh toán\n" +
                "3. 📍 Nhập địa chỉ giao hàng\n\n" +
                "Bạn muốn xem món gì? 😊";
        
        return ChatResponse.builder()
                .message(response)
                .type("text")
                .suggestions(List.of("Xem menu", "Món hot", "Giỏ hàng"))
                .build();
    }
    
    /**
     * Xử lý kiểm tra đơn hàng
     */
    private ChatResponse handleOrderStatus(String message, User user) {
        String response = "Để kiểm tra đơn hàng, bạn vào mục \"Đơn hàng của tôi\" nhé!\n\n" +
                "Hoặc cho mình biết mã đơn hàng, mình sẽ kiểm tra giúp bạn 😊";
        
        return ChatResponse.builder()
                .message(response)
                .type("text")
                .suggestions(List.of("Đơn hàng của tôi", "Đặt hàng mới"))
                .build();
    }
    
    /**
     * Xử lý chat chung
     */
    private ChatResponse handleGeneralChat(String message, User user) {
        // Không load products cho general chat, chỉ chat đơn giản
        String aiResponse = geminiService.chat(message, "");
        
        return ChatResponse.builder()
                .message(aiResponse)
                .type("text")
                .suggestions(List.of("Xem menu", "Giảm giá", "Đặt hàng"))
                .build();
    }
    
    // Helper methods
    
    private String buildProductsContext(List<Product> products) {
        StringBuilder context = new StringBuilder("DANH SÁCH SẢN PHẨM:\n\n");
        
        for (Product p : products) {
            context.append(String.format(
                    "- %s (%s): %,.0fđ - %s (Còn %d)\n",
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getDescription(),
                    p.getStock()
            ));
        }
        
        return context.toString();
    }
    

    
    private String formatDiscount(Voucher voucher) {
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            return voucher.getDiscountValue() + "%";
        } else {
            return String.format("%,.0fđ", voucher.getDiscountValue());
        }
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
