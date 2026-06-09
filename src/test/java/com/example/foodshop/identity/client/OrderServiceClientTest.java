package com.example.foodshop.identity.client;

import com.example.foodshop.identity.client.OrderServiceClient.CreateShipperProfileRequest;
import com.example.foodshop.identity.client.OrderServiceClient.OrderServiceException;
import com.example.foodshop.identity.client.OrderServiceClient.ShipperResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceClient.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private OrderServiceClient orderServiceClient;
    
    private static final String ORDER_SERVICE_URL = "http://localhost:8082";
    
    @BeforeEach
    void setUp() {
        orderServiceClient = new OrderServiceClient(restTemplate, ORDER_SERVICE_URL);
    }
    
    @Test
    void createShipperProfile_Success() {
        // Arrange
        CreateShipperProfileRequest request = new CreateShipperProfileRequest(
            1L, "John Doe", "+84901234567", "john@example.com", "Motorbike", "59A-12345"
        );
        
        ShipperResponse expectedResponse = new ShipperResponse(
            1L, 1L, "John Doe", "+84901234567", "john@example.com",
            "AVAILABLE", "Motorbike", "59A-12345", 0, 0, 5.0, true, "2024-01-15T10:30:00"
        );
        
        ResponseEntity<ShipperResponse> responseEntity = 
            new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);
        
        when(restTemplate.exchange(
            eq(ORDER_SERVICE_URL + "/internal/shippers"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(ShipperResponse.class)
        )).thenReturn(responseEntity);
        
        // Act
        ShipperResponse result = orderServiceClient.createShipperProfile(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getUserId(), result.getUserId());
        assertEquals(expectedResponse.getName(), result.getName());
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(Class.class));
    }
    
    @Test
    void createShipperProfile_ClientError_NoRetry() {
        // Arrange
        CreateShipperProfileRequest request = new CreateShipperProfileRequest(
            1L, "John Doe", "+84901234567", "john@example.com", "Motorbike", "59A-12345"
        );
        
        HttpClientErrorException clientError = 
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Phone already exists");
        
        when(restTemplate.exchange(
            anyString(), any(), any(), any(Class.class)
        )).thenThrow(clientError);
        
        // Act & Assert
        OrderServiceException exception = assertThrows(
            OrderServiceException.class,
            () -> orderServiceClient.createShipperProfile(request)
        );
        
        assertTrue(exception.getMessage().contains("Failed to create shipper profile"));
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(Class.class));
    }
    
    @Test
    void createShipperProfile_ServerError_RetriesAndFails() {
        // Arrange
        CreateShipperProfileRequest request = new CreateShipperProfileRequest(
            1L, "John Doe", "+84901234567", "john@example.com", "Motorbike", "59A-12345"
        );
        
        HttpServerErrorException serverError = 
            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        
        when(restTemplate.exchange(
            anyString(), any(), any(), any(Class.class)
        )).thenThrow(serverError);
        
        // Act & Assert
        OrderServiceException exception = assertThrows(
            OrderServiceException.class,
            () -> orderServiceClient.createShipperProfile(request)
        );
        
        assertTrue(exception.getMessage().contains("Failed to create shipper profile after 3 attempts"));
        verify(restTemplate, times(3)).exchange(anyString(), any(), any(), any(Class.class));
    }
    
    @Test
    void createShipperProfile_ConnectionError_RetriesAndFails() {
        // Arrange
        CreateShipperProfileRequest request = new CreateShipperProfileRequest(
            1L, "John Doe", "+84901234567", "john@example.com", "Motorbike", "59A-12345"
        );
        
        ResourceAccessException connectionError = 
            new ResourceAccessException("Connection refused");
        
        when(restTemplate.exchange(
            anyString(), any(), any(), any(Class.class)
        )).thenThrow(connectionError);
        
        // Act & Assert
        OrderServiceException exception = assertThrows(
            OrderServiceException.class,
            () -> orderServiceClient.createShipperProfile(request)
        );
        
        assertTrue(exception.getMessage().contains("Failed to connect to Order Service after 3 attempts"));
        verify(restTemplate, times(3)).exchange(anyString(), any(), any(), any(Class.class));
    }
    
    @Test
    void createShipperProfile_ServerError_RetriesAndSucceeds() {
        // Arrange
        CreateShipperProfileRequest request = new CreateShipperProfileRequest(
            1L, "John Doe", "+84901234567", "john@example.com", "Motorbike", "59A-12345"
        );
        
        ShipperResponse expectedResponse = new ShipperResponse(
            1L, 1L, "John Doe", "+84901234567", "john@example.com",
            "AVAILABLE", "Motorbike", "59A-12345", 0, 0, 5.0, true, "2024-01-15T10:30:00"
        );
        
        ResponseEntity<ShipperResponse> successResponse = 
            new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);
        
        HttpServerErrorException serverError = 
            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Temporary error");
        
        // First call fails, second call succeeds
        when(restTemplate.exchange(
            anyString(), any(), any(), any(Class.class)
        )).thenThrow(serverError)
          .thenReturn(successResponse);
        
        // Act
        ShipperResponse result = orderServiceClient.createShipperProfile(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        verify(restTemplate, times(2)).exchange(anyString(), any(), any(), any(Class.class));
    }
}
