package com.example.foodshop.identity.service;

import com.example.foodshop.identity.dto.RegisterRequest;
import com.example.foodshop.identity.dto.UserDTO;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.admin.default.email:admin@foodshop.local}")
    private String defaultAdminEmail;

    @Value("${app.admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setAccountLocked(false);

        return userRepository.save(user);
    }

    @Transactional
    public User processOAuth2User(String email, String name, String googleId) {
        return userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(name);
                    newUser.setEmail(email);
                    newUser.setGoogleId(googleId);
                    newUser.setRole("USER");
                    newUser.setAccountLocked(false);
                    return userRepository.save(newUser);
                });
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setAccountLocked(user.getAccountLocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    @Transactional
    public void ensureDefaultAdmin() {
        if (userRepository.countByRole("ADMIN") > 0) {
            return;
        }

        User admin = new User();
        admin.setUsername(defaultAdminUsername);
        admin.setEmail(defaultAdminEmail);
        admin.setPasswordHash(passwordEncoder.encode(defaultAdminPassword));
        admin.setRole("ADMIN");
        admin.setAccountLocked(false);
        userRepository.save(admin);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new RuntimeException("Tai khoan nay khong ho tro doi mat khau bang cach nay");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mat khau hien tai khong dung");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
