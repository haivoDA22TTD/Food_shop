package com.example.foodshop.service;

import com.example.foodshop.entity.User;
import com.example.foodshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(String email, String googleId, String name) {
        log.info("OAuth2 - Finding or creating user for email: {}, googleId: {}", email, googleId);
        
        // First, try to find by Google ID
        var userByGoogleId = userRepository.findByGoogleId(googleId);
        if (userByGoogleId.isPresent()) {
            log.info("Found existing user by Google ID: {}", userByGoogleId.get().getUsername());
            return userByGoogleId.get();
        }
        
        // Check if email already exists
        var userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            // Link Google account to existing user
            User existingUser = userByEmail.get();
            log.info("Linking Google account to existing user: {}", existingUser.getUsername());
            
            // Check if this user already has a different Google ID
            if (existingUser.getGoogleId() != null && !existingUser.getGoogleId().equals(googleId)) {
                log.error("User {} already linked to different Google account", existingUser.getUsername());
                throw new IllegalStateException("Email already linked to different Google account");
            }
            
            existingUser.setGoogleId(googleId);
            return userRepository.save(existingUser);
        }
        
        // Create new user with unique username
        String baseUsername = email.split("@")[0];
        String username = generateUniqueUsername(baseUsername);
        
        log.info("Creating new user with username: {}", username);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(null); // Null password for OAuth users
        newUser.setRole(User.Role.CUSTOMER);
        newUser.setGoogleId(googleId);
        
        User savedUser = userRepository.save(newUser);
        log.info("Successfully created new user: {}", savedUser.getUsername());
        return savedUser;
    }
    
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + "_" + counter;
            counter++;
            
            // Prevent infinite loop
            if (counter > 1000) {
                username = baseUsername + "_" + System.currentTimeMillis();
                break;
            }
        }
        
        return username;
    }
}
