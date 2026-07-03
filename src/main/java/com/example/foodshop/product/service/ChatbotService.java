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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChatbotService {
    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final ProductService productService;
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Tạm tắt Gemini khi bị 429, reset sau 60 giây
    private final AtomicLong geminiDisabledUntil = new AtomicLong(0);
    private static final long COOLDOWN_MS = 60_000;

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

    public ChatbotService(ProductService productService, GeminiConfig geminiConfig) {
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

        String productContext = buildProductContext(matchedProducts);

        // Thử gọi Gemini, nếu fail thì dùng fallback
        String aiResponse = callGeminiWithFallback(query, productContext, matchedProducts);

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

    private String callGeminiWithFallback(String query, String productContext, List<Product> products) {
        // Nếu đang trong cooldown (vừa bị 429), dùng fallback luôn
        if (System.currentTimeMillis() < geminiDisabledUntil.get()) {
            log.info("Gemini in cooldown, using fallback response");
            return buildFallbackResponse(query, products);
        }

        // Thử gọi Gemini
        String geminiResult = callGeminiAPI(query, productContext);
        if (geminiResult != null) {
            return geminiResult;
        }

        // Gemini fail → dùng fallback thông minh
        return buildFallbackResponse(query, products);
    }

    /**
     * Gọi Gemini API. Trả về null nếu fail.
     */
    private String callGeminiAPI(String userMessage, String productContext) {
        if (!geminiConfig.isEnabled()) {
            log.warn("Gemini disabled via config");
            return null;
        }
        if (geminiConfig.getApiKey() == null || geminiConfig.getApiKey().isEmpty()) {
            log.warn("Gemini API key is null or empty");
            return null;
        }

        log.info("Calling Gemini API, key starts with: {}...", 
                geminiConfig.getApiKey().substring(0, Math.min(8, geminiConfig.getApiKey().length())));

        try {
            String fullPrompt = SYSTEM_PROMPT
                    + "\n\nThông tin sản phẩm từ database:\n" + productContext
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
                JsonNode parts = root.path("candidates").get(0)
                        .path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                geminiDisabledUntil.set(System.currentTimeMillis() + COOLDOWN_MS);
                log.warn("Gemini 429 rate limit. Cooldown {}s", COOLDOWN_MS / 1000);
            } else if (e.getStatusCode().value() == 400) {
                log.error("Gemini 400 Bad Request - check API key or request format: {}", e.getMessage());
            } else if (e.getStatusCode().value() == 403) {
                log.error("Gemini 403 Forbidden - API key invalid or revoked: {}", e.getMessage());
            } else {
                log.error("Gemini HTTP error {}: {}", e.getStatusCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Fallback thông minh dựa trên rule-based khi Gemini không khả dụng.
     */
    private String buildFallbackResponse(String query, List<Product> products) {
        String lower = query.toLowerCase();

        // Có sản phẩm tìm thấy
        if (!products.isEmpty()) {
            StringBuilder sb = new StringBuilder("Mình tìm thấy các món phù hợp cho bạn:\n\n");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                sb.append(String.format("%d. %s - %sđ",
                        i + 1, p.getName(),
                        p.getPrice() != null ? String.format("%,.0f", p.getPrice().doubleValue()) : "Liên hệ"));
                if (p.getDescription() != null && !p.getDescription().isBlank()) {
                    String desc = p.getDescription();
                    if (desc.length() > 60) desc = desc.substring(0, 60) + "...";
                    sb.append("\n   ").append(desc);
                }
                sb.append("\n");
            }
            sb.append("\nBạn muốn xem chi tiết món nào không? 😊");
            return sb.toString();
        }

        // Rule-based theo intent
        if (lower.contains("giao hàng") || lower.contains("ship") || lower.contains("vận chuyển")) {
            return "🚚 Food Shop giao hàng nhanh trong vòng 30-45 phút! Phí ship tính theo khoảng cách. Bạn có thể theo dõi đơn hàng trong mục \"Đơn hàng\" nhé.";
        }
        if (lower.contains("thanh toán") || lower.contains("trả tiền") || lower.contains("payment")) {
            return "💳 Food Shop hỗ trợ nhiều hình thức thanh toán:\n• 💵 COD - Thanh toán khi nhận hàng\n• 💜 ZaloPay\n• 🏧 Chuyển khoản QR VCB\n\nBạn muốn biết thêm về hình thức nào?";
        }
        if (lower.contains("giá") || lower.contains("bao nhiêu") || lower.contains("đắt") || lower.contains("rẻ")) {
            return "💰 Food Shop có nhiều món với giá từ 20.000đ đến 200.000đ. Bạn muốn xem món trong khoảng giá nào để mình gợi ý?";
        }
        if (lower.contains("giờ") || lower.contains("mở cửa") || lower.contains("đóng cửa")) {
            return "🕐 Food Shop mở cửa từ 7:00 - 22:00 tất cả các ngày trong tuần. Sẵn sàng phục vụ bạn! 🍔";
        }
        if (lower.contains("hot") || lower.contains("bán chạy") || lower.contains("ngon") || lower.contains("gợi ý")) {
            return "⭐ Các món bán chạy nhất của Food Shop:\n• 🍔 Hamburger\n• 🍗 Gà rán\n• 🍕 Pizza\n• 🍟 Khoai tây chiên\n\nBạn muốn xem chi tiết món nào?";
        }
        if (lower.contains("xin chào") || lower.contains("hello") || lower.contains("hi")
                || lower.contains("alo") || lower.contains("chào") || lower.contains("hey")) {
            return "Xin chào! 👋 Mình là trợ lý Food Shop, sẵn sàng giúp bạn tìm món ngon. Bạn muốn ăn gì hôm nay? 🍔🍕🍗";
        }
        if (lower.contains("cảm ơn") || lower.contains("thanks")) {
            return "Không có gì! 😊 Food Shop luôn sẵn sàng phục vụ bạn. Chúc bạn ngon miệng! 🍽️";
        }

        return "Mình chưa hiểu câu hỏi của bạn 😅 Bạn có thể hỏi về:\n• 🍔 Tên món ăn cụ thể\n• 💰 Giá cả\n• 🚚 Giao hàng\n• 💳 Thanh toán\n• ⭐ Món bán chạy";
    }

    private String buildProductContext(List<Product> products) {
        if (products.isEmpty()) return "Không tìm thấy sản phẩm nào.";
        StringBuilder sb = new StringBuilder("Sản phẩm tìm thấy:\n");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append(String.format("%d. %s - %sđ", i + 1, p.getName(),
                    p.getPrice() != null ? p.getPrice().longValue() : "Liên hệ"));
            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                sb.append(" | ").append(p.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private List<String> getSmartSuggestions(String lastMessage) {
        String lower = lastMessage.toLowerCase();
        if (lower.contains("giá") || lower.contains("bao nhiêu")) {
            return List.of("Món dưới 50k", "Món bán chạy nhất", "Combo tiết kiệm");
        }
        if (lower.contains("giao hàng") || lower.contains("ship")) {
            return List.of("Phí ship bao nhiêu?", "Giao bao lâu?", "Theo dõi đơn hàng");
        }
        if (lower.contains("thanh toán")) {
            return List.of("COD", "ZaloPay", "Chuyển khoản");
        }
        return List.of("Có món gì ngon?", "Món bán chạy", "Giao hàng thế nào?");
    }
}