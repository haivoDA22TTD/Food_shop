package com.example.foodshop.controller;

import com.example.foodshop.dto.ChatRequest;
import com.example.foodshop.dto.ChatResponse;
import com.example.foodshop.entity.User;
import com.example.foodshop.service.ChatbotService;
import com.example.foodshop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    private final UserService userService;
    
    /**
     * Endpoint để chat với AI
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {
        
        try {
            // Get current user
            User user = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                user = userService.findByUsername(username);
            }
            
            log.info("Chat request from user: {}, message: {}", 
                    user != null ? user.getUsername() : "anonymous", 
                    request.getMessage());
            
            // Process message
            ChatResponse response = chatbotService.processMessage(request.getMessage(), user);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.ok(ChatResponse.builder()
                    .message("Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.")
                    .type("error")
                    .build());
        }
    }
    
    /**
     * Endpoint để lấy gợi ý câu hỏi
     */
    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions() {
        return ResponseEntity.ok(new String[]{
                "Có món gì ngon?",
                "Món nào dưới 50k?",
                "Gợi ý combo cho 2 người",
                "Có mã giảm giá không?",
                "Hamburger còn hàng không?",
                "Giao hàng mất bao lâu?"
        });
    }
}
