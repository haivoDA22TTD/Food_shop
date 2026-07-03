package com.example.foodshop.product.service;

import com.example.foodshop.product.config.GeminiConfig;
import com.example.foodshop.product.dto.ChatResponse;
import com.example.foodshop.product.entity.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatbotService {
    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final ProductService productService;
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý ảo thân thiện của cửa hàng "Food Shop" - một cửa hàng bán đồ ăn nhanh.
        
        Nhiệm vụ của bạn:
        - Giúp khách hàng tìm kiếm món ăn phù hợp
        - Trả lời câu hỏi về menu, giá cả, nguyên liệu
        - Đưa ra gợi ý món ăn based on sở thích khách hàng
        - Giải đáp thắc mắc về đơn hàng, giao hàng, thanh toán
        - Hỗ trợ khách hàng một cách thân thiện, nhiệt tình
        
        Quy tắc:
        - Luôn trả lời bằng tiếng Việt
        - Ngắn gọn, dễ hiểu, sử dụng emoji phù hợp
        - Nếu biết thông tin sản phẩm từ database, hãy ưu tiên hiển thị
        - Nếu không biết câu trả lời, hãy đề nghị khách hàng liên hệ bộ phận CSKH
        - Không tự invented thông tin về sản phẩm, giá cả
        """;

    public ChatbotService(ProductService productService,
                          GeminiConfig geminiConfig) {
        this.productService = productService;
        this.geminiConfig = geminiConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public ChatResponse processMessage(String message) {
        String query = message == null ? "" : message.trim();

        if (query.isEmpty()) {
            return ChatResponse.builder()
                    .message("Xin chào! Bạn cần mình hỗ trợ gì ạ? 😊")
                    .type("text")
                    .suggestions(List.of("Gợi ý món hot", "Xem menu", "Giá cả thế nào?"))
                    .build();
        }

        // Search products from database
        List<Product> matchedProducts = productService.searchProducts(query).stream()
                .limit(5)
                .toList();

        // Build product context for Gemini
        String productContext = buildProductContext(matchedProducts);

        // Call Gemini API
        String aiResponse = callGeminiAPI(query, productContext);

        Map<String, Object> data = new HashMap<>();
        if (!matchedProducts.isEmpty()) {
            data.put("products", matchedProducts);
        }

        return ChatResponse.builder()
                .message(aiResponse)
                .type(!matchedProducts.isEmpty() ? "product_list" : "text")
                .data(data)
                .suggestions(getSmartSuggestions(query))
                .build();
    }

    private String buildProductContext(List<Product> products) {
        if (products.isEmpty()) {
            return "Không tìm thấy sản phẩm nào trong database.";
        }

        StringBuilder sb = new StringBuilder("Sản phẩm tìm thấy trong database:\n");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append(String.format("%d. %s - %sđ",
                    i + 1,
                    p.getName(),
                    p.getPrice() != null ? p.getPrice().longValue() : "Liên hệ"));
            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                sb.append(" | Mô tả: ").append(p.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String callGeminiAPI(String userMessage, String productContext) {
        if (!geminiConfig.isEnabled() || geminiConfig.getApiKey() == null || geminiConfig.getApiKey().isEmpty()) {
            log.warn("Gemini API is disabled or API key is not configured");
            return "Xin lỗi, trợ lý AI hiện đang bảo trì. Vui lòng thử lại sau. 🤖";
        }

        try {
            String fullPrompt = SYSTEM_PROMPT + "\n\nThông tin sản phẩm từ database:\n" + productContext
                    + "\n\nCâu hỏi của khách hàng: " + userMessage;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", fullPrompt)))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "maxOutputTokens", 500,
                            "topP", 0.9
                    )
            );

            String url = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).path("content").path("parts");
                    if (content.isArray() && content.size() > 0) {
                        return content.get(0).path("text").asText();
                    }
                }
            }

            log.warn("Gemini API returned unexpected response");
            return "Xin lỗi, mình gặp sự cố khi xử lý. Bạn thử lại nhé! 😅";

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            return "Xin lỗi, trợ lý AI tạm thời không khả dụng. Bạn thử lại sau nhé! 🤖";
        }
    }

    private List<String> getSmartSuggestions(String lastMessage) {
        String lower = lastMessage.toLowerCase();
        if (lower.contains("giá") || lower.contains("bao nhiêu") || lower.contains("đắt")) {
            return List.of("Xem món dưới 50k", "Món bán chạy nhất", "So sánh giá");
        }
        if (lower.contains("gợi ý") || lower.contains("gợi ý")) {
            return List.of("Món hot nhất", "Combo tiết kiệm", "Món mới");
        }
        if (lower.contains("giao hàng") || lower.contains("ship") || lower.contains("vận chuyển")) {
            return List.of("Phí ship bao nhiêu?", "Giao bao lâu?", "Theo dõi đơn hàng");
        }
        if (lower.contains("thanh toán") || lower.contains("trả tiền") || lower.contains("payment")) {
            return List.of("COD là gì?", "ZaloPay", "Chuyển khoản");
        }
        return List.of("Có món gì ngon?", "Món bán chạy", "Xem menu");
    }
}
