package com.example.foodshop.identity.service;

import com.example.foodshop.identity.dto.RegisterRequest;
import com.example.foodshop.identity.dto.ShipperRegistrationRequest;
import com.example.foodshop.identity.dto.UserDTO;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${app.admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.admin.default.email:admin@foodshop.local}")
    private String defaultAdminEmail;

    @Value("${app.admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Value("${app.order.service.url:http://localhost:8082}")
    private String orderServiceUrl;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
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

    /**
     * Register a new shipper account (admin-initiated)
     * Creates user in Identity Service and shipper profile in Order Service
     */
    @Transactional
    public User registerShipper(ShipperRegistrationRequest request) {
        // Validate required fields
        if (request.getUsername() == null || request.getUsername().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank() ||
            request.getEmail() == null || request.getEmail().isBlank() ||
            request.getName() == null || request.getName().isBlank() ||
            request.getPhone() == null || request.getPhone().isBlank()) {
            throw new RuntimeException("Missing required fields: username, password, email, name, phone");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }

        // Create User entity with SHIPPER role
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("SHIPPER");
        user.setAccountLocked(false);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Create shipper profile in Order Service
        try {
            Map<String, Object> shipperProfileRequest = new HashMap<>();
            shipperProfileRequest.put("userId", savedUser.getId());
            shipperProfileRequest.put("name", request.getName());
            shipperProfileRequest.put("phone", request.getPhone());
            shipperProfileRequest.put("email", request.getEmail());
            shipperProfileRequest.put("vehicleType", request.getVehicleType());
            shipperProfileRequest.put("vehicleNumber", request.getVehicleNumber());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(shipperProfileRequest, headers);

            String url = orderServiceUrl + "/internal/shippers";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Unable to create shipper profile in Order Service");
            }
        } catch (Exception e) {
            // Rollback user creation if shipper profile creation fails
            throw new RuntimeException("Unable to create shipper profile in Order Service: " + e.getMessage());
        }

        return savedUser;
    }
}
