package com.example.foodshop.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    
    // Production backend URL
    private static final String BASE_URL = "https://food-shop-iswi.onrender.com";
    
    // For local development, uncomment this:
    // private static final String BASE_URL = "http://localhost:8080";
    
    private static ApiClient instance;
    private final OkHttpClient client;
    private final Gson gson;
    private String jwtToken;
    
    private ApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    public void setJwtToken(String token) {
        this.jwtToken = token;
    }
    
    public String getJwtToken() {
        return jwtToken;
    }
    
    public boolean isLoggedIn() {
        return jwtToken != null && !jwtToken.isEmpty();
    }
    
    // Login
    public LoginResponse login(String username, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String json = gson.toJson(loginRequest);
        
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/login")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Đăng nhập thất bại: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, LoginResponse.class);
        }
    }
    
    // Register
    public String register(String username, String password, String email, String fullName) throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(username, password, email, fullName);
        String json = gson.toJson(registerRequest);
        
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/register")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Đăng ký thất bại: " + errorBody);
            }
            
            return response.body().string();
        }
    }
    
    // Get all products
    public List<ProductDTO> getAllProducts() throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + "/api/products")
                .get();
        
        if (isLoggedIn()) {
            requestBuilder.addHeader("Authorization", "Bearer " + jwtToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải danh sách sản phẩm: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, new TypeToken<List<ProductDTO>>(){}.getType());
        }
    }
    
    // Get my orders
    public List<OrderDTO> getMyOrders() throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/orders/my-orders")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải đơn hàng: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, new TypeToken<List<OrderDTO>>(){}.getType());
        }
    }
    
    // Cancel order
    public CancelOrderResponse cancelOrder(Long orderId) throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        RequestBody body = RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/orders/" + orderId + "/cancel")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            System.out.println("Cancel order response: " + responseBody);
            System.out.println("Response code: " + response.code());
            
            if (!response.isSuccessful()) {
                // Parse error response
                try {
                    CancelOrderResponse errorResponse = gson.fromJson(responseBody, CancelOrderResponse.class);
                    String errorMsg = errorResponse.error != null ? errorResponse.error : "Không thể hủy đơn hàng";
                    if (errorResponse.reason != null) {
                        errorMsg += ": " + errorResponse.reason;
                    }
                    throw new IOException(errorMsg);
                } catch (Exception e) {
                    throw new IOException("Không thể hủy đơn hàng (HTTP " + response.code() + "): " + responseBody);
                }
            }
            
            return gson.fromJson(responseBody, CancelOrderResponse.class);
        }
    }
    
    // Create order
    public OrderDTO createOrder(OrderRequest orderRequest) throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        String json = gson.toJson(orderRequest);
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/orders/create")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tạo đơn hàng: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, OrderDTO.class);
        }
    }
    
    // Get product reviews
    public List<ReviewDTO> getProductReviews(Long productId) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/reviews/product/" + productId)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải đánh giá: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, new TypeToken<List<ReviewDTO>>(){}.getType());
        }
    }
    
    // Create review
    public String createReview(ReviewRequest reviewRequest) throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        String json = gson.toJson(reviewRequest);
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/reviews/create")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Không thể gửi đánh giá: " + errorBody);
            }
            
            return response.body().string();
        }
    }
    
    // Send chat message to AI chatbot
    public String sendChatMessage(String message) throws IOException {
        System.out.println("Sending chat message: " + message);
        
        ChatRequest chatRequest = new ChatRequest(message);
        String json = gson.toJson(chatRequest);
        System.out.println("Request JSON: " + json);
        
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + "/api/chatbot/chat")
                .post(body);
        
        // Add auth header if logged in
        if (isLoggedIn()) {
            requestBuilder.addHeader("Authorization", "Bearer " + jwtToken);
            System.out.println("Added auth header");
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response code: " + response.code());
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                System.err.println("Chatbot error response: " + errorBody);
                throw new IOException("Chatbot error: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            System.out.println("Response body: " + responseBody);
            
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new IOException("Empty response from chatbot");
            }
            
            ChatResponse chatResponse = gson.fromJson(responseBody, ChatResponse.class);
            if (chatResponse == null || chatResponse.getResponse() == null) {
                throw new IOException("Invalid response format from chatbot");
            }
            
            System.out.println("Parsed response: " + chatResponse.getResponse());
            return chatResponse.getResponse();
        } catch (Exception e) {
            System.err.println("Exception in sendChatMessage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Validate voucher
    public VoucherValidationResponse validateVoucher(String voucherCode, Double orderTotal) throws IOException {
        System.out.println("Validating voucher: " + voucherCode + ", orderTotal: " + orderTotal);
        
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        // Use GET request with query params instead of POST with JSON body
        String url = BASE_URL + "/api/vouchers/validate?code=" + voucherCode + "&orderTotal=" + orderTotal;
        System.out.println("Request URL: " + url);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .get()  // Changed from POST to GET
                .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body().string();
            System.out.println("Response code: " + response.code());
            System.out.println("Response body: " + responseBody);
            
            if (!response.isSuccessful()) {
                // Backend returns plain text error for GET endpoint
                throw new IOException(responseBody);
            }
            
            // Parse response - backend returns Map<String, Object>
            // We need to convert it to VoucherValidationResponse
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> responseMap = gson.fromJson(responseBody, java.util.Map.class);
            
            VoucherValidationResponse result = new VoucherValidationResponse();
            result.valid = responseMap.get("valid") != null ? (Boolean) responseMap.get("valid") : true;
            result.message = "Áp dụng mã giảm giá thành công!";
            
            // Get discount amount
            Object discountObj = responseMap.get("discountAmount");
            if (discountObj instanceof Number) {
                result.discountAmount = ((Number) discountObj).doubleValue();
            }
            
            // Calculate final amount
            result.finalAmount = orderTotal - result.discountAmount;
            
            System.out.println("Parsed response: valid=" + result.valid + 
                             ", discount=" + result.discountAmount);
            return result;
        } catch (IOException e) {
            System.err.println("Exception in validateVoucher: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // DTOs
    public static class LoginRequest {
        private String username;
        private String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String fullName;
        
        public RegisterRequest(String username, String password, String email, String fullName) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullName = fullName;
        }
    }
    
    public static class LoginResponse {
        private String token;
        private String username;
        private String role;
        
        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
    
    public static class ProductDTO {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private String category;
        private Integer stock;
        private String image;
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Double getPrice() { return price; }
        public String getCategory() { return category; }
        public Integer getStock() { return stock; }
        public String getImage() { return image; }
    }
    
    public static class OrderRequest {
        private String shippingAddress;
        private String paymentMethod;
        private String voucherCode;
        private List<OrderItemRequest> items;
        
        public OrderRequest(String shippingAddress, String paymentMethod, List<OrderItemRequest> items) {
            this.shippingAddress = shippingAddress;
            this.paymentMethod = paymentMethod;
            this.items = items;
        }
        
        public void setVoucherCode(String voucherCode) {
            this.voucherCode = voucherCode;
        }
    }
    
    public static class OrderItemRequest {
        private Long id;  // Backend expects "id" not "productId"
        private Integer quantity;
        
        public OrderItemRequest(Long productId, Integer quantity) {
            this.id = productId;
            this.quantity = quantity;
        }
        
        public Long getId() { return id; }
        public Integer getQuantity() { return quantity; }
    }
    
    public static class OrderDTO {
        private Long id;
        private String shippingAddress;
        private String paymentMethod;
        private Double totalAmount;
        private String status;
        private String createdAt;
        private List<OrderItemDTO> orderItems;
        
        public Long getId() { return id; }
        public String getShippingAddress() { return shippingAddress; }
        public String getPaymentMethod() { return paymentMethod; }
        public Double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
        public List<OrderItemDTO> getOrderItems() { return orderItems; }
    }
    
    public static class OrderItemDTO {
        private Long id;
        private Integer quantity;
        private Double price;
        private ProductDTO product;
        
        public Long getId() { return id; }
        public Integer getQuantity() { return quantity; }
        public Double getPrice() { return price; }
        public ProductDTO getProduct() { return product; }
    }
    
    public static class ReviewRequest {
        private Long productId;
        private Long orderId;
        private Integer rating;
        private String comment;
        
        public ReviewRequest(Long productId, Long orderId, Integer rating, String comment) {
            this.productId = productId;
            this.orderId = orderId;
            this.rating = rating;
            this.comment = comment;
        }
    }
    
    public static class ReviewDTO {
        private Long id;
        private Integer rating;
        private String comment;
        private String createdAt;
        private UserDTO user;
        
        public Long getId() { return id; }
        public Integer getRating() { return rating; }
        public String getComment() { return comment; }
        public String getCreatedAt() { return createdAt; }
        public UserDTO getUser() { return user; }
    }
    
    public static class UserDTO {
        private Long id;
        private String username;
        private String fullName;
        
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
    }
    
    // Cancel Order Response
    public static class CancelOrderResponse {
        public boolean success;
        public String message;
        public String warning;
        public String error;
        public String reason;
        public Integer cancelledCount;
    }
    
    // Chat Request/Response
    public static class ChatRequest {
        private String message;
        
        public ChatRequest(String message) {
            this.message = message;
        }
    }
    
    public static class ChatResponse {
        private String message;  // Backend uses "message" not "response"
        private String type;
        private Map<String, Object> data;
        
        public String getResponse() { 
            return message;  // Map to getResponse() for compatibility
        }
        
        public String getMessage() { return message; }
        public String getType() { return type; }
        public Map<String, Object> getData() { return data; }
    }
    
    // Voucher Validation
    public static class VoucherValidationRequest {
        private String code;
        private Double orderTotal;
        
        public VoucherValidationRequest(String code, Double orderTotal) {
            this.code = code;
            this.orderTotal = orderTotal;
        }
    }
    
    public static class VoucherValidationResponse {
        public boolean valid;
        public String message;
        public Double discountAmount;
        public Double finalAmount;
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Double getDiscountAmount() { return discountAmount; }
        public Double getFinalAmount() { return finalAmount; }
    }
}
