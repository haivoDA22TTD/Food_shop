package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.dto.ShipperRegistrationRequest;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.repository.UserRepository;
import com.example.foodshop.identity.security.JwtUtil;
import com.example.foodshop.identity.service.AuthRateLimitService;
import com.example.foodshop.identity.service.TokenBlacklistService;
import com.example.foodshop.identity.service.UserService;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthController create-shipper endpoint
 * Tests admin-only access, validation, and integration with UserService
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerCreateShipperTest {

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

    private ShipperRegistrationRequest validRequest;
    private User mockShipperUser;

    @BeforeEach
    void setUp() {
        // Create valid shipper registration request
        validRequest = new ShipperRegistrationRequest();
        validRequest.setUsername("shipper123");
        validRequest.setPassword("SecurePass123");
        validRequest.setEmail("shipper123@example.com");
        validRequest.setName("John Doe");
        validRequest.setPhone("+84901234567");
        validRequest.setVehicleType("Motorbike");
        validRequest.setVehicleNumber("59A-12345");

        // Create mock user entity
        mockShipperUser = new User();
        mockShipperUser.setId(123L);
        mockShipperUser.setUsername("shipper123");
        mockShipperUser.setEmail("shipper123@example.com");
        mockShipperUser.setRole("SHIPPER");

        // Mock JWT generation
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
            .thenReturn("mock.jwt.token");
        
        // Mock Redis-dependent services
        when(authRateLimitService.allow(anyString(), anyInt(), any()))
            .thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(anyString()))
            .thenReturn(false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_Success() throws Exception {
        // Given - Order Service is available and responds successfully
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Shipper profile created", HttpStatus.CREATED));

        // Clear any existing test data
        userRepository.deleteAll();

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.username").value("shipper123"))
                .andExpect(jsonPath("$.email").value("shipper123@example.com"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify user was created in database
        User createdUser = userRepository.findByUsername("shipper123").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getRole()).isEqualTo("SHIPPER");
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateShipper_Forbidden_NonAdminUser() throws Exception {
        // When & Then - non-admin user should get 403 Forbidden
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateShipper_Unauthorized_NoAuthentication() throws Exception {
        // When & Then - unauthenticated request should get 401 Unauthorized
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_MissingUsername() throws Exception {
        // Given - request without username
        validRequest.setUsername(null);

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required fields: username, password, email, name, phone"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_MissingPassword() throws Exception {
        // Given - request without password
        validRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_UsernameAlreadyExists() throws Exception {
        // Given - create a user first
        userRepository.deleteAll();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        // When & Then - try to create again with same username
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_EmailAlreadyExists() throws Exception {
        // Given - create a user first
        userRepository.deleteAll();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        // When & Then - try to create with different username but same email
        ShipperRegistrationRequest duplicateEmailRequest = new ShipperRegistrationRequest();
        duplicateEmailRequest.setUsername("different_username");
        duplicateEmailRequest.setPassword("SecurePass123");
        duplicateEmailRequest.setEmail(validRequest.getEmail());  // Same email
        duplicateEmailRequest.setName("Different Name");
        duplicateEmailRequest.setPhone("+84987654321");

        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_InvalidEmailFormat() throws Exception {
        // Given - request with invalid email format
        validRequest.setEmail("invalid-email-format");

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_InternalServerError_OrderServiceFailure() throws Exception {
        // Given - Order Service fails with exception
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RestClientException("Unable to create shipper profile in Order Service"));

        userRepository.deleteAll();

        // When & Then - On unfixed code, this returns 500 error (current buggy behavior)
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.detail").exists());

        // Verify User was NOT created due to rollback (current buggy behavior)
        User user = userRepository.findByUsername("shipper123").orElse(null);
        assertThat(user).isNull();  // User should NOT exist due to transaction rollback
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_Success_WithOptionalFields() throws Exception {
        // Given - request with all optional fields and Order Service available
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        userRepository.deleteAll();

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.role").value("SHIPPER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_AuthResponseFlatStructure() throws Exception {
        // Given - Order Service available
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

        userRepository.deleteAll();

        // When & Then - verify response has flat structure (no nested objects)
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.username").isString())
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.role").isString())
                // Ensure no nested objects
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.userDetails").doesNotExist());
    }

    /**
     * BUG CONDITION EXPLORATION TEST
     * **Validates: Requirements 1.1, 1.2, 1.3**
     * 
     * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
     * 
     * Property 1: Bug Condition - Shipper Creation Fails When Order Service Unavailable
     * 
     * This test encodes the EXPECTED behavior after the fix:
     * - User entity with SHIPPER role SHOULD be created successfully
     * - Success response SHOULD be returned to admin
     * - No transaction rollback SHOULD occur
     * 
     * On UNFIXED code, this test will FAIL because:
     * - Order Service call times out/fails
     * - Transaction is rolled back
     * - No User entity is created
     * - Error response is returned
     * 
     * After the fix is implemented, this test will PASS, confirming the expected behavior.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testBugCondition_ShipperCreation_SucceedsWhenOrderServiceUnavailable_ConnectionRefused() throws Exception {
        // Given - Order Service is unavailable (connection refused - service sleeping/down)
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new ResourceAccessException("I/O error on POST request", 
                new ConnectException("Connection refused")));

        // Clear any existing test data
        userRepository.deleteAll();

        // Create valid shipper registration request
        ShipperRegistrationRequest request = new ShipperRegistrationRequest();
        request.setUsername("shipper_test_unavailable");
        request.setPassword("SecurePass123");
        request.setEmail("shipper.unavailable@example.com");
        request.setName("Test Shipper");
        request.setPhone("+84901234567");
        request.setVehicleType("Motorbike");
        request.setVehicleNumber("59A-12345");

        // Mock JWT generation to return a valid token
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
            .thenReturn("mock.jwt.token.for.shipper");

        // When - Admin attempts to create shipper account
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // Then - EXPECTED BEHAVIOR (after fix):
                .andExpect(status().isCreated())  // Should return 201 Created
                .andExpect(jsonPath("$.token").exists())  // Should return JWT token
                .andExpect(jsonPath("$.userId").exists())  // Should return userId
                .andExpect(jsonPath("$.username").value("shipper_test_unavailable"))
                .andExpect(jsonPath("$.email").value("shipper.unavailable@example.com"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify User entity was created in database (expected behavior after fix)
        User createdUser = userRepository.findByUsername("shipper_test_unavailable")
            .orElse(null);
        assertThat(createdUser).isNotNull();  // User SHOULD exist
        assertThat(createdUser.getRole()).isEqualTo("SHIPPER");
        assertThat(createdUser.getEmail()).isEqualTo("shipper.unavailable@example.com");
    }

    /**
     * BUG CONDITION EXPLORATION TEST - Timeout Scenario
     * **Validates: Requirements 1.1, 1.2, 1.3**
     * 
     * Tests the same bug condition but with Order Service timeout instead of connection refused
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testBugCondition_ShipperCreation_SucceedsWhenOrderServiceUnavailable_Timeout() throws Exception {
        // Given - Order Service times out (service sleeping and takes too long to wake up)
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new ResourceAccessException("Read timed out", 
                new SocketTimeoutException("Read timed out")));

        // Clear any existing test data
        userRepository.deleteAll();

        // Create valid shipper registration request
        ShipperRegistrationRequest request = new ShipperRegistrationRequest();
        request.setUsername("shipper_test_timeout");
        request.setPassword("SecurePass123");
        request.setEmail("shipper.timeout@example.com");
        request.setName("Test Shipper Timeout");
        request.setPhone("+84901234568");
        request.setVehicleType("Bike");
        request.setVehicleNumber("59A-12346");

        // Mock JWT generation
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
            .thenReturn("mock.jwt.token.timeout");

        // When - Admin attempts to create shipper account
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // Then - EXPECTED BEHAVIOR (after fix):
                .andExpect(status().isCreated())  // Should return 201 Created
                .andExpect(jsonPath("$.token").exists())  // Should return JWT token
                .andExpect(jsonPath("$.userId").exists())  // Should return userId
                .andExpect(jsonPath("$.username").value("shipper_test_timeout"))
                .andExpect(jsonPath("$.email").value("shipper.timeout@example.com"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify User entity was created in database (expected behavior after fix)
        User createdUser = userRepository.findByUsername("shipper_test_timeout")
            .orElse(null);
        assertThat(createdUser).isNotNull();  // User SHOULD exist
        assertThat(createdUser.getRole()).isEqualTo("SHIPPER");
        assertThat(createdUser.getEmail()).isEqualTo("shipper.timeout@example.com");
    }

    /**
     * BUG CONDITION EXPLORATION TEST - Generic Error Scenario
     * **Validates: Requirements 1.1, 1.2, 1.3**
     * 
     * Tests the same bug condition but with generic Order Service error response
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testBugCondition_ShipperCreation_SucceedsWhenOrderServiceUnavailable_ErrorResponse() throws Exception {
        // Given - Order Service returns error response (503 Service Unavailable)
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE));

        // Clear any existing test data
        userRepository.deleteAll();

        // Create valid shipper registration request
        ShipperRegistrationRequest request = new ShipperRegistrationRequest();
        request.setUsername("shipper_test_error");
        request.setPassword("SecurePass123");
        request.setEmail("shipper.error@example.com");
        request.setName("Test Shipper Error");
        request.setPhone("+84901234569");
        request.setVehicleType("Car");
        request.setVehicleNumber("59A-12347");

        // Mock JWT generation
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
            .thenReturn("mock.jwt.token.error");

        // When - Admin attempts to create shipper account
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // Then - EXPECTED BEHAVIOR (after fix):
                .andExpect(status().isCreated())  // Should return 201 Created
                .andExpect(jsonPath("$.token").exists())  // Should return JWT token
                .andExpect(jsonPath("$.userId").exists())  // Should return userId
                .andExpect(jsonPath("$.username").value("shipper_test_error"))
                .andExpect(jsonPath("$.email").value("shipper.error@example.com"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify User entity was created in database (expected behavior after fix)
        User createdUser = userRepository.findByUsername("shipper_test_error")
            .orElse(null);
        assertThat(createdUser).isNotNull();  // User SHOULD exist
        assertThat(createdUser.getRole()).isEqualTo("SHIPPER");
        assertThat(createdUser.getEmail()).isEqualTo("shipper.error@example.com");
    }
}
