package com.example.foodshop.service;

import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    // Sử dụng Gemini Pro - Most stable and widely available
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    
    private final WebClient webClient;
    private final Gson gson;
    
    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl(GEMINI_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Gọi Gemini API để xử lý câu hỏi của user
     * FALLBACK: Nếu Gemini lỗi, dùng logic đơn giản
     */
    public String chat(String userMessage, String context) {
        try {
            log.info("Chatbot request - User message: {}", userMessage);
            
            // Check API key
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${GEMINI_API_KEY}")) {
                log.warn("Gemini API key not configured, using fallback logic");
                return generateFallbackResponse(userMessage);
            }
            
            String prompt = buildPrompt(userMessage, context);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            log.info("Calling Gemini API with model: gemini-pro");
            
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.info("Gemini API response received");
            String result = extractTextFromResponse(response);
            log.info("Chatbot response: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Gemini API error, using fallback: {}", e.getMessage());
            return generateFallbackResponse(userMessage);
        }
    }
    
    /**
     * Fallback response khi Gemini API lỗi - Dùng logic đơn giản
     */
    private String generateFallbackResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // Chào hỏi
        if (lowerMessage.contains("xin chào") || lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("chào")) {
            return "Xin chào! 👋 Tôi là trợ lý của Food Shop.\n\n" +
                   "Tôi có thể giúp bạn:\n" +
                   "• Tìm món ăn ngon\n" +
                   "• Tạo mã giảm giá\n" +
                   "• Tư vấn sản phẩm\n\n" +
                   "Bạn muốn tìm món gì?";
        }
        
        // Tìm món ăn
        if (lowerMessage.contains("món") || lowerMessage.contains("có gì") || lowerMessage.contains("menu") || lowerMessage.contains("ngon")) {
            return "Chúng tôi có nhiều món ngon:\n\n" +
                   "🍓 Bingsu Dâu - 85,000đ\n" +
                   "🍜 Tokbokki - 45,000đ\n" +
                   "🍩 Donut Socola - 35,000đ\n" +
                   "🍰 Chè Khúc Bạch - 40,000đ\n\n" +
                   "Bạn muốn xem chi tiết món nào?";
        }
        
        // Tìm theo giá
        if (lowerMessage.contains("50") || lowerMessage.contains("rẻ") || lowerMessage.contains("giá")) {
            return "Các món dưới 50k:\n\n" +
                   "🍜 Tokbokki - 45,000đ\n" +
                   "🍩 Donut Socola - 35,000đ\n" +
                   "🍰 Chè Khúc Bạch - 40,000đ\n\n" +
                   "Tất cả đều ngon và giá hợp lý! 😊";
        }
        
        // Giao hàng
        if (lowerMessage.contains("giao") || lowerMessage.contains("ship") || lowerMessage.contains("phí")) {
            return "📦 Thông tin giao hàng:\n\n" +
                   "• Phí ship: 30,000đ\n" +
                   "• Thời gian: 30-45 phút\n" +
                   "• Giao hàng toàn quốc\n\n" +
                   "Đặt hàng ngay để nhận ưu đãi!";
        }
        
        // Đặt hàng
        if (lowerMessage.contains("đặt") || lowerMessage.contains("mua") || lowerMessage.contains("order")) {
            return "Để đặt hàng:\n\n" +
                   "1️⃣ Thêm món vào giỏ hàng\n" +
                   "2️⃣ Vào giỏ hàng\n" +
                   "3️⃣ Click 'Thanh toán'\n" +
                   "4️⃣ Điền thông tin giao hàng\n\n" +
                   "Rất đơn giản! 😊";
        }
        
        // Default
        return "Tôi có thể giúp bạn:\n\n" +
               "• Xem menu món ăn\n" +
               "• Tìm món theo giá\n" +
               "• Tạo mã giảm giá\n" +
               "• Hướng dẫn đặt hàng\n\n" +
               "Bạn muốn biết điều gì?";
    }
    
    /**
     * Xây dựng prompt cho Gemini với context
     */
    private String buildPrompt(String userMessage, String context) {
        return String.format("""
            Bạn là trợ lý AI của Food Shop - cửa hàng thực phẩm trực tuyến.
            
            NHIỆM VỤ:
            - Tư vấn sản phẩm cho khách hàng
            - Trả lời câu hỏi về sản phẩm, giá cả, giao hàng
            - Gợi ý món ăn phù hợp
            - Tạo voucher giảm giá khi cần thiết
            - Giúp khách hàng đặt hàng
            
            NGUYÊN TẮC:
            - Thân thiện, nhiệt tình
            - Trả lời ngắn gọn, dễ hiểu
            - Dùng emoji phù hợp
            - Luôn gợi ý thêm sản phẩm (upsell)
            
            CONTEXT (Thông tin sản phẩm):
            %s
            
            KHÁCH HÀNG HỎI: %s
            
            TRẢ LỜI (ngắn gọn, thân thiện):
            """, context, userMessage);
    }
    
    /**
     * Extract text từ response của Gemini
     */
    private String extractTextFromResponse(String response) {
        try {
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return "Xin lỗi, tôi không hiểu câu hỏi của bạn.";
        }
    }
    
    /**
     * Phân tích intent của user (search, voucher, order, etc.)
     */
    public String analyzeIntent(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("giảm giá") || lowerMessage.contains("voucher") 
            || lowerMessage.contains("mã giảm") || lowerMessage.contains("khuyến mãi")) {
            return "VOUCHER_REQUEST";
        }
        
        if (lowerMessage.contains("đặt") || lowerMessage.contains("mua") 
            || lowerMessage.contains("order")) {
            return "ORDER_REQUEST";
        }
        
        if (lowerMessage.contains("tìm") || lowerMessage.contains("có món") 
            || lowerMessage.contains("gợi ý")) {
            return "PRODUCT_SEARCH";
        }
        
        if (lowerMessage.contains("đơn hàng") || lowerMessage.contains("order") 
            || lowerMessage.contains("giao")) {
            return "ORDER_STATUS";
        }
        
        return "GENERAL_CHAT";
    }
}
