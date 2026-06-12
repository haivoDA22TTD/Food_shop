package com.example.foodshop.identity.controller;

import com.example.foodshop.identity.config.TestConfig;
import com.example.foodshop.identity.dto.ShipperRegistrationRequest;
import com.example.foodshop.identity.entity.User;
import com.example.foodshop.identity.security.JwtUtil;
import com.example.foodshop.identity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
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
@Import(TestConfig.class)
class AuthControllerCreateShipperTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

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
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_Success() throws Exception {
        // Given
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenReturn(mockShipperUser);

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.username").value("shipper123"))
                .andExpect(jsonPath("$.email").value("shipper123@example.com"))
                .andExpect(jsonPath("$.role").value("SHIPPER"));

        // Verify service was called
        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
        verify(jwtUtil, times(1)).generateToken("shipper123", 123L, "SHIPPER");
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateShipper_Forbidden_NonAdminUser() throws Exception {
        // When & Then - non-admin user should get 403 Forbidden
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        // Verify service was never called
        verify(userService, never()).registerShipper(any());
    }

    @Test
    void testCreateShipper_Unauthorized_NoAuthentication() throws Exception {
        // When & Then - unauthenticated request should get 401 Unauthorized
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());

        // Verify service was never called
        verify(userService, never()).registerShipper(any());
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

        // Verify service was never called
        verify(userService, never()).registerShipper(any());
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

        // Verify service was never called
        verify(userService, never()).registerShipper(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_UsernameAlreadyExists() throws Exception {
        // Given
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));

        // Verify service was called
        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_EmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));

        // Verify service was called
        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_BadRequest_InvalidEmailFormat() throws Exception {
        // Given
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenThrow(new RuntimeException("Invalid email format"));

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format"));

        // Verify service was called
        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_InternalServerError_OrderServiceFailure() throws Exception {
        // Given
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenThrow(new RuntimeException("Unable to create shipper profile in Order Service"));

        // When & Then
        mockMvc.perform(post("/api/auth/create-shipper")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.detail").exists());

        // Verify service was called
        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_Success_WithOptionalFields() throws Exception {
        // Given - request with all optional fields
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenReturn(mockShipperUser);

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

        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateShipper_AuthResponseFlatStructure() throws Exception {
        // Given
        when(userService.registerShipper(any(ShipperRegistrationRequest.class)))
            .thenReturn(mockShipperUser);

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

        verify(userService, times(1)).registerShipper(any(ShipperRegistrationRequest.class));
    }
}
