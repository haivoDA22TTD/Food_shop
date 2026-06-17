package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.dto.AuthRequest;
import com.example.foodshop.identity.dto.ShipperRegistrationRequest;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.repository.UserRepository;
import com.example.foodshop.identity.security.JwtUtil;
import com.example.foodshop.identity.service.AuthRateLimitService;
import com.example.foodshop.identity.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRESERVATION PROPERTY TESTS (Task 2)
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 * 
 * IMPORTANT: These tests follow observation-first methodology
 * - Tests observe behavior on UNFIXED code for non-buggy inputs
 * - Tests capture existing behavior patterns that must be preserved
 * - EXPECTED OUTCOME: All tests MUST PASS on unfixed code
 * 
 * Property 2: Preservation - Existing Authentication and Validation Behavior
 * 
 * These tests verify:
 * - Validation errors occur BEFORE any profile creation attempt
 * - Existing shipper authentication succeeds without duplicate profile creation
 * - Non-shipper users authenticate without shipper profile checks
 * - Existing shipper profiles remain functional
 * - JWT-based authorization continues to work for protected endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ShipperCreationPreservationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private AuthRateLimitService authRateLimitService;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Mock JWT generation
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString()))
            .thenReturn("mock.jwt.token");

        // Mock Redis-dependent services
        when(authRateLimitService.allow(anyString(), anyInt(), any()))
            .thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(anyString()))
            .thenReturn(false);
    }

    /**
     * PRESERVATION TEST 1: Validation Errors for Duplicate Username
     * **Validates: Requirement 3.1**
     * 
     * Observes: When admin creates shipper with duplicate username, validation error occurs
     * Expected: Validation should fail BEFORE any profile creation attempt
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_ValidationError_DuplicateUsername() throws Exception {
        // Given - Order Service is available and first shipper is created successfully
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        userRepository.deleteAll();

        ShipperRegistrationRequest firstRequest = new ShipperRegistrationRequest();
        firstRequest.setUsername("shipper_duplicate");
        firstRequest.setPassword("SecurePass123");
        firstRequest.setEmail("first@example.com");
        firstRequest.setName("First Shipper");
        firstRequest.setPhone("+84901111111");
        firstRequest.setVehicleType("Bike");
        firstRequest.setVehicleNumber("59A-11111");

        // Create first shipper
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Admin attempts to create second shipper with same username
        ShipperRegistrationRequest duplicateRequest = new ShipperRegistrationRequest();
        duplicateRequest.setUsername("shipper_duplicate");  // Same username
        duplicateRequest.setPassword("SecurePass456");
        duplicateRequest.setEmail("second@example.com");  // Different email
        duplicateRequest.setName("Second Shipper");
        duplicateRequest.setPhone("+84902222222");
        duplicateRequest.setVehicleType("Motorbike");
        duplicateRequest.setVehicleNumber("59A-22222");

        // Then - PRESERVED BEHAVIOR: Validation error occurs
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));

        // Verify only one user exists (duplicate was rejected)
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
    }

    /**
     * PRESERVATION TEST 2: Validation Errors for Duplicate Email
     * **Validates: Requirement 3.1**
     * 
     * Observes: When admin creates shipper with duplicate email, validation error occurs
     * Expected: Validation should fail BEFORE any profile creation attempt
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_ValidationError_DuplicateEmail() throws Exception {
        // Given - Order Service is available and first shipper is created successfully
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        userRepository.deleteAll();

        ShipperRegistrationRequest firstRequest = new ShipperRegistrationRequest();
        firstRequest.setUsername("shipper_first");
        firstRequest.setPassword("SecurePass123");
        firstRequest.setEmail("duplicate@example.com");
        firstRequest.setName("First Shipper");
        firstRequest.setPhone("+84903333333");
        firstRequest.setVehicleType("Car");
        firstRequest.setVehicleNumber("59A-33333");

        // Create first shipper
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Admin attempts to create second shipper with same email
        ShipperRegistrationRequest duplicateEmailRequest = new ShipperRegistrationRequest();
        duplicateEmailRequest.setUsername("shipper_second");  // Different username
        duplicateEmailRequest.setPassword("SecurePass456");
        duplicateEmailRequest.setEmail("duplicate@example.com");  // Same email
        duplicateEmailRequest.setName("Second Shipper");
        duplicateEmailRequest.setPhone("+84904444444");
        duplicateEmailRequest.setVehicleType("Bike");
        duplicateEmailRequest.setVehicleNumber("59A-44444");

        // Then - PRESERVED BEHAVIOR: Validation error occurs
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));

        // Verify only one user exists (duplicate was rejected)
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
    }

    /**
     * PRESERVATION TEST 3: Validation Errors for Invalid Email Format
     * **Validates: Requirement 3.1**
     * 
     * Observes: When admin creates shipper with invalid email format, validation error occurs
     * Expected: Validation should fail BEFORE any profile creation attempt
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_ValidationError_InvalidEmailFormat() throws Exception {
        // Given - Request with invalid email format
        userRepository.deleteAll();

        ShipperRegistrationRequest invalidEmailRequest = new ShipperRegistrationRequest();
        invalidEmailRequest.setUsername("shipper_invalid_email");
        invalidEmailRequest.setPassword("SecurePass123");
        invalidEmailRequest.setEmail("not-an-email-address");  // Invalid format
        invalidEmailRequest.setName("Invalid Email Shipper");
        invalidEmailRequest.setPhone("+84905555555");
        invalidEmailRequest.setVehicleType("Motorbike");
        invalidEmailRequest.setVehicleNumber("59A-55555");

        // When & Then - PRESERVED BEHAVIOR: Validation error occurs
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format"));

        // Verify no user was created
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(0);
    }

    /**
     * PRESERVATION TEST 4: Validation Errors for Missing Required Fields
     * **Validates: Requirement 3.1**
     * 
     * Observes: When admin creates shipper with missing fields, validation error occurs
     * Expected: Validation should fail BEFORE any profile creation attempt
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_ValidationError_MissingRequiredFields() throws Exception {
        // Given - Request with missing username
        userRepository.deleteAll();

        ShipperRegistrationRequest missingFieldRequest = new ShipperRegistrationRequest();
        missingFieldRequest.setUsername(null);  // Missing
        missingFieldRequest.setPassword("SecurePass123");
        missingFieldRequest.setEmail("missing@example.com");
        missingFieldRequest.setName("Missing Field Shipper");
        missingFieldRequest.setPhone("+84906666666");

        // When & Then - PRESERVED BEHAVIOR: Validation error occurs
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingFieldRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required fields: username, password, email, name, phone"));

        // Verify no user was created
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(0);
    }

    /**
     * PRESERVATION TEST 5: Successful Shipper Creation When Order Service Available
     * **Validates: Requirements 3.2, 3.4**
     * 
     * Observes: When Order Service is available, shipper creation succeeds normally
     * Expected: User entity created, JWT token returned, success response
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_SuccessfulCreation_OrderServiceAvailable() throws Exception {
        // Given - Order Service is available and responds successfully
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Shipper profile created", HttpStatus.CREATED));

        userRepository.deleteAll();

        ShipperRegistrationRequest validRequest = new ShipperRegistrationRequest();
        validRequest.setUsername("shipper_success");
        validRequest.setPassword("SecurePass123");
        validRequest.setEmail("success@example.com");
        validRequest.setName("Success Shipper");
        validRequest.setPhone("+84907777777");
        validRequest.setVehicleType("Car");
        validRequest.setVehicleNumber("59A-77777");

        // When & Then - PRESERVED BEHAVIOR: Creation succeeds
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("shipper_success"))
                .andExpect(jsonPath("$.email").value("success@example.com"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify user was created in database
        User createdUser = userRepository.findByUsername("shipper_success").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getRole()).isEqualTo("SHIPPER");
        assertThat(createdUser.getEmail()).isEqualTo("success@example.com");
    }

    /**
     * PRESERVATION TEST 6: Non-Shipper User Authentication
     * **Validates: Requirement 3.3**
     * 
     * Observes: Regular users (non-shipper role) authenticate normally
     * Expected: Authentication succeeds without any shipper profile checks
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    void testPreservation_NonShipperAuthentication_NoShipperChecks() throws Exception {
        // Given - Regular user exists (not a shipper)
        userRepository.deleteAll();

        User regularUser = new User();
        regularUser.setUsername("regular_user");
        regularUser.setEmail("regular@example.com");
        regularUser.setPasswordHash("$2a$10$YourEncodedPasswordHashHere");  // BCrypt encoded
        regularUser.setRole("USER");  // Not a SHIPPER
        regularUser.setAccountLocked(false);
        userRepository.save(regularUser);

        // Mock authentication
        when(jwtUtil.generateToken("regular_user", regularUser.getId(), "USER", regularUser.getEmail()))
            .thenReturn("regular.user.jwt.token");

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("regular_user");
        loginRequest.setPassword("password123");

        // When - Regular user logs in
        // Then - PRESERVED BEHAVIOR: Authentication succeeds without shipper checks
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("USER"));

        // Note: This test verifies that non-shipper users are not affected by shipper logic
    }

    /**
     * PRESERVATION TEST 7: Existing Shipper Profile Remains Functional
     * **Validates: Requirement 3.4**
     * 
     * Observes: Existing shipper accounts continue to work after fix
     * Expected: No duplicate profiles created, existing profile remains valid
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_ExistingShipperProfile_RemainsFunctional() throws Exception {
        // Given - Existing shipper user in database
        userRepository.deleteAll();

        User existingShipper = new User();
        existingShipper.setUsername("existing_shipper");
        existingShipper.setEmail("existing@example.com");
        existingShipper.setPasswordHash("$2a$10$ExistingShipperPasswordHash");
        existingShipper.setRole("SHIPPER");
        existingShipper.setAccountLocked(false);
        User savedShipper = userRepository.save(existingShipper);

        // Mock JWT generation for login
        when(jwtUtil.generateToken("existing_shipper", savedShipper.getId(), "SHIPPER", savedShipper.getEmail()))
            .thenReturn("existing.shipper.jwt.token");

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("existing_shipper");
        loginRequest.setPassword("password123");

        // When - Existing shipper logs in
        // Then - PRESERVED BEHAVIOR: Authentication succeeds normally
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("existing_shipper"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify user still exists and unchanged
        User verifyUser = userRepository.findByUsername("existing_shipper").orElse(null);
        assertThat(verifyUser).isNotNull();
        assertThat(verifyUser.getRole()).isEqualTo("SHIPPER");
        assertThat(verifyUser.getId()).isEqualTo(savedShipper.getId());
    }

    /**
     * PRESERVATION TEST 8: JWT Token Structure and Claims
     * **Validates: Requirement 3.5**
     * 
     * Observes: JWT tokens contain required claims for authorization
     * Expected: Token contains userId, username, email, role claims
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPreservation_JwtTokenStructure_ContainsRequiredClaims() throws Exception {
        // Given - Order Service is available
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        userRepository.deleteAll();

        ShipperRegistrationRequest request = new ShipperRegistrationRequest();
        request.setUsername("shipper_jwt_test");
        request.setPassword("SecurePass123");
        request.setEmail("jwt.test@example.com");
        request.setName("JWT Test Shipper");
        request.setPhone("+84908888888");
        request.setVehicleType("Bike");
        request.setVehicleNumber("59A-88888");

        // When - Admin creates shipper
        // Then - PRESERVED BEHAVIOR: Response contains all required token fields
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())  // JWT token
                .andExpect(jsonPath("$.type").value("Bearer"))  // Token type
                .andExpect(jsonPath("$.userId").exists())  // User ID claim
                .andExpect(jsonPath("$.username").exists())  // Username claim
                .andExpect(jsonPath("$.email").exists())  // Email claim
                .andExpect(jsonPath("$.role").value("SHIPPER"));  // Role claim

        // Note: After fix, email will also be in JWT token itself (not just response)
        // This test verifies the response structure is preserved
    }

    /**
     * PRESERVATION TEST 9: Authorization Protected Endpoints
     * **Validates: Requirement 3.5**
     * 
     * Observes: Only ADMIN role can create shippers
     * Expected: Non-admin users receive 403 Forbidden
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    @WithMockUser(roles = "USER")
    void testPreservation_Authorization_NonAdminForbidden() throws Exception {
        // Given - Regular user (not admin) attempts to create shipper
        ShipperRegistrationRequest request = new ShipperRegistrationRequest();
        request.setUsername("unauthorized_shipper");
        request.setPassword("SecurePass123");
        request.setEmail("unauthorized@example.com");
        request.setName("Unauthorized Shipper");
        request.setPhone("+84909999999");
        request.setVehicleType("Car");
        request.setVehicleNumber("59A-99999");

        // When & Then - PRESERVED BEHAVIOR: Non-admin receives 403 Forbidden
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Verify no user was created
        User user = userRepository.findByUsername("unauthorized_shipper").orElse(null);
        assertThat(user).isNull();
    }

    /**
     * PRESERVATION TEST 10: Unauthenticated Access Denied
     * **Validates: Requirement 3.5**
     * 
     * Observes: Unauthenticated requests are rejected
     * Expected: Unauthenticated access receives 401 Unauthorized
     * 
     * This test MUST PASS on unfixed code (baseline behavior to preserve)
     */
    @Test
    void testPreservation_Authentication_UnauthenticatedDenied() throws Exception {
        // Given - Unauthenticated request (no @WithMockUser)
        ShipperRegistrationRequest request = new ShipperRegistrationRequest();
        request.setUsername("unauthenticated_shipper");
        request.setPassword("SecurePass123");
        request.setEmail("unauth@example.com");
        request.setName("Unauth Shipper");
        request.setPhone("+84900000000");
        request.setVehicleType("Bike");
        request.setVehicleNumber("59A-00000");

        // When & Then - PRESERVED BEHAVIOR: Unauthenticated receives 401 Unauthorized
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        // Verify no user was created
        User user = userRepository.findByUsername("unauthenticated_shipper").orElse(null);
        assertThat(user).isNull();
    }
}
