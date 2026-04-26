package com.example.foodshop.product.controller;

import com.example.foodshop.product.dto.ChatRequest;
import com.example.foodshop.product.dto.ChatResponse;
import com.example.foodshop.product.service.ChatbotService;
import com.example.foodshop.product.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {
    private final ChatbotService chatbotService;
    private final RateLimitService rateLimitService;

    public ChatbotController(ChatbotService chatbotService, RateLimitService rateLimitService) {
        this.chatbotService = chatbotService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String sourceIp = httpRequest.getRemoteAddr() != null ? httpRequest.getRemoteAddr() : "unknown";
        if (!rateLimitService.allow("chatbot:chat:" + sourceIp, 30, Duration.ofMinutes(1))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many chatbot requests. Please try again later."));
        }

        ChatResponse response = chatbotService.processMessage(request.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions() {
        return ResponseEntity.ok(new String[]{
                "Có món gì ngon?",
                "Món nào dưới 50k?",
                "Gợi ý combo cho 2 người",
                "Hamburger còn hàng không?",
                "Món gà nào bán chạy?",
                "Gợi ý món tráng miệng"
        });
    }
}
