package com.example.foodshop.order.client;

import com.example.foodshop.order.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IdentityServiceClientFallback implements IdentityServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(IdentityServiceClientFallback.class);
    
    @Override
    public UserDTO getUserById(Long userId) {
        logger.warn("Identity Service is unavailable. Returning fallback user info for userId: {}", userId);
        
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setId(userId);
        fallbackUser.setUsername("User #" + userId);
        fallbackUser.setEmail("unknown@example.com");
        fallbackUser.setFullName("User #" + userId);
        
        return fallbackUser;
    }
}
