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
    
    // Sử dụng Gemini 1.5 Flash - Stable, reliable, FREE!
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    
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
     */
    public String chat(String userMessage, String context) {
        try {
            log.info("Chatbot request - User message: {}", userMessage);
            
            // Check API key
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${GEMINI_API_KEY}")) {
                log.error("Gemini API key not configured!");
                return "Xin lỗi, hệ thống chatbot chưa được cấu hình. Vui lòng liên hệ admin.";
            }
            
            String prompt = buildPrompt(userMessage, context);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            log.info("Calling Gemini API with model: gemini-1.5-flash");
            
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            log.error("Gemini API error - Status: {}", clientResponse.statusCode());
                            return clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> log.error("Error body: {}", body))
                                .then();
                        }
                    )
                    .bodyToMono(String.class)
                    .block();
            
            log.info("Gemini API response received");
            String result = extractTextFromResponse(response);
            log.info("Chatbot response: {}", result);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error calling Gemini API - Message: {}, Cause: {}", 
                e.getMessage(), 
                e.getCause() != null ? e.getCause().getMessage() : "Unknown"
            );
            e.printStackTrace();
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ admin.";
        }
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
